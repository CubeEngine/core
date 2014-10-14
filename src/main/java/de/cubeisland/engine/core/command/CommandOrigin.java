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
package de.cubeisland.engine.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.cubeisland.engine.command.methodic.InvokableMethod;
import de.cubeisland.engine.core.module.Module;

/**
 * The origin of a Command in CubeEngine based on an invokable Method
 */
public class CommandOrigin implements InvokableMethod
{
    private final Method method;
    private final Object holder;
    private final Module module;

    public CommandOrigin(Method method, Object holder, Module module)
    {
        this.method = method;
        this.holder = holder;
        this.module = module;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object getHolder()
    {
        return holder;
    }

    /**
     * Returns the module owning the command
     *
     * @return the module
     */
    public Module getModule()
    {
        return module;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T invoke(Object... args) throws InvocationTargetException, IllegalAccessException
    {
        return (T)method.invoke(holder, args);
    }
}
