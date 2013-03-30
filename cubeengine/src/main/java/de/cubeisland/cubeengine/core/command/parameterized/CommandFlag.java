package de.cubeisland.cubeengine.core.command.parameterized;

public class CommandFlag
{
    private final String name;
    private final String longName;

    public CommandFlag(String name, String longName)
    {
        this.name = name;
        this.longName = longName;
    }

    public String getName()
    {
        return name;
    }

    public String getLongName()
    {
        return longName;
    }
}
