package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.ModuleConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.Option;
import java.util.HashMap;

/**
 *
 * @author Faithcaio
 */
public class FlyConfiguration extends ModuleConfiguration
{
    @Option("debug")
    public boolean debugMode = false;
    @Option("mode.flycommand")
    public boolean flycommand = true; //if false fly command does not work
    @Option("mode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
    //TODO remove this test
    @Option("configtests.test1")
    public HashMap<String, String> test1 = new HashMap<String, String>()
    {
        
        {
            put("2StringTest1", "A Text!");
            put("2StringTest2", "A Text too!");
        }
    };
    @Option("configtests.test2")
    public HashMap<String, Object> test2 = new HashMap<String, Object>()
    {
        
        {
            put("2ObjectTest1", "A Text!");
            put("2ObjectTest2", 1234567);
            String[] blub = {"An String Array ftw","cookies 2nd line","3rd","first from behind"};
            put("2ObjectTest3", blub);
        }
    };

    public FlyConfiguration(Module module)
    {
        super(module);
    }
}
