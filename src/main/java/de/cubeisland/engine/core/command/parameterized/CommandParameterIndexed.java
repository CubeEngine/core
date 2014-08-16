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

import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.command.ArgumentReader;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class CommandParameterIndexed implements CommandParametersIndexed
{
    /**
     * The display label for the indexed parameter
     */
    private final String[] labels;
    private final Class<?>[] types;
    private final int greed;
    private final boolean required;

    private Completer completer;


    public CommandParameterIndexed(String[] labels, Class<?>[] types, boolean required, int greed)
    {
        int i = 0;
        for (Class<?> type : types)
        {
            expect(ArgumentReader.hasReader(type), "The indexed parameter '" + labels[0] + "(" + i + ")' has an unreadable type: " + type.getName());
            i++;
        }
        this.labels = labels;
        this.types = types;
        this.required = required;
        this.greed = greed;
    }

    public int getGreed()
    {
        return greed;
    }

    public String[] getLabels()
    {
        return labels;
    }

    public Class<?>[] getType()
    {
        return types;
    }

    public Completer getCompleter()
    {
        return completer;
    }

    public void setCompleter(Completer completer)
    {
        this.completer = completer;
    }

    public boolean isRequired()
    {
        return required;
    }

    public static CommandParameterIndexed greedyIndex()
    {
        return new CommandParameterIndexed(new String[]{"0"}, new Class[]{String.class}, false, -1);
    }

    public static CommandParameterIndexed emptyIndex(String label)
    {
        return new CommandParameterIndexed(new String[]{label}, new Class[]{String.class}, false, 1);
    }

    @Override
    public List<CommandParameterIndexed> getAll()
    {
        return Arrays.asList(this);
    }

    @Override
    public List<CommandParametersIndexed> get()
    {
        return Arrays.asList((CommandParametersIndexed)this);
    }
}
