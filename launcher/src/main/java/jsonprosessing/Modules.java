package jsonprosessing;

import java.util.ArrayList;
import de.cubeisland.engine.reflect.Section;
import de.cubeisland.engine.reflect.codec.yaml.ReflectedYaml;

/**
 * Created by Tim on 30.09.2015.
 */
public class Modules extends ReflectedYaml
{

    Module modules[];

    public class Module implements Section
    {
        String name;
        String id;
        String from;
    }

    public ArrayList<DisplayedModule> buildModules()
    {
        ArrayList<DisplayedModule> returnedModules = new ArrayList<>();
        for (Module module : this.modules)
        {
            DisplayedModule mod = new DisplayedModule(module.name, module.id, module.from);
            returnedModules.add(mod);
        }
        return returnedModules;
    }
}
