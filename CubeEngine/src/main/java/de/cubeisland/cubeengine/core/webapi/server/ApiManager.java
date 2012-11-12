package de.cubeisland.cubeengine.core.webapi.server;

import de.cubeisland.cubeengine.core.webapi.ApibukkitController;
import de.cubeisland.cubeengine.core.webapi.server.serializer.JsonSerializer;
import de.cubeisland.cubeengine.core.webapi.server.serializer.RawSerializer;
import de.cubeisland.cubeengine.core.webapi.server.serializer.XmlSerializer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;

/**
 * The class manages all the registered API controllers, serializers,
 * white-/blacklisting and action disabling.
 */
public final class ApiManager
{
    private static ApiManager instance = null;
    private final Map<String, ApiController> controllers;
    private final Map<String, ApiResponseSerializer> responseSerializers;
    private final Map<String, Collection<String>> disabledActions;
    private boolean whitelistEnabled;
    private final Collection<String> whitelist;
    private boolean blacklistEnabled;
    private final Collection<String> blacklist;
    private ApiResponseSerializer defaultSerializer;

    private ApiManager()
    {
        this.controllers = new ConcurrentHashMap<String, ApiController>();
        this.responseSerializers = new ConcurrentHashMap<String, ApiResponseSerializer>();
        this.disabledActions = new ConcurrentHashMap<String, Collection<String>>();
        this.whitelist = Collections.synchronizedList(new ArrayList<String>());
        this.blacklist = Collections.synchronizedList(new ArrayList<String>());

        this.whitelistEnabled = false;
        this.blacklistEnabled = false;

        this.defaultSerializer = new JsonSerializer();

        this.registerSerializer(new JsonSerializer())
            .registerSerializer(new XmlSerializer())
            .registerSerializer(new RawSerializer())
            .registerSerializer(this.defaultSerializer);
    }

    /**
     * Returns the singleton instance of the ApiManager
     *
     * @return the manager instance
     */
    public static ApiManager getInstance()
    {
        if (instance == null)
        {
            instance = new ApiManager();
        }
        return instance;
    }

    /**
     * Checks whether there is a controller with the given name
     *
     * @param controller the name of the controller return true if it exists
     */
    public boolean isControllerRegistered(String controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        return this.controllers.containsKey(controller.toLowerCase());
    }

    /**
     * checks whether the given controller is registered
     *
     * @param controller the controller instance
     * @return true if it is registered
     */
    public boolean isControllerRegistered(ApiController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        return this.isControllerRegistered(controller.getName());
    }

    /**
     * Registeres a controller
     *
     * @param controller the controller instance
     * @return fluent interface
     */
    public ApiManager registerController(ApiController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller must not be null!");
        }
        if ("apibukkit".equals(controller.getName()) && controller.getClass() != ApibukkitController.class)
        {
            throw new IllegalArgumentException("apibukkit can only be registered with an instance of " + ApibukkitController.class.getName());
        }
        this.controllers.put(controller.getName(), controller);

