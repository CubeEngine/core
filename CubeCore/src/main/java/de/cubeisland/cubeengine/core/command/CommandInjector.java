package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;

/**
 *
 * @author Phillip Schichtel
 */
public interface CommandInjector
{
    public void initialize(Core core);
    public void inject(CommandWrapper command);
    public void remove(String name);
    public void clear();
}
