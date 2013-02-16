package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.List;

public abstract class ParamCompleter
{
    private final Class[] compatibleClasses;

    protected ParamCompleter(Class... compatibleClasses)
    {
        this.compatibleClasses = compatibleClasses;
    }

    public boolean isCompatible(Class type)
    {
        for (Class compatibleClass : this.compatibleClasses)
        {
            if (compatibleClass == type)
            {
                return true;
            }
        }
        return false;
    }

    public Class[] getCompatibleClasses()
    {
        return this.compatibleClasses;
    }

    public abstract List<String> complete(CommandSender sender, String token);
}
