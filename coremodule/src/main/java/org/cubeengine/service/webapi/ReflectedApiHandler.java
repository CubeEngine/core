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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.ProviderManager;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.command.CommandManager;

public final class ReflectedApiHandler extends ApiHandler
{
    private final Method method;
    private final Object holder;
    private final ProviderManager providerManager;

    public ReflectedApiHandler(Module module, String route, String permission,
                               LinkedHashMap<String, Class> params, RequestMethod reqMethod, Method method,
                               Object holder)
    {
        super(module, route, permission, params, reqMethod);
        this.method = method;
        this.method.setAccessible(true);
        this.holder = holder;
        this.providerManager = module.getModularity().provide(CommandManager.class).getProviderManager();
    }

    @Override
    public ApiResponse execute(final ApiRequest request)
    {
        try
        {
            Parameters params = request.getParams();
            List<Object> list = new ArrayList<>();
            list.add(request);
            for (Entry<String, Class> entry : this.getParameters().entrySet())
            {
                list.add(providerManager.read(entry.getValue(), entry.getValue(), new CommandInvocation(null, params.getString(entry.getKey()), providerManager)));
            }
            return (ApiResponse)this.method.invoke(this.holder, list.toArray(new Object[list.size()]));
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
