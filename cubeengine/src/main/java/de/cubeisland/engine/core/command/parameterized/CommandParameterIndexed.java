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

import de.cubeisland.engine.core.command.ArgumentReader;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class CommandParameterIndexed
{
    /**
     * The display label for the indexed parameter
     * <p>Labels like <code>true|false</code> will register a TabCompleter if not given
     */
    private final String label;
    private final Class<?> type;

    private Completer completer;

    public CommandParameterIndexed(String label, Class<?> type)
    {
        expect(ArgumentReader.hasReader(type), "The indexed parameter '" + label + "' has an unreadable type: " + type.getName());
        this.label = label;
        this.type = type;
    }

    public String getLabel()
    {
        return label;
    }

    public Class<?> getType()
    {
        return type;
    }

    public Completer getCompleter()
    {
        return completer;
    }

    public void setCompleter(Completer completer)
    {
        this.completer = completer;
    }
}
