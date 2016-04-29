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
package org.cubeengine.libcube.service.command;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.ContextValue;
import org.cubeengine.libcube.service.i18n.I18n;

public class CommandContextValue implements ContextValue
{
    private I18n i18n;

    public CommandContextValue(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public Object getContext(CommandInvocation invocation, Class<?> clazz)
    {
        return new CommandContext(invocation, i18n);
    }
}
