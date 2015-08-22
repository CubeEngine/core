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
package org.cubeengine.service.command;

import java.lang.reflect.Method;
import de.cubeisland.engine.butler.parametric.InvokableMethod;
import de.cubeisland.engine.modularity.core.Module;

/**
 * The origin of a Command in CubeEngine based on an invokable Method
 */
public class CommandOrigin extends InvokableMethod
{
    private final Module module;

    public CommandOrigin(Method method, Object holder, Module module)
    {
        super(method, holder);
        this.module = module;
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
}
