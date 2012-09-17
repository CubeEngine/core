package de.cubeisland.cubeengine.core.bukkit;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.LocaleLanguage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitUtils
{
    private static final Field localeStringField;
    
    static
    {
        try
        {
            localeStringField = LocaleLanguage.class.getDeclaredField("d");
            localeStringField.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize the Bukkit-Language-Hack!");
        }
    }

    private BukkitUtils()
    {}
    
    public static String getLanguage(Player player)
    {
        if (player.getClass() == CraftPlayer.class)
        {
            try
            {
                return (String)localeStringField.get(((CraftPlayer)player).getHandle().getLocale());
            }
            catch (Exception e)
            {}
        }
        return null;
    }
    
    public static CommandMap getCommandMap(PluginManager pluginManager)
    {
        if (pluginManager.getClass() == SimplePluginManager.class)
        {
            try
            {
                for (Field field : pluginManager.getClass().getDeclaredFields())
                {
                    if (CommandMap.class.isAssignableFrom(field.getType()))
                    {
                        field.setAccessible(true);
                        return (CommandMap)field.get(pluginManager);
                    }
                }
            }
            catch (Exception e)
            {}
        }
        return null;
    }
    
    public static Map<String, Command> getKnownCommandMap(CommandMap commandMap)
    {
        if (commandMap.getClass() == SimpleCommandMap.class)
        {
            try
            {
                for (Field field : commandMap.getClass().getDeclaredFields())
                {
                    if (Map.class.isAssignableFrom(field.getType()))
                    {
                        field.setAccessible(true);
                        return (Map<String, Command>)field.get(commandMap);
                    }
                }
            }
            catch (Exception e)
            {}
        }
        return null;
    }
    
    public static void registerPacketHookInjector(Plugin plugin, PluginManager pluginManager)
    {
        if (!PacketHookInjector.injected)
        {
            pluginManager.registerEvents(PacketHookInjector.INSTANCE, plugin);
        }
    }
    
    private static class PacketHookInjector implements Listener
    {
        public static final PacketHookInjector INSTANCE = new PacketHookInjector();
        public static boolean injected = false;
        
        private PacketHookInjector()
        {}
        
        @EventHandler(priority = EventPriority.LOW)
        public void onPlayerJoin(PlayerJoinEvent event)
        {
            try
            {
                final Player player = event.getPlayer();
                EntityPlayer playerEntity = ((CraftPlayer)player).getHandle();
                MinecraftServer minecraftServer = ((CraftServer)player.getServer()).getServer();

                if (playerEntity.netServerHandler.getClass() != CubeEngineNetServerHandler.class)
                {
                    playerEntity.netServerHandler = new CubeEngineNetServerHandler(
                        player.getServer().getPluginManager(),
                        playerEntity.netServerHandler,
                        minecraftServer,
                        playerEntity.netServerHandler.networkManager,
                        playerEntity
                    );
                }
            }
            catch (Exception e)
            {}
        }
    }
}
