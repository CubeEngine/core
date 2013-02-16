package de.cubeisland.cubeengine.core.command.parameterized;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommandParameter
{
    private final String name;
    private final Set<String> aliases;

    private final Class type;
    private boolean required;

    private Completer completer;

    public CommandParameter(String name, Class type)
    {
        this.name = name;
        this.aliases = new HashSet<String>(0);
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
