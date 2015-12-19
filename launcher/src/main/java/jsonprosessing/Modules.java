package jsonprosessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import de.cubeisland.engine.reflect.Reflector;
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

    //TODO replace with data from API
    public ArrayList<DisplayedModule> load()
    {
        //Create the reflector
        Reflector reflector = new Reflector();
        Modules modules;
        InputStream in = null;
        try
        {
            in = new FileInputStream("launcher/src/main/resources/structure.json");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        modules = reflector.load(Modules.class, inputStreamReader);
        return modules.buildModules();
    }
}
