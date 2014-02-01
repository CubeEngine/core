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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.cubeisland.engine.core.command.ArgumentReader;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class CommandParameter
{
    private final String name;
    private final Set<String> aliases;

    private final Class<?> type;
    private boolean required;

    private Completer completer;

    public CommandParameter(String name, Class<?> type)
    {
        expect(ArgumentReader.hasReader(type), "The named parameter '" + name + "' has an unreadable type: " + type.getName());
        this.name = name;
        this.aliases = new HashSet<>(0);
        this.type = type;
        this.required = false;
        this.completer = null;
    }

    public String getName()
    {
        return this.name;
    }

    public Set<String> getAliases()
    {
        return this.aliases;
    }

    public CommandParameter addAlias(String alias)
    {
        this.aliases.add(alias);
        return this;
    }

    public CommandParameter addAliases(Collection<String> aliases)
    {
        this.aliases.addAll(aliases);
        return this;
    }

    public CommandParameter addAliases(String... aliases)
    {
        for (String alias : aliases)
        {
            this.addAlias(alias);
        }
        return this;
    }

    public CommandParameter removeAlias(String alias)
    {
        this.aliases.remove(alias);
        return this;
    }

    public CommandParameter removeAliases(Collection<String> aliases)
    {
        this.aliases.removeAll(aliases);
        return this;
    }

    public CommandParameter removeAliases(String... aliases)
    {
        for (String alias : aliases)
        {
            this.removeAlias(alias);
        }
        return this;
    }

    public Class getType()
    {
        return this.type;
    }

    public boolean isRequired()
    {
        return this.required;
    }

    public CommandParameter setRequired(boolean required)
    {
        this.required = required;
        return this;
    }

    public Completer getCompleter()
    {
        return this.completer;
    }

    public CommandParameter setCompleter(Completer completer)
    {
        this.completer = completer;
        return this;
    }
}
