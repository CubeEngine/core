package de.cubeisland.cubeengine.core.bukkit;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet204LocaleAndViewDistance;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeEngineNetServerHandler extends NetServerHandler
{
    private final PluginManager pm;
    
    public CubeEngineNetServerHandler(EntityPlayer player)
    {
        super(player.server, player.netServerHandler.networkManager, player);
        this.pm = player.getBukkitEntity().getServer().getPluginManager();
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        super.a(packet204localeandviewdistance);
        this.pm.callEvent(new PlayerLanguageReceivedEvent(this.player.getBukkitEntity(), packet204localeandviewdistance.d()));
    }
}
