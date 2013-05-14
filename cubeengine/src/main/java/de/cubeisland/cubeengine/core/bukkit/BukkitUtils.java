/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.bukkit;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Filter;
import java.util.logging.Logger;

import net.minecraft.server.v1_5_R3.DedicatedPlayerList;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Item;
import net.minecraft.server.v1_5_R3.LocaleLanguage;
import net.minecraft.server.v1_5_R3.PlayerConnection;
import net.minecraft.server.v1_5_R3.RecipesFurnace;
import net.minecraft.server.v1_5_R3.ServerConnection;
import net.minecraft.server.v1_5_R3.TileEntityFurnace;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.v1_5_R3.CraftServer;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;

/**
 * This class contains various methods to access bukkit-related stuff.
 */
public class BukkitUtils
{
    private static Field localeStringField;
    private static Field playerConnectionListField = findFirstField(List.class, ServerConnection.class);

    static boolean init(BukkitCore core)
    {
        try
        {
            localeStringField = findFirstField(String.class, LocaleLanguage.class);
            localeStringField.setAccessible(true);

            playerConnectionListField = findFirstField(List.class, ServerConnection.class);
            playerConnectionListField.setAccessible(true);
        }
        catch (Exception e)
        {
            core.getLog().log(ERROR, "Failed to initialize the required hacks!", e);
            return false;
        }
        return true;
    }

    private BukkitUtils()
    {}

    public static boolean isCompatible(BukkitCore core)
    {
        String serverClassName = core.getServer().getClass().getName();
        return (serverClassName.startsWith("org.bukkit.craftbukkit.") && serverClassName.endsWith(".CraftServer"));
    }

