package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.BasicContext;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.*;

public class ParameterizedContext extends BasicContext
{
    private final Set<String> flags;
    private final Map<String, Object> params;

    private final int flagCount;
    private final int paramCount;

    public ParameterizedContext(CubeCommand command, CommandSender sender, Stack<String> labels, LinkedList<String> args, Set<String> flags, Map<String, Object> params)
    {
        super(command, sender, labels, args);
        this.flags = flags;
        this.params = params;

        this.flagCount = flags.size();
        this.paramCount = params.size();
    }

    public boolean hasFlag(String name)
    {
        return this.flags.contains(name.toLowerCase(Locale.ENGLISH));
    }

    public boolean hasFlags(String... names)
    {
        for (String name : names)
        {
            if (!this.hasFlag(name))
            {
                return false;
            }
        }
        return true;
    }

    public int getFlagCount()
    {
        return this.flagCount;
    }

    public Set<String> getFlags()
    {
        return new THashSet<String>(this.flags);
    }

    public boolean hasParams()
    {
        return this.paramCount > 0;
    }

    public Map<String, Object> getParams()
    {
        return new THashMap<String, Object>(this.params);
    }

    public boolean hasParam(String name)
    {
        return this.params.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    public <T> T getParam(String name)
    {
        return (T)this.params.get(name.toLowerCase(Locale.ENGLISH));
    }

    public <T> T getParam(String name, T def)
    {
        try
        {
            T value = this.getParam(name);
            if (value != null)
            {
                return value;
            }
        }
        catch (Exception ignored)
        {}
        return def;
    }

    public String getString(String name)
    {
        return this.getParam(name);
    }

    public String getString(String name, String def)
    {
        return this.getParam(name, def);
    }

    public User getUser(String name)
    {
        return this.getParam(name, null);
    }
}
