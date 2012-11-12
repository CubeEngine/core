package de.cubeisland.cubeengine.guests.prevention;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.guests.Guests;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class manages the Prevention's.
 */
public class PreventionManager
{
    private final Guests guests;
    private final PermissionManager pm;
    private final EventManager em;
    private final THashMap<String, Prevention> preventions;
    private final THashMap<String, Punishment> punishments;
    
    public PreventionManager(Guests guests)
    {
        this.guests = guests;
        this.pm = guests.getCore().getPermissionManager();
        this.em = guests.getCore().getEventManager();
        this.preventions = new THashMap<String, Prevention>();
        this.punishments = new THashMap<String, Punishment>();
    }

    /**
     * Returns the named prevention or null if not available
     *
     * @param name the name of the prevention
     * @return the prevention or null
     */
    public Prevention getPrevention(String name)
    {
        return this.preventions.get(name);
    }

    /**
     * Returns all registered preventions
     *
     * @return a collection of all preventions
     */
    public Collection<Prevention> getPreventions()
    {
        return new ArrayList<Prevention>(this.preventions.values());
    }

    /**
     * Registeres a prevention
     *
     * @param prevention the prevention to register
     * @return fluent interface
     */
    public PreventionManager registerPrevention(Prevention prevention)
    {
        if (prevention == null)
        {
            throw new IllegalArgumentException("prevention must not be null!");
        }
        if (!prevention.isLoaded())
        {
            prevention.load();
        }
        if (!this.preventions.containsValue(prevention))
        {
            this.preventions.put(prevention.getName(), prevention);
            this.pm.registerPermission(this.guests, prevention.getPermission(), PermissionDefault.OP);
        }
        
        return this;
    }

    /**
     * Unregisteres a prevention if registered
     *
     * @param name the name of the prevention
     * @return fluent interface
     */
    public PreventionManager unregisterPrevention(String name)
    {
        this.preventions.remove(name);
        return this;
    }

    public boolean enablePrevention(final String name)
    {
        final Prevention prevention = this.preventions.get(name);
        if (prevention != null)
        {
            return this.enablePrevention(prevention);
        }
        return false;
    }

    /**
     * Enables the named prevention if registered
     * 
     * @param prevention the preventions name
     * @param server an Server instance
     * @param config the prevention's configuration
     * @return true if the intialization was successful
     */
    public boolean enablePrevention(final Prevention prevention)
    {
        if (prevention != null && !prevention.isEnabled())
        {
            try
            {
                prevention.enable();
                this.em.registerListener(this.guests, prevention);

                prevention.setEnabled(true);
                return true;
            }
            catch (Throwable t)
            {
                this.guests.getLogger().log(Level.SEVERE, "Failed to enable the prevention '" + prevention.getName() + "'...", t);
            }
        }
        return false;
    }

    /**
     * This method loads all registered preventions based on the given ConfigurationSection and the default configuraiton.
     * The given ConfigurationSection should have a key "preventions" on top level, otherwise this will fail
     * 
     * @param config the configuration
     * @return fluent interface
     */
    public PreventionManager enablePreventions()
    {
        for (Prevention prevention : this.preventions.values())
        {
            if (prevention.getConfig().getBoolean("enable"))
            {
                this.enablePrevention(prevention);
            }
        }

        return this;
    }

    /**
     * Disables the named prevention
     *
     * @param name name of the prevention
     * @return fluent interface
     */
    public PreventionManager disablePrevention(String name)
    {
        final Prevention prevention = this.preventions.get(name);
        if (prevention != null)
        {
            this.enablePrevention(prevention);
        }
        return this;
    }

    /**
     * Disables the named prevention
     *
     * @param prevention name of the prevention
     * @return fluent interface
     */
    public PreventionManager disablePrevention(Prevention prevention)
    {
        if (prevention != null && prevention.isEnabled())
        {
            prevention.setEnabled(false);
            this.em.unregisterListener(this.guests, prevention);
            try
            {
                prevention.disable();
            }
            catch (Throwable t)
            {
                this.guests.getLogger().log(Level.SEVERE, "Failed to disable the prevention '" + prevention.getName() + "'...", t);
            }
        }
        return this;
    }

    /**
     * Disables all preventions
     *
     * @return fluent interface
     */
    public PreventionManager disablePreventions()
    {
        for (Prevention prevention : this.preventions.values())
        {
            this.disablePrevention(prevention);
        }
        return this;
    }

    /**
     * Registeres a punishment
     *
     * @param punishment the punishment
     * @return fluent interface
     */
    public PreventionManager registerPunishment(Punishment punishment)
    {
        if (punishment == null)
        {
            throw new IllegalArgumentException("The punishment must not be null!");
        }
        this.punishments.put(punishment.getName(), punishment);
        return this;
    }

    /**
     * Unregisteres a punishment
     *
     * @param punishment the punishment to unregister
     * @return fluent interface
     */
    public PreventionManager unregisterPunishment(Punishment punishment)
    {
        if (punishment == null)
        {
            throw new IllegalArgumentException("The punishment must not be null!");
        }
        return this.unregisterPunishment(punishment.getName());
    }

    /**
     * Unregisteres a punishment by its name
     *
     * @param punishment the name of the punishment to unregister
     * @return fluent interface
     */
    public PreventionManager unregisterPunishment(String name)
    {
        this.punishments.remove(name);
        return this;
    }

    /**
     * Returns a punishment by its name
     *
     * @param name the name of the punishment
     * @return the punishment
     */
    public Punishment getPunishment(String name)
    {
        return this.punishments.get(name);
    }
}
