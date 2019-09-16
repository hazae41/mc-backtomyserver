package hazae41.minecraft.backtomyserver

import hazae41.minecraft.kotlin.bungee.BungeePlugin
import hazae41.minecraft.kotlin.bungee.PluginConfigFile
import hazae41.minecraft.kotlin.bungee.command
import hazae41.minecraft.kotlin.bungee.init
import hazae41.minecraft.kotlin.bungee.listen
import hazae41.minecraft.kotlin.bungee.msg
import hazae41.minecraft.kotlin.bungee.update
import hazae41.minecraft.kotlin.catch
import hazae41.minecraft.kotlin.ex
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent.Reason.JOIN_PROXY
import java.lang.Exception

object Config : PluginConfigFile("config"){
    val force by boolean("force")
    val blacklist by stringList("blacklist")
}

class Plugin : BungeePlugin(){
    override fun onEnable() {
        update(51010)
        init(Config)

        listen<ServerConnectEvent>{
            if(it.reason != JOIN_PROXY) return@listen
            if(!Config.force) return@listen
            it.target = it.player.reconnectServer
        }

        listen<PlayerDisconnectEvent>{
            val server = it.player.server.info
            if(server.name in Config.blacklist) return@listen
            it.player.reconnectServer = server
        }

        command("backtomyserver", "backtomyserver.send", "btms"){ args ->
            catch<Exception>(::msg){
                val name = args.getOrNull(0) ?: throw ex("/btms <player>")
                val player = proxy.matchPlayer(name).firstOrNull() ?: throw ex("Unknown player")
                player.connect(player.reconnectServer)
            }
        }
    }
}