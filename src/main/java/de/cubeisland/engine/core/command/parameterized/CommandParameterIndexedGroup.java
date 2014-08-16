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

import java.util.ArrayList;
import java.util.List;

public class CommandParameterIndexedGroup implements CommandParametersIndexed
{
    private final List<CommandParametersIndexed> group = new ArrayList<>();
    private final boolean groupRequired;
    private final Integer groupSize;

    public CommandParameterIndexedGroup(boolean groupRequired, Integer groupSize)
    {
        this.groupRequired = groupRequired;
        this.groupSize = groupSize;
    }

    @Override
    public List<CommandParameterIndexed> getAll()
    {
        List<CommandParameterIndexed> list = new ArrayList<>();
        for (CommandParametersIndexed commandParameter : group)
        {
            list.addAll(commandParameter.getAll());
        }
        return list;
    }

    @Override
    public List<CommandParametersIndexed> get()
    {
        return group;
    }

    public boolean isFull()
    {
        return group.size() >= this.groupSize;
    }

    public boolean isGroupRequired()
    {
        return groupRequired;
    }
}
