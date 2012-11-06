package de.cubeisland.cubeengine.guests.prevention;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.guests.Guests;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectObjectProcedure;
import java.util.Locale;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

/**
 * This class represents a prevention
 *
 * @author Phillip Schichtel
 */
public abstract class Prevention implements Listener
{
    private static final PunishmentProcedure PUNISHMENT_PROCEDURE = new PunishmentProcedure();
    private static final String PERMISSION_BASE = "cubeengine.guests.prevention.";

    private final String name;
    private final String permission;
    private final Guests guests;
    private final boolean allowPunishing;

    private boolean loaded;
    private String message;
    private int throttleDelay;
    private boolean enabled;
    private boolean enableByDefault;
    private PreventionConfiguration config;
    private TObjectLongMap<Player> messageThrottleTimestamps;

    private boolean enablePunishing;
    private TIntObjectMap<THashMap<Punishment, ConfigurationSection>> violationPunishmentMap;
    private TObjectIntMap<Player> playerViolationMap;
    private TObjectLongMap<Player> punishThrottleTimestamps;
    private int highestPunishmentViolation;

    /**
     * Initializes the prevention with its name, the corresponding plugin and
     * allowed punishing.
     *
     * @param name the name of the prevention
     * @param guests the plugin
     */
    public Prevention(final String name, final Guests guests)
    {
        this(name, guests, true);
    }

    /**
     * Initializes the prevention with its name, the corresponding plugin and
     * whether to allow punishing.
     *
     * @param name the name of the prevention
     * @param guests the plugin
     * @param allowPunishing whether to allow punishing
     */
    public Prevention(final String name, final Guests guests, final boolean allowPunishing)
    {
        this.name = name;
        this.permission = PERMISSION_BASE + name.toLowerCase(Locale.ENGLISH);
        this.guests = guests;
        this.allowPunishing = allowPunishing;

        this.loaded = false;
        this.message = null;
        this.throttleDelay = 0;
        this.enabled = false;
        this.enableByDefault = false;
        this.config = null;
        this.highestPunishmentViolation = 0;
        this.enablePunishing = false;
    }

    /**
     * Returns the configuration of this prevention
     *
     * @return the config
     */
    public final PreventionConfiguration getConfig()
    {
        return this.config;
    }
    
    public final Guests getModule()
    {
        return this.guests;
    }

    /**
     * Resets the configuration of this prevention
     *
     * @return true on success
     */
    public final boolean resetConfig()
    {
        this.config = PreventionConfiguration.get(this.guests.getPreventionsFolder(), this, false);
        return saveConfig();
    }

