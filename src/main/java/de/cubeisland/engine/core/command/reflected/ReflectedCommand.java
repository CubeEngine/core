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
package de.cubeisland.engine.core.command.reflected;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CubeContextFactory;
import de.cubeisland.engine.core.command.exception.CommandException;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;

public class ReflectedCommand extends CubeCommand
{
    private final Object holder;
    private final Method method;

    @SuppressWarnings("unchecked")
    public ReflectedCommand(Module module, Object holder, Method method, String name, String description, CubeContextFactory factory, Permission permission, boolean checkperm)
    {
        super(module, name, description, factory, permission, checkperm);

        this.holder = holder;
        this.method = method;
        this.method.setAccessible(true);

        Alias annotation = method.getAnnotation(Alias.class);
        if (annotation != null)
        {
            this.registerAlias(annotation.names(), annotation.parents(), annotation.prefix(), annotation.suffix());
        }
    }

    @Override
    public CommandResult run(final CubeContext context)
    {
        try
        {
            Object result = this.method.invoke(this.holder, context);
            if (result instanceof CommandResult)
            {
                return (CommandResult)result;
            }
            return null;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof CommandException)
            {
                throw (CommandException)e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }
    }
}
