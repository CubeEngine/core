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

import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.module.Module;

public abstract class ContainerCommand extends CubeCommand implements CommandHolder
{
    private DelegatingContextFilter delegation;

    public ContainerCommand(Module module)
    {
        new ContainerCommandBuilder(this).setModule(module);
    }

    public void delegateChild(final String name)
    {
        this.delegation = new DelegatingContextFilter()
        {
            @Override
            public String delegateTo(CubeContext context)
            {
                return name;
            }
        };
    }

    public void delegateChild(DelegatingContextFilter filter)
    {
        this.delegation = filter;
    }

    public DelegatingContextFilter getDelegation()
    {
        return this.delegation;
    }

    public static abstract class DelegatingContextFilter
    {
        public abstract String delegateTo(CubeContext context);

        public CubeContext filterContext(CubeContext context, String child)
        {
            return context;
        }
    }
}
