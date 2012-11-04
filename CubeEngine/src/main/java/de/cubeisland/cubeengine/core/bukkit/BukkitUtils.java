package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.LocaleLanguage;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.ServerConnection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

/**
 * This class contains various methods to access bukkit-related stuff.
 */
public class BukkitUtils
{
    private static final Field localeStringField;
    private static Field nshListField = null;

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
        try
        {
            nshListField = ServerConnection.class.getDeclaredField("d");
            nshListField.setAccessible(true);
        }
        catch (Exception e)
        {
        }
    }

    private BukkitUtils()
    {
    }

    /**
     * Returns the locale string of a player.
     *
     * @param player the Player instance
     * @return the locale string of the player
     */
    public static String getLanguage(Player player)
    {
        if (player.getClass() == CraftPlayer.class)
        {
            try
            {
                return (String)localeStringField.get(((CraftPlayer)player).getHandle().getLocale());
            }
            catch (Exception e)
            {
            }
        }
        return null;
    }

    private static Field findCommandMapField(Object o)
    {
        for (Field field : o.getClass().getDeclaredFields())
        {
            if (CommandMap.class.isAssignableFrom(field.getType()))
            {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static void swapCommandMap(Server server, PluginManager pm, CommandMap commandMap)
    {
        Validate.notNull(commandMap, "The command map must not be null!");

        if (pm.getClass() == SimplePluginManager.class && server.getClass() == CraftServer.class)
        {
            Field serverField = findCommandMapField(server);
            Field pmField = findCommandMapField(pm);
            if (serverField != null && pmField != null)
            {
                try
                {
                    serverField.set(server, commandMap);
                    pmField.set(pm, commandMap);
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    /**
     * Registers the packet hook injector
     *
     * @param plugin a Plugin to register the injector with
     */
    public static void registerPacketHookInjector(Plugin plugin)
    {
        if (!PacketHookInjector.injected)
        {
            plugin.getServer().getPluginManager().registerEvents(PacketHookInjector.INSTANCE, plugin);
        }
    }

    private static class PacketHookInjector implements Listener
    {
        public static final PacketHookInjector INSTANCE = new PacketHookInjector();
        public static boolean injected = false;

        private PacketHookInjector()
        {
        }

        /**
         * The event listener swaps the joining player's NetServerHandler
         * instance with a custom one including all the magic to make the new
         * NetServerHandler work.
         *
         * @param event
         */
        @EventHandler(priority = EventPriority.LOW)
        public void onPlayerJoin(PlayerJoinEvent event)
        {
            if (nshListField == null)
            {
                return;
            }
            final Player player = event.getPlayer();
            EntityPlayer playerEntity = ((CraftPlayer)player).getHandle();
            NetServerHandler oldHandler = playerEntity.netServerHandler;
            try
            {

                if (oldHandler.getClass() != CubeEngineNetServerHandler.class)
                {
                    CubeEngineNetServerHandler handler = new CubeEngineNetServerHandler(playerEntity);

                    Location loc = player.getLocation();
                    handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

                    ServerConnection sc = playerEntity.server.ae();
                    ((List<NetServerHandler>)nshListField.get(sc)).remove(oldHandler);
                    sc.a(handler);
                    System.out.print("Replaced the NetServerHandler of player '" + player.getName() + "'"); // TODO log this as debug or smt like this
                    oldHandler.disconnected = true;
                }
            }
            catch (Exception e)
            {
                playerEntity.netServerHandler = oldHandler;
                e.printStackTrace(System.err);
            }
        }
    }

    public static void reloadHelpMap()
    {
        SimpleHelpMap helpMap = (SimpleHelpMap)Bukkit.getHelpMap();

        helpMap.clear();
        helpMap.initializeGeneralTopics();
        helpMap.initializeCommands();
    }

    public static boolean renameItemStack(ItemStack itemStack, String name)
    {
        name = ChatFormat.parseFormats(name);
        if (itemStack == null || itemStack.getType().equals(Material.AIR))
        {
            return false;
        }
        CraftItemStack cis = ((CraftItemStack)itemStack);
        NBTTagCompound tag = cis.getHandle().getTag();
        if (tag == null)
        {
            cis.getHandle().setTag(tag = new NBTTagCompound());
        }
        if (tag.hasKey("display") == false)
        {
            tag.setCompound("display", new NBTTagCompound());
        }
        NBTTagCompound display = tag.getCompound("display");
        if (name == null || name.equals(""))
        {
            display.remove("Name");
            return true;
        }
        display.setString("Name", name);
        return true;
    }

    public static String getItemStackName(ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return null;
        }
        CraftItemStack cis = ((CraftItemStack)itemStack);
        NBTTagCompound tag = cis.getHandle().getTag();
        if (tag == null)
        {
            cis.getHandle().setTag(tag = new NBTTagCompound());
        }
        if (tag.hasKey("display") == false)
        {
            return null;
        }
        String name = tag.getCompound("display").getString("Name");
        if (name.equals(""))
        {
            return null;
        }
        return name;
    }

    public static CraftItemStack changeHead(ItemStack head, String name)
    {
        if (head.getType().equals(Material.SKULL_ITEM))
        {
            head.setDurability((short)3);
            CraftItemStack newHead = new CraftItemStack(head);
            NBTTagCompound newHeadData = new NBTTagCompound();
            newHeadData.setString("SkullOwner", name);
            newHead.getHandle().tag = newHeadData;
            return newHead;
        }
        else
        {
            return null;
        }
    }
}