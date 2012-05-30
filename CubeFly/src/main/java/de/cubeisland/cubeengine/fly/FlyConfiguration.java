package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.ModuleConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.Option;

/**
 *
 * @author Faithcaio
 */
public class FlyConfiguration extends ModuleConfiguration
{
    @Option("debug") public boolean debugMode = false;
    @Option("mode.flycommand") public boolean flycommand = true; //if false fly command does not work
    @Option("mode.flyfeather") public boolean flyfeather = true; //if false feather fly does not work
    
    public FlyConfiguration(Module module)
    {
        super(module);
    }
    
    public void loadConfig()
    {
        this.loadConfiguration(this.getClass());
    }
    
    public void saveConfig()
    {
        this.saveConfiguration(this.getClass());
    }
}
