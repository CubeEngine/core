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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;

public final class ReflectedApiHandler extends ApiHandler
{
    private final Method method;
    private final Object holder;


    public ReflectedApiHandler(Module module, String route, Permission permission,
                               LinkedHashMap<String, Class> params, RequestMethod reqMethod, Method method,
                               Object holder)
    {
        super(module, route, permission, params, reqMethod);
        this.method = method;
        this.method.setAccessible(true);
        this.holder = holder;
    }

    @Override
    public ApiResponse execute(final ApiRequest request)
    {
        try
        {
            return (ApiResponse)this.method.invoke(this.holder, request);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            return new ApiResponse();
        }
    }

    public Object getHolder()
    {
        return holder;
    }
}
