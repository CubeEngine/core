package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.module.Module;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleLogger extends CubeLogger
{
    private final Module module;

    public ModuleLogger(Module module)
    {
        super(module.getName());
        this.module = module;
    }

    public Module getModule()
    {
        return this.module;
    }
}
