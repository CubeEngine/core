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
package de.cubeisland.engine.core.command.context;

import java.util.List;

import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexedGroup;
import de.cubeisland.engine.core.command.parameterized.CommandParametersIndexed;

public class ArgBounds
{
    public static final int NO_MAX = -1;
    private final int min;
    private final int max;

    public ArgBounds(int min)
    {
        this(min, min);
    }

    public ArgBounds(int min, int max)
    {
        if (max > NO_MAX && min > max)
        {
            throw new IllegalArgumentException("The arg limit must not be greater than the minimum!");
        }
        this.min = min;
        this.max = max;
    }

    public ArgBounds(List<CommandParametersIndexed> indexed)
    {
        int tMin = 0;
        int tMax = 0;
        int n = 0;
        for (CommandParametersIndexed iParams : indexed)
        {
            n++;
            if (iParams instanceof CommandParameterIndexedGroup)
            {
                ArgBounds argBounds = new ArgBounds(iParams.get());
                tMin += ((CommandParameterIndexedGroup)iParams).isGroupRequired() ? argBounds.getMin() : 0;
                tMax += argBounds.getMax();
                if (argBounds.getMax() == NO_MAX)
                {
                    tMax = NO_MAX;
                }
            }
            else if (iParams instanceof CommandParameterIndexed)
            {
                if (((CommandParameterIndexed)iParams).getGreed() == -1)
                {
                    if (n == indexed.size())
                    {
                        tMax = NO_MAX;
                        if (((CommandParameterIndexed)iParams).isRequired())
                        {
                            tMin++;
                        }
                        break;
                    }
                    throw new IllegalArgumentException("Greedy arguments are only allowed at the end!");
                }
                else if (((CommandParameterIndexed)iParams).isRequired())
                {
                    tMin += ((CommandParameterIndexed)iParams).getGreed();
                }
                tMax += ((CommandParameterIndexed)iParams).getGreed();
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        this.min = tMin;
        this.max = tMax;
    }

    public int getMin()
    {
        return this.min;
    }

    public int getMax()
    {
        return max;
    }
}
