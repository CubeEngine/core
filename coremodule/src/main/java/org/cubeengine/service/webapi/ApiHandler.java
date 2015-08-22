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
package org.cubeengine.service.webapi;

import java.util.LinkedHashMap;
import de.cubeisland.engine.modularity.core.Module;

public abstract class ApiHandler
{
    private final Module module;
    private final String route; // and command (for ws)
    private final String permission;
    private final LinkedHashMap<String, Class> parameters;
    private final RequestMethod reqMethod;

    protected ApiHandler(Module module, String route, String perm, LinkedHashMap<String, Class> params, RequestMethod reqMethod)
    {
        this.module = module;
        this.route = route;
        this.permission = perm;
        this.parameters = params;
        this.reqMethod = reqMethod;
    }

    public abstract ApiResponse execute(ApiRequest request);

    public Module getModule()
    {
        return module;
    }

    public String getRoute()
    {
        return route;
    }

    public String getPermission()
    {
        return permission;
    }

    public LinkedHashMap<String, Class> getParameters()
    {
        return parameters;
    }

    public RequestMethod getReqMethod()
    {
        return reqMethod;
    }
}