    /**
     * Reloads the prevention's configuration
     *
     * @return true on success
     */
    public final boolean reloadConfig()
    {
        try
        {
            this.config.load();
            return true;
        }
        catch (Throwable e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     * Saves the configuration of the prevention
     *
     * @return true on success
     */
    public final boolean saveConfig()
    {
        try
        {
            this.config.save();
            return true;
        }
        catch (Throwable e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     * Generates the default configuration of this prevention.
     * This method should be overridden for custom configs.
     *
     * @return the default config
     */
    public Configuration getDefaultConfig()
    {
        // TODO port to our configuration API
        Configuration defaultConfig = new MemoryConfiguration();

//        defaultConfig.set("enable", this.enableByDefault);
//        // TODO perperly solve the color code problem
//        defaultConfig.set("message", this.guests.getTranslation().translate("message_" + this.name));
//        if (this.throttleDelay > 0)
//        {
//            defaultConfig.set("throttleDelay", getThrottleDelay());
//        }
//
//        if (this.allowPunishing)
//        {
//            defaultConfig.set("punish", this.enablePunishing);
//            defaultConfig.set("punishments.3.slap.damage", 4);
//            defaultConfig.set("punishments.5.kick.reason", this.guests.getTranslation().translate("defaultKickReason"));
//        }

        return defaultConfig;
    }

    /**
     * Returns a list of strings for the config header
     *
     * @return the lines
     */
    public String getConfigHeader()
    {
        return "This is the configuration file of the " + this.name + " configuration.\n";
    }

    /**
     * Loads the configuration of the prevention.
     * this method should be called right after the object got constructed.
     */
    public void load()
    {
        this.loaded = true;
        this.config = PreventionConfiguration.get(this.guests.getPreventionsFolder(), this);
        if (this.allowPunishing && this.config.get("punishments", null) != null)
        {
            this.config.getDefaultSection().set("punishments", null);
        }
        this.config.safeSave();
    }

    /**
     * Enables the prevention.
     * This method should be overridden for custom configs.
     *
     * @param server an Server instance
     * @param config the configuration of this prevention
     */
    public void enable()
    {
        this.messageThrottleTimestamps = new TObjectLongHashMap<Player>();
        this.throttleDelay = config.getInt("throttleDelay", 0) * 1000;
        this.setMessage(config.getString("message"));

        if (this.allowPunishing)
        {
            this.punishThrottleTimestamps = new TObjectLongHashMap<Player>();
            this.violationPunishmentMap = new TIntObjectHashMap<THashMap<Punishment, ConfigurationSection>>();
            this.playerViolationMap = new TObjectIntHashMap<Player>();

            this.enablePunishing = config.getBoolean("punish", this.enablePunishing);

            if (this.enablePunishing)
            {
                ConfigurationSection punishmentsSection = config.getConfigurationSection("punishments");
                if (punishmentsSection != null)
                {
                    int violation;
                    THashMap<Punishment, ConfigurationSection> punishments;
                    ConfigurationSection violationSection;
                    ConfigurationSection punishmentSection;
                    final PreventionManager pm = this.guests.getPreventionManager();
                    Punishment punishment;

                    for (String violationString : punishmentsSection.getKeys(false))
                    {
                        try
                        {
                            violation = Integer.parseInt(violationString);
                            punishments = this.violationPunishmentMap.get(violation);
                            violationSection = punishmentsSection.getConfigurationSection(violationString);
                            if (violationSection != null)
                            {
                                for (String punishmentName : violationSection.getKeys(false))
                                {
                                    punishment = pm.getPunishment(punishmentName);
                                    if (punishment != null)
                                    {
                                        punishmentSection = violationSection.getConfigurationSection(punishmentName);
                                        if (punishmentSection != null)
                                        {
                                            if (punishments == null)
                                            {
                                                punishments = new THashMap<Punishment, ConfigurationSection>();
                                                this.violationPunishmentMap.put(violation, punishments);
                                                this.highestPunishmentViolation = Math.max(this.highestPunishmentViolation, violation);
                                            }
                                            punishments.put(punishment, punishmentSection);
                                        }
                                    }
                                }
                            }
                        }
                        catch (NumberFormatException e)
                        {}
                    }
                }
            }
        }
    }

    /**
     * Disables the prevention.
     * This method should be overridden to cleanup customized preventions
     */
    public void disable()
    {
        this.messageThrottleTimestamps.clear();
        this.messageThrottleTimestamps = null;

        if (this.allowPunishing)
        {
            this.playerViolationMap.clear();
            this.playerViolationMap = null;

            this.punishThrottleTimestamps.clear();
            this.punishThrottleTimestamps = null;
            
            this.violationPunishmentMap.clear();
            this.violationPunishmentMap = null;
        }
    }

    /**
     * Sets whether to enable this prevention by default
     *
     * @param enable true to enable it by default
     */
    public final void setEnableByDefault(boolean enable)
    {
        this.enableByDefault = enable;
    }

    /**
     * Returns whether this prevention will be enabled by default
     *
     * @return true if it will be enabled by default
     */
    public final boolean getEnableByDefault()
    {
        return this.enableByDefault;
    }

    /**
     * Returns whether this prevention is already loaded
     *
     * @return true if loaded
     */
    public final boolean isLoaded()
    {
        return this.loaded;
    }

    /**
     * Returns whether this prevention is enabled.
     *
     * @return true if this prevention is enabled
     */
    public final boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * Sets the enabled state of this prevention
     *
     * @param enable
     */
    public final void setEnabled(boolean enable)
    {
        this.enabled = enable;
    }

    /**
     * Returns the prevention's name
     * 
     * @return the name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the prevention's permission
     *
     * @return the permission
     */
    public final String getPermission()
    {
        return this.permission;
    }

    /**
     * Returns the message this prevention will send to players
     *
     * @return the message
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * Sets the message this prevention will send to players
     *
     * @param message the new message
     */
    public void setMessage(String message)
    {
        this.message = parseMessage(message);
    }

    /**
     * Returns the delay this preventions uses for throttled messages
     *
     * @return the delay
     */
    public int getThrottleDelay()
    {
        return this.throttleDelay / 1000;
    }

    /**
     * Sets the delay this preventions uses for throttled messages
     *
     * @return the delay
     */
    public void setThrottleDelay(int delay)
    {
        this.throttleDelay = delay * 1000;
    }

    /**
     * Sets whether this prevention enables punishing
     * 
     * @param enable true to enable it
     */
    public void setEnablePunishing(boolean enable)
    {
        this.enablePunishing = enable;
    }

    /**
     * Returns whether this prevention enables punishing
     *
     * @return true if it enables it
     */
    public boolean getEnablePunishing()
    {
        return this.enablePunishing;
    }

    /**
     * Checks whether a player can pass a prevention
     *
     * @param player the player
     * @return true if the player can pass the prevention
     */
    public boolean can(final Player player)
    {
        return player.hasPermission(this.permission);
    }

    /**
     * Does the same as sendMessage(Player), except that this method throttles the messages sending
     * 
     * @param player hte player to send to
     */
    public void sendMessage(final Player player)
    {
        if (this.message == null)
        {
            return;
        }
        if (this.throttleDelay > 0)
        {
            final long next = this.messageThrottleTimestamps.get(player);
            final long current = System.currentTimeMillis();
            if (next < current)
            {
                this.messageThrottleTimestamps.put(player, current + this.throttleDelay);
            }
            else
            {
                return;
            }
        }
        
        player.sendMessage(this.message);
    }

    /**
     * This method combines can(Player) and sendMessage(Player),
     * by first checking whether player can pass the prevention and if not,
     * the given cancellable event gets cancelled and the message is sent to the
     * player.
     *
     * @param event a cancellable event
     * @param player the player
     * @return true if the action was prevented
     */
    public boolean prevent(final Cancellable event, final Player player)
    {
        if (!this.can(player))
        {
            event.setCancelled(true);
            sendMessage(player);
            punish(player);
            return true;
        }
        return false;
    }

    public synchronized void punish(final Player player)
    {
        if (!guests.allowPunishments() || !this.allowPunishing || !this.enablePunishing)
        {
            return;
        }
        Integer violations = this.playerViolationMap.get(player);
        if (violations == null || violations >= this.highestPunishmentViolation)
        {
            violations = 0;
        }
        this.playerViolationMap.put(player, ++violations);

        THashMap<Punishment, ConfigurationSection> punishments = this.violationPunishmentMap.get(violations);
        if (punishments == null)
        {
            return;
        }

        if (this.throttleDelay > 0)
        {
            final long next = this.messageThrottleTimestamps.get(player);
            final long current = System.currentTimeMillis();
            if (next < current)
            {
                this.messageThrottleTimestamps.put(player, current + this.throttleDelay);
            }
            else
            {
                return;
            }
        }
        
        PUNISHMENT_PROCEDURE.player = player;
        punishments.forEachEntry(PUNISHMENT_PROCEDURE);
        PUNISHMENT_PROCEDURE.player = null;
    }

    /**
     * Parses a message
     *
     * @param message the message to parse
     * @return null if message is null or empty, otherwise the parsed message
     */
    public static String parseMessage(final String message)
    {
        if (message == null)
        {
            return null;
        }
        if (message.length() == 0)
        {
            return null;
        }
        return ChatFormat.parseFormats(message);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "{name=" + this.name + ", permission=" + this.permission.toString() + ", plugin=" + this.guests.toString() + "}";
    }

    private static final class PunishmentProcedure implements TObjectObjectProcedure<Punishment, ConfigurationSection>
    {
        public Player player;

        public boolean execute(Punishment punishment, ConfigurationSection config)
        {
            System.err.println("Punishment: " + punishment.getName());
            punishment.punish(this.player, config);
            return true;
        }
    }
}

