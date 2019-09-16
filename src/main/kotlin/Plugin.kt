package hazae41.minecraft.backtomyserver

import hazae41.minecraft.kotlin.bungee.BungeePlugin
import hazae41.minecraft.kotlin.bungee.PluginConfigFile
import hazae41.minecraft.kotlin.bungee.command
import hazae41.minecraft.kotlin.bungee.info
import hazae41.minecraft.kotlin.bungee.init
import hazae41.minecraft.kotlin.bungee.listen
import hazae41.minecraft.kotlin.bungee.msg
import hazae41.minecraft.kotlin.bungee.update
import hazae41.minecraft.kotlin.catch
import hazae41.minecraft.kotlin.ex
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent.Reason.JOIN_PROXY

object Config : PluginConfigFile("config"){
    val debug by boolean("debug")
    val force by boolean("force")
    val blacklist by stringList("blacklist")
}

object Players : PluginConfigFile("players"){
    class Player(player: ProxiedPlayer) {
        var reconnectTo by string(player.uniqueId.toString())
    }
}

class Plugin : BungeePlugin(){

    var ProxiedPlayer.reconnectTo: ServerInfo?
        get() = Players.Player(this).reconnectTo.let(proxy::getServerInfo)
        set(value) { Players.Player(this).reconnectTo = value?.name ?: return }

    val ProxiedPlayer.forcedHost
        get() = pendingConnection.run {
            virtualHost.hostString in listener.forcedHosts
        }

    fun debug(msg: String) {
        if(Config.debug) info(msg)
    }

    override fun onEnable() {
        update(51010)
        init(Config, Players)

        listen<ServerConnectEvent>{
            if(it.reason != JOIN_PROXY) return@listen
            if(it.player.forcedHost && !Config.force) return@listen
            it.target = it.player.reconnectTo ?: return@listen
            debug("Redirecting ${it.player.name} to ${it.target.name}")
        }

        listen<PlayerDisconnectEvent>{
            val server = it.player.server?.info ?: return@listen
            if(server.name in Config.blacklist) return@listen
            it.player.reconnectTo = server
            debug("Saved ${it.player.name} to ${server.name}")
        }

        command("backtomyserver", "backtomyserver.send", "btms"){ args ->
            catch<Exception>(::msg){
                val pname = args.getOrNull(0) ?: throw ex("&cUsage: /btms <player>")
                val player = proxy.matchPlayer(pname).firstOrNull() ?: throw ex("&cUnknown player")
                val server = player.reconnectTo ?: throw ex("&cUnknown reconnect server")
                player.connect(server)
                throw ex("&bSuccessfully sent ${player.name} to ${server.name}")
            }
        }
    }
}