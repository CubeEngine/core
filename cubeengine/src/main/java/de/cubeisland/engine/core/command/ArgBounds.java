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

import java.util.List;

import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

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

    public ArgBounds(List<CommandParameterIndexed> indexed)
    {
        int tMin = 0;
        int tMax = 0;
        for (int i = 0; i < indexed.size(); i++)
        {
            CommandParameterIndexed indexedParam = indexed.get(i);
            if (indexedParam.getCount() == -1)
            {
                if (i + 1 == indexed.size())
                {
                    tMax = NO_MAX;
                    if (indexedParam.isGroupRequired())
                    {
                        tMin++;
                    }
                    break;
                }
                throw new IllegalArgumentException("Greedy arguments are only allowed at the end!");
            }
            if (indexedParam.isGroupRequired())
            {
                tMin += indexedParam.getCount();
                if (!indexedParam.isRequired())
                {
                    tMin -= 1;
                }
            }
            tMax += indexedParam.getCount();
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

    public boolean inBounds(int n)
    {
        if (n < this.min)
        {
            return false;
        }
        if (this.max > NO_MAX && n > this.max)
        {
            return false;
        }
        return true;
    }
}
