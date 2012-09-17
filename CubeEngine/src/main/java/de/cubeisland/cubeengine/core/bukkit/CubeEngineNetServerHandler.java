package de.cubeisland.cubeengine.core.bukkit;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet204LocaleAndViewDistance;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeEngineNetServerHandler extends NetServerHandler
{
    private final PluginManager pm;
    private final NetServerHandler parent;
    
    public CubeEngineNetServerHandler(PluginManager pm, NetServerHandler parent, MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, inetworkmanager, entityplayer);
        this.pm = pm;
        this.parent = parent;
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        this.pm.callEvent(new LanguageReceivedEvent(packet204localeandviewdistance.d()));
        parent.a(packet204localeandviewdistance);
    }
}
