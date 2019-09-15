package hazae41.minecraft.backtomyserver

import hazae41.minecraft.kotlin.bungee.BungeePlugin
import hazae41.minecraft.kotlin.bungee.PluginConfigFile
import hazae41.minecraft.kotlin.bungee.init
import hazae41.minecraft.kotlin.bungee.listen
import hazae41.minecraft.kotlin.bungee.update
import net.md_5.bungee.api.event.PlayerDisconnectEvent

object Config : PluginConfigFile("config"){
    val blacklist by stringList("blacklist")
}

class Plugin : BungeePlugin(){
    override fun onEnable() {
        update(51010)
        init(Config)

        listen<PlayerDisconnectEvent>{
            val server = it.player.server.info
            if(server.name !in Config.blacklist)
                it.player.reconnectServer = server
        }
    }
}