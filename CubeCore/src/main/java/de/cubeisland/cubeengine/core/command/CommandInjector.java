package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.CubeCore;

/**
 *
 * @author CodeInfection
 */
public interface CommandInjector
{
    public void initialize(CubeCore core);
    public void inject(CommandWrapper command);
    public void remove(String name);
    public void clear();
}
