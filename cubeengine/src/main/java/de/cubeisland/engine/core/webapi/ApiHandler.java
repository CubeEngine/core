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
package de.cubeisland.engine.core.webapi;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import de.cubeisland.engine.core.module.Module;

/**
 * This class is a wrapper for the action requestMethods to extract the information
 * from the annotation and to link the method with its holder.
 *
 * This class is usually not needed by holder developers
 */
public final class ApiHandler
{
    private final ApiHolder holder;
    private final String route;
    private final Method method;
    private final boolean authNeeded;
    private final String[] parameters;
    private final Set<RequestMethod> requestMethods;

    /**
     * Initializes the request action.
     *
     * @param holder     the parent
     * @param route      the route of the action
     * @param method     the method to invoke
     * @param authNeeded whether authentication is needed
     */
    ApiHandler(ApiHolder holder, String route, Method method, boolean authNeeded, String[] parameters, RequestMethod[] requestMethods)
    {
        this.holder = holder;
        this.route = route;
        this.method = method;
        this.authNeeded = authNeeded;
        this.parameters = parameters;
        this.requestMethods = EnumSet.copyOf(Arrays.asList(requestMethods));

        this.method.setAccessible(true);
    }

    public Module getModule()
    {
        return this.holder.getModule();
    }

    public ApiHolder getHolder()
    {
        return this.holder;
    }

    /**
     * Returns the route of action
     *
     * @return the route
     */
    public String getRoute()
    {
        return this.route;
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

    public boolean isMethodAccepted(RequestMethod method)
    {
        return this.requestMethods.contains(method);
    }

    /**
     * This method handles the request.
     */
    public void execute(final ApiRequest request, final ApiResponse response) throws Exception
    {
        this.method.invoke(this.holder, request, response);
    }

    @Override
    public String toString()
    {
        return this.getRoute();
    }
}
