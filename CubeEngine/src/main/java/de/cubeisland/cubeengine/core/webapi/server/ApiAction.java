package de.cubeisland.cubeengine.core.webapi.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is a wrapper for the action methods to extract the information
 * from the annotation and to link the method with its controller.
 *
 * This class is usually not needed by controller developers
 */
public final class ApiAction
{
    private final ApiHolder controller;
    private final String name;
    private final Method method;
    private final boolean authNeeded;
    private final String[] parameters;
    private final String seriaizer;

    /**
     * Initializes the request action.
     *
     * @param controller the parent
     * @param name       the name of the action
     * @param method     the method to invoke
     * @param authNeeded whether authentication is needed
     */
    public ApiAction(ApiHolder controller, String name, Method method, boolean authNeeded, String[] parameters, String serializer)
    {
        this.controller = controller;
        this.name = name;
        this.method = method;
        this.authNeeded = authNeeded;
        this.parameters = parameters;
        this.seriaizer = serializer;

        this.method.setAccessible(true);
    }

    /**
     * Returns the name of action
     *
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns whether this action requires authentication.
     *
     * @return whether authentication is needed
     */
    public Boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * Returns an array of the required parameters
     *
     * @return the required parameters
     */
    public String[] getParameters()
    {
        return this.parameters;
    }

    /**
     * Returns the name serializer this method prevers
     *
     * @return the name of the serializer
     */
    public String getSerializer()
    {
        return this.seriaizer;
    }

    /**
     * This method handles the request.
     *
     * @param server a org.bukkit.Server instance
     * @return the response
     * @throws ApiRequestException
     */
    public void execute(ApiRequest request, ApiResponse response) throws Throwable
    {
        try
        {
            this.method.invoke(this.controller, request, response);
        }
        catch (InvocationTargetException ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}