        return this;
    }

    /**
     * Unregisteres a controller by name
     *
     * @param controller the name of the controller
     * @return fluent interface
     */
    public ApiManager unregisterController(String controller)
    {
        this.controllers.remove(controller);
        return this;
    }

    /**
     * Unregisteres a controller
     *
     * @param controller the controller instance
     * @return fluent interface
     */
    public ApiManager unregisterController(ApiController controller)
    {
        if (controller != null)
        {
            this.unregisterController(controller.getName());
        }
        return this;
    }

    /**
     * Unregisteres all controllers of the given plugin
     *
     * @param plugin the plugin instance that registeres the controllers
     * @return fluent interface
     */
    public ApiManager unregisterControllers(Plugin plugin)
    {
        if (plugin != null)
        {
            for (ApiController controller : this.getControllers(plugin))
            {
                this.unregisterController(controller);
            }
        }
        return this;
    }

    /**
     * Gets a controller by name
     *
     * @param name the name of the controller
     * @return the registered controller or null if it does not exist
     */
    public ApiController getController(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.controllers.get(name.toLowerCase());
    }

    /**
     * Gets all controllers of a plugin
     *
     * @param plugin the instance of the plugin which registered the controllers
     * @return a collection of all the controllers
     */
    public Collection<ApiController> getControllers(Plugin plugin)
    {
        if (plugin == null)
        {
            throw new IllegalArgumentException("plugin must not be null!");
        }
        Collection<ApiController> controllersOfPlugin = new ArrayList<ApiController>();
        for (ApiController controller : this.getControllers())
        {
            if (controller.getPlugin().equals(plugin))
            {
                controllersOfPlugin.add(controller);
            }
        }
        return controllersOfPlugin;
    }

    /**
     * Returns all controllers
     *
     * @return a collection of all controllers
     */
    public Collection<ApiController> getControllers()
    {
        return this.controllers.values();
    }

    /**
     * Returns a copy of the name-controller map
     *
     * return the name-controller map
     */
    public Map<String, ApiController> getControllerMap()
    {
        return new HashMap<String, ApiController>(this.controllers);
    }

    /**
     * Clears the controllers
     *
     * @return fluent interface
     */
    public ApiManager clearControllers()
    {
        this.controllers.clear();
        return this;
    }

    /**
     * Checks whether there is a serializer with the given name
     *
     * @param name the name of the serializer
     * @return true if there is one
     */
    public boolean isSerializerRegistered(String name)
    {
        if (name == null)
        {
            return false;
        }
        return this.responseSerializers.containsKey(name.toLowerCase());
    }

    /**
     * Registeres a serializer
     *
     * @param serializer the instance of the serializer
     * @return fluent interface
     */
    public ApiManager registerSerializer(ApiResponseSerializer serializer)
    {
        if (serializer == null)
        {
            throw new IllegalArgumentException("serializer must not be null!");
        }
        if (serializer.getName() == null)
        {
            throw new IllegalArgumentException("The serializer has no name!");
        }

        this.responseSerializers.put(serializer.getName().toLowerCase(), serializer);
        return this;
    }

    /**
     * Unregisteres a serializer by name
     *
     * @param name the name of the serializer
     * @return fluent interface
     */
    public ApiManager unregisterSerializer(String name)
    {
        if (name != null)
        {
            this.responseSerializers.remove(name.toLowerCase());
        }
        return this;
    }

    /**
     * Clears al serializers
     *
     * @return fluent interface
     */
    public ApiManager clearSerializers()
    {
        this.responseSerializers.clear();
        return this;
    }

    /**
     * Gets a serializer by name
     *
     * @param name the name of the serializer
     * @return the serializer or null if it does not exist
     */
    public ApiResponseSerializer getSerializer(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.responseSerializers.get(name);
    }

    /**
     * Returns the default serializer
     *
     * @return the serializer
     */
    public ApiResponseSerializer getDefaultSerializer()
    {
        return this.defaultSerializer;
    }

    /**
     * Sets the default serializer
     *
     * @return serializer the instance if the serializer
     * @return fluent interface
     */
    public ApiManager setDefaultSerializer(ApiResponseSerializer serializer)
    {
        if (serializer == null)
        {
            throw new IllegalArgumentException("serializer must not be null!");
        }
        this.defaultSerializer = serializer;
        return this;
    }

    /**
     * Returns whether whitelisting is enabled
     *
     * @return true if enabled
     */
    public boolean isWhitelistEnabled()
    {
        return this.whitelistEnabled;
    }

    /**
     * Sets the enabled state of the whitelisting
     *
     * @param state true to enable, false to disable
     * @return fluent interface
     */
    public ApiManager setWhitelistEnabled(boolean state)
    {
        this.whitelistEnabled = state;
        return this;
    }

    /**
     * Sets the whitelist
     *
     * @param whitelist the whitelist as a string collection
     * @return fluent interface
     */
    public ApiManager setWhitelist(Collection<String> whitelist)
    {
        this.whitelist.clear();
        this.whitelist.addAll(whitelist);
        return this;
    }

    /**
     * Checks whether an InetSocketAddress is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(InetSocketAddress ip)
    {
        return this.isWhitelisted(ip.getAddress());
    }

    /**
     * Checks whether an InetAddress is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(InetAddress ip)
    {
        return this.isWhitelisted(ip.getHostAddress());
    }

    /**
     * Checks whether an string representation of an IP is whitelisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isWhitelisted(String ip)
    {
        if (this.whitelistEnabled)
        {
            return this.whitelist.contains(ip);
        }
        else
        {
            return true;
        }
    }

    /**
     * Sets the enabled state of the blacklisting
     *
     * @param state true to enable, false to disable
     * @return fluent interface
     */
    public ApiManager setBlacklistEnabled(boolean state)
    {
        this.blacklistEnabled = state;
        return this;
    }

    /**
     * Returns whether blacklisting is enabled
     *
     * @return true if it is
     */
    public boolean isBlacklistEnabled()
    {
        return this.blacklistEnabled;
    }

    /**
     * Sets the blacklist
     *
     * @param blacklist the blacklist as a string collection
     * @return fluent interface
     */
    public ApiManager setBlacklist(Collection<String> blacklist)
    {
        this.blacklist.clear();
        this.blacklist.addAll(blacklist);
        return this;
    }

    /**
     * Checks whether an InetSocketAddress is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(InetSocketAddress ip)
    {
        return this.isBlacklisted(ip.getAddress());
    }

    /**
     * Checks whether an InetAddress is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(InetAddress ip)
    {
        return this.isBlacklisted(ip.getHostAddress());
    }

    /**
     * Checks whether a string representation of an IP is blacklisted
     *
     * @param ip the IP
     * @return true if it is
     */
    public boolean isBlacklisted(String ip)
    {
        if (this.blacklistEnabled)
        {
            return this.blacklist.contains(ip);
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets the disabled actions
     *
     * @param disabledActions the disabled actions as a controller-actions map
     * @return fluent interface
     */
    public ApiManager setDisabledActions(Map<String, Collection<String>> disabledActions)
    {
        this.disabledActions.clear();
        this.disabledActions.putAll(disabledActions);
        return this;
    }

    /**
     * Checks whether the given action of the given controller is disabled
     *
     * @param controller the name of the controller
     * @param action     the name of the action
     * @return true if it is
     */
    public boolean isActionDisabled(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions == null)
        {
            return false;
        }
        return (actions.contains(action) || actions.contains("*"));
    }

    /**
     * Disables a controller by setting the wiildcard action "*"
     *
     * @param controller the name of the controller
     * @return fluent interface
     */
    public ApiManager disableController(String controller)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            actions.clear();
            actions.add("*");
        }
        else
        {
            actions = new ArrayList(1);
            actions.add("*");
            this.disabledActions.put(controller, actions);
        }
        return this;
    }

    /**
     * Disables the given action of the given controller
     *
     * @param controller the controller name
     * @param action     the action name
     * @return fluent interface
     */
    public ApiManager disableAction(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            if (!actions.contains(action))
            {
                actions.add(action);
            }
        }
        else
        {
            actions = new ArrayList(1);
            actions.add(action);
            this.disabledActions.put(controller, actions);
        }
        return this;
    }

    /**
     * Removes all disabled actions of a controller
     *
     * @param controller the controller name
     * @return fluent interface
     */
    public ApiManager removeDisabledActions(String controller)
    {
        this.disabledActions.remove(controller);
        return this;
    }

    /**
     * Removes the given disabled action of the given controller
     *
     * @param controller the controller name
     * @param action     the action name
     * @return fluent interface
     */
    public ApiManager removeDisabledAction(String controller, String action)
    {
        Collection<String> actions = this.disabledActions.get(controller);
        if (actions != null)
        {
            actions.remove(action);
        }
        return this;
    }
}