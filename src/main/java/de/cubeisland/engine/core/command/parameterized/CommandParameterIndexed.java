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
package de.cubeisland.engine.core.command.parameterized;

import de.cubeisland.engine.command.context.IndexedParameter;
import de.cubeisland.engine.core.permission.Permission;

public class CommandParameterIndexed extends IndexedParameter
{
    protected final Permission permission;

    public CommandParameterIndexed(Class<?> type, Class<?> reader, int greed, boolean required, String valueLabel,
                                   String description, Permission permission)
    {
        super(type, reader, greed, required, valueLabel, description);
        this.permission = permission;
    }

    public static CommandParameterIndexed greedyIndex()
    {
        return new CommandParameterIndexed(String.class, String.class, -1, false, "0", null, null);
    }

    public static CommandParameterIndexed emptyIndex(String label)
    {
        return new CommandParameterIndexed(String.class, String.class, 1, false, label, null, null);
    }
}