    public static Locale getLocaleFromSender(I18n i18n, CommandSender sender)
    {
        if (sender instanceof de.cubeisland.cubeengine.core.command.CommandSender)
        {
            return ((de.cubeisland.cubeengine.core.command.CommandSender)sender).getLocale();
        }
        Locale locale = null;
        if (sender instanceof Player)
        {
            locale = getLocaleFromUser(i18n, (Player)sender);
        }
        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Returns the locale string of a player.
     *
     * @param player the Player instance
     * @return the locale string of the player
     */
    private static Locale getLocaleFromUser(I18n i18n, Player player)
    {
        if (player.getClass() == CraftPlayer.class)
        {
            try
            {
                final String localeString = (String)localeStringField.get(((CraftPlayer)player).getHandle().getLocale());
                final Language lang = i18n.getLanguage(I18n.stringToLocale(localeString));
                if (lang != null)
                {
                    return lang.getLocale();
                }
            }
            catch (Exception ignored)
            {}
        }
        return null;
    }

    static ConsoleReader getConsoleReader(final Server server)
    {
        return ((CraftServer)server).getServer().reader;
    }

    public static CommandMap getCommandMap(final Server server)
    {
        return ((CraftServer)server).getCommandMap();
    }

    private static Field findFirstField(Class type, Object o)
    {
        return findFirstField(type, o.getClass());
    }

    private static Field findFirstField(Class<?> type, Class clazz)
    {
        for (Field field : clazz.getDeclaredFields())
        {
            if (type.isAssignableFrom(field.getType()))
            {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    static SimpleCommandMap swapCommandMap(SimpleCommandMap commandMap)
    {
        assert commandMap != null: "The command map must not be null!";

        final Server server = Bukkit.getServer();
        final PluginManager pm = Bukkit.getPluginManager();

        Field serverField = findFirstField(CommandMap.class, server);
        Field pmField = findFirstField(CommandMap.class, pm);

        SimpleCommandMap oldMap = ((CraftServer)server).getCommandMap();
        if (serverField != null && pmField != null)
        {
            try
            {
                serverField.set(server, commandMap);
                pmField.set(pm, commandMap);
            }
            catch (Exception e)
            {
                CubeEngine.getLog().log(DEBUG, e.getLocalizedMessage(), e);
            }
        }
        return oldMap;
    }

    static void resetCommandMap()
    {
        SimpleCommandMap current = ((CraftServer)Bukkit.getServer()).getCommandMap();
        if (current instanceof CubeCommandMap)
        {
            CubeCommandMap cubeMap = (CubeCommandMap)current;
            swapCommandMap(current = new SimpleCommandMap(Bukkit.getServer()));

            Collection<Command> commands = cubeMap.getKnownCommands().values();

            for (Command command : commands)
            {
                command.unregister(cubeMap);
                if (command instanceof CubeCommand)
                {
                    continue;
                }
                String prefix = "";
                if (command instanceof PluginCommand)
                {
                    prefix = ((PluginCommand)command).getPlugin().getName();
                }
                else if (command instanceof BukkitCommand)
                {
                    prefix = "bukkit";
                }
                current.register(command.getLabel(), prefix, command);
            }
        }
    }

    private static Filter filter = null;
    private static CommandLogFilter commandFilter = null;

    static void disableCommandLogging()
    {
        if (commandFilter == null)
        {
            commandFilter = new CommandLogFilter();
        }
        Logger logger = Bukkit.getLogger();
        filter = logger.getFilter();
        logger.setFilter(commandFilter);
    }

    static void resetCommandLogging()
    {
        if (commandFilter != null)
        {
            Logger logger = Bukkit.getLogger();
            if (logger.getFilter() == commandFilter)
            {
                logger.setFilter(filter);
            }
            filter = null;
        }
    }

    private static PacketHookInjector hookInjector = null;
    /**
     * Registers the packet hook injector
     *
     * @param core the BukkitCore
     */
    static synchronized void registerPacketHookInjector(BukkitCore core)
    {
        if (hookInjector == null)
        {
            hookInjector = new PacketHookInjector();
            core.getServer().getPluginManager().registerEvents(hookInjector, core);

            for (Player player : Bukkit.getOnlinePlayers())
            {
                hookInjector.swap(player);
            }
        }
    }

    public static int getPing(Player onlinePlayer)
    {
        return ((CraftPlayer)onlinePlayer).getHandle().ping;
    }

    private static class PacketHookInjector implements Listener
    {
        public void shutdown()
        {
            HandlerList.unregisterAll(this);

            for (Player player : Bukkit.getOnlinePlayers())
            {
                resetPlayerNetServerHandler(player);
            }
        }

        /**
         * The event listener swaps the joining player's NetServerHandler
         * instance with a custom one including all the magic to make the new
         * NetServerHandler work.
         *
         * @param event the join event object
         */
        @EventHandler(priority = EventPriority.LOW)
        public void onPlayerJoin(PlayerJoinEvent event)
        {
            this.swap(event.getPlayer());
        }

        public void swap(final Player player)
        {
            final EntityPlayer entity = ((CraftPlayer)player).getHandle();

            swapPlayerNetServerHandler(entity, new CubePlayerConnection(player, entity));
        }
    }

    private static final Location helperLocation = new Location(null, 0, 0, 0);

    @SuppressWarnings("unchecked")
    private static void swapPlayerNetServerHandler(EntityPlayer player, PlayerConnection newHandler)
    {
        if (playerConnectionListField == null)
        {
            return;
        }
        PlayerConnection oldHandler = player.playerConnection;
        try
        {
            if (oldHandler.getClass() != newHandler.getClass())
            {
                Location loc = player.getBukkitEntity().getLocation(helperLocation);
                newHandler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

                ServerConnection sc = player.server.ae();
                ((List<PlayerConnection>)playerConnectionListField.get(sc)).remove(oldHandler);
                sc.a(newHandler);
                CubeEngine.getLog().log(DEBUG, "Replaced the NetServerHandler of player ''{0}''", player.getName());
                oldHandler.disconnected = true;
            }
        }
        catch (Exception e)
        {
            player.playerConnection = oldHandler;
            CubeEngine.getLog().log(DEBUG, e.getLocalizedMessage(), e);
        }
    }

    public static void resetPlayerNetServerHandler(Player player)
    {
        final EntityPlayer entity = ((CraftPlayer)player).getHandle();

        swapPlayerNetServerHandler(entity, new PlayerConnection(entity.server, entity.playerConnection.networkManager, entity));
    }

    public static boolean isInvulnerable(Player player)
    {
        if (player != null)
        {
            if (player instanceof User)
            {
                player = ((User)player).getOfflinePlayer().getPlayer();
            }
            if (player != null && player instanceof CraftPlayer)
            {
                return ((CraftPlayer)player).getHandle().abilities.isInvulnerable;
            }
        }
        return false;
    }

    public static void setInvulnerable(Player player, boolean state)
    {
        if (player != null && player instanceof User)
        {
            player = ((User)player).getOfflinePlayer().getPlayer();
        }
        if (player != null && player instanceof CraftPlayer)
        {
            ((CraftPlayer)player).getHandle().abilities.isInvulnerable = state;
            ((CraftPlayer)player).getHandle().updateAbilities();
        }
    }

    public static synchronized void cleanup()
    {
        if (hookInjector != null)
        {
            hookInjector.shutdown();
            hookInjector = null;
        }

        resetCommandMap();
        resetCommandLogging();
    }

    public static void setOnlineMode(boolean mode)
    {
        ((CraftServer)Bukkit.getServer()).getServer().setOnlineMode(mode);
        saveServerProperties();
    }

    public static void saveServerProperties()
    {
        ((CraftServer)Bukkit.getServer()).getServer().getPropertyManager().savePropertiesFile();
    }

    public static void wipeWhiteliste()
    {
        DedicatedPlayerList playerList = ((CraftServer)Bukkit.getServer()).getHandle();
        playerList.getWhitelisted().clear();
        // The method to write the whitelist (DedicatedPlayerList.w()) is private,
        // however removing an entry triggers the write :)
        playerList.removeWhitelist("");
    }

    /**
     * Returns true if given material is allowed to be placed in the top brewingstand slot
     *
     * @param material
     * @return
     */
    public static boolean canBePlacedInBrewingstand(Material material)
    {
        return Item.byId[material.getId()].w();
    }

    public static boolean isFuel(ItemStack item)
    {
        // Create an NMS item stack
        net.minecraft.server.v1_5_R3.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // Use the NMS TileEntityFurnace to check if the item being clicked is a fuel
        return TileEntityFurnace.isFuel(nmss);
    }

    public static boolean isSmeltable(ItemStack item)
    {
        net.minecraft.server.v1_5_R3.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // If the result of that item being cooked is null, it is not cookable
        return RecipesFurnace.getInstance().getResult(nmss.getItem().id) != null;
    }

    static void setSignalHandlers(final BukkitCore core)
    {
        try
        {
            Class.forName("sun.misc.Signal");

            Signal.handle(new Signal("INT"), new SignalHandler()
            {
                private long lastReceived = 0;

                @Override
                public void handle(Signal signal)
                {
                    if (this.lastReceived == -1)
                    {
                        return;
                    }
                    final long time = System.currentTimeMillis();
                    if (time - this.lastReceived <= 5000)
                    {
                        core.getLog().log(NOTICE, "Shutting down the server now!");
                        core.getServer().shutdown();
                        this.lastReceived = -1;
                    }
                    else
                    {
                        this.lastReceived = time;
                        core.getLog().log(NOTICE, "You can't copy content from the console using CTRL-C!");
                        core.getLog().log(NOTICE, "If you really want shutdown the server use the stop command or press CTRL-C again within 5 seconds!");
                    }
                }
            });

            try
            {
                Signal.handle(new Signal("HUP"), new SignalHandler() {
                    private volatile boolean reloading = false;

                    @Override
                    public void handle(Signal signal)
                    {
                        if (!this.reloading)
                        {
                            this.reloading = true;
                            core.getLog().log(NOTICE, "Reloading the server!");
                            core.getServer().reload();
                            core.getLog().log(NOTICE, "Done reloading the server!");
                            this.reloading = false;
                        }
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
                core.getLog().log(NOTICE, "You're OS does not support the HUP signal! This can be ignored.");
            }

            Signal.handle(new Signal("TERM"), new SignalHandler() {
                private volatile boolean shuttingDown = false;

                @Override
                public void handle(Signal signal)
                {
                    if (!this.shuttingDown)
                    {
                        this.shuttingDown = true;
                        core.getLog().log(NOTICE, "Shutting down the server!");
                        core.getServer().shutdown();
                    }
                }
            });
        }
        catch (ClassNotFoundException ignored)
        {}
    }
}
