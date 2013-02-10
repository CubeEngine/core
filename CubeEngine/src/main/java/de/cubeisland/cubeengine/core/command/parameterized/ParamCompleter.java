package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.user.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public abstract List<String> complete(User sender, String token);
}
