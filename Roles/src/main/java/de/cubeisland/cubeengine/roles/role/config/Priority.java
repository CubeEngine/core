package de.cubeisland.cubeengine.roles.role.config;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Priority
{
    private static final TIntObjectHashMap<Priority> prio = new TIntObjectHashMap<Priority>();
    private static final THashMap<String, Priority> prioNames = new THashMap<String, Priority>();
    public static final Priority ABSULTEZERO = new Priority(-273, "ABSULTEZERO");
    public static final Priority MINIMUM = new Priority(0, "MINIMUM");
    public static final Priority LOWEST = new Priority(125, "LOWEST");
    public static final Priority LOW = new Priority(375, "LOW");
    public static final Priority NORMAL = new Priority(500, "NORMAL");
    public static final Priority HIGH = new Priority(625, "HIGH");
    public static final Priority HIGHER = new Priority(750, "HIGHER");
    public static final Priority HIGHEST = new Priority(1000, "HIGHEST");
    public static final Priority OVER9000 = new Priority(9001, "OVER9000");
    public final int value;
    public final String name;

    private Priority(int value, String name)
    {
        this.value = value;
        this.name = name;
        if (name != null)
        {
            prioNames.put(name, this);
        }
        prio.put(value, this);
    }

    public Priority(int value)
    {
        this(value, null);
    }

    public static Priority getPriority(int value)
    {
        Priority p = prio.get(value);
        if (p == null)
        {
            p = new Priority(value);
        }
        return p;
    }

    @Override
    public String toString()
    {
        return this.name == null ? String.valueOf(this.value) : this.name;
    }

    public static Priority getByName(String name)
    {
        return prioNames.get(name);
    }
}
