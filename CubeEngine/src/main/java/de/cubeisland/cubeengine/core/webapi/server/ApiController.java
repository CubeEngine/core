package de.cubeisland.cubeengine.core.webapi.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * The ApiController is the base class for all controllers The extending class
 * must at least call the super contructor with a Plugin instance. To be able to
 * register the controller, the class also needs to be annotated with the
 *
 * @Controller annotation
 *
 * @since 1.0.0
 */
public abstract class ApiController
{
    private final Plugin plugin;
    private final Server server;
    private final PluginManager pluginManager;
    private final boolean authNeeded;
    private final Map<String, ApiAction> actions;
    private final String name;
    private final String serializer;
    private final boolean unknownToDefaultRouting;

    /**
     * Initializes the controllers
     *
     * @param plugin     the plugin this controllers corresponds to
     * @param authNeeded whether the controllers actions need authentication by
     *                   default or not
     */
    public ApiController(Plugin plugin)
    {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.pluginManager = this.server.getPluginManager();
        this.authNeeded = true;
        this.actions = new ConcurrentHashMap<String, ApiAction>();


        Class<? extends ApiController> clazz = this.getClass();
        Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
        if (controllerAnnotation == null)
        {
            throw new IllegalStateException("Missing annotation for controller " + clazz.getSimpleName());
        }
        this.name = controllerAnnotation.name().trim().toLowerCase();
        this.serializer = controllerAnnotation.serializer();
        this.unknownToDefaultRouting = controllerAnnotation.unknownToDefault();

        for (final Method method : clazz.getDeclaredMethods())
        {
            Action actionAnnotation = method.getAnnotation(Action.class);
            if (actionAnnotation != null)
            {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 2)
                {
                    if (parameterTypes[0].equals(ApiRequest.class) && parameterTypes[1].equals(ApiResponse.class))
                    {
                        String actionName = actionAnnotation.name().trim();
                        if (actionName.length() == 0)
                        {
                            actionName = method.getName();
                        }
                        actionName = actionName.toLowerCase();

                        // debug("  Found action: " + this.name + "/" + actionName); -- TODO fix logging
                        this.actions.put(actionName, new ApiAction(this, actionName, method, actionAnnotation.authenticate(), actionAnnotation.parameters(), actionAnnotation.serializer()));
                    }
                    else
                    {
                        // error("Annotated method " + method.getName() + " has wrong parameters"); -- TODO fix logging
                    }
                }
                else
                {
                    // error("Annotated method " + method.getName() + " has too few or too many parameters"); -- TODO fix logging
                }
            }
        }
    }

    /**
     * Returns the name of this controller
     *
     * @return the name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the corresponding plugin.
     *
     * @return the currensponding plugin
     */
    public final Plugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Returns the server of this controller's plugin
     *
     * @return the server
     */
    public final Server getServer()
    {
        return this.server;
    }

    /**
     * Returns the plugin manager of this controller's plugin
     *
     * @return the plugin manager
     */
    public final PluginManager getPluginManager()
    {
        return this.pluginManager;
    }

    /**
     * Returns whether this actions needs authentication.
     *
     * @return true if auth is needed, otherwise false
     */
    public final boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * Returns the default serializer of te default action
     *
     * @return the serializer
     */
    public final String getSerializer()
    {
        return this.serializer;
    }

    /**
     * Sets an action for the given name.
     *
     * @param name   the name
     * @param action the action
     */
    public final void setAction(String name, ApiAction action)
    {
        if (name != null && action != null)
        {
            name = name.toLowerCase();
            this.actions.put(name, action);
            // debug(String.format("Registered action '%s' in '%s'", name, this.getClass().getSimpleName())); -- TODO fix logging
        }
    }

    /**
     * Returns the action with given name.
     *
     * @param name the name
     * @return the action
     */
    public final ApiAction getAction(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.actions.get(name.toLowerCase());
    }

    /**
     * Returns all actions.
     *
     * @return a map of all actions
     */
    public final Map<String, ApiAction> getActions()
    {
        return this.actions;
    }

    /**
     * This method will be called if the requested action was not found.
     *
     * @param request  the request
     * @param response the response
     */
    public void defaultAction(ApiRequest request, ApiResponse response)
    {
        response.setContent(this.getActions().keySet());
    }

    /**
     * Returns whether this controller allows to route unknown actions to the
     * default action
     *
     * @return true if this controller allows to route unknown actions to the
     *         default action
     */
    public boolean isUnknownToDefaultRoutingAllowed()
    {
        return this.unknownToDefaultRouting;
    }
}