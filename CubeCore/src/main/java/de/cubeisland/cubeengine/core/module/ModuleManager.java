package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import gnu.trove.map.hash.THashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author CodeInfection
 */
public class ModuleManager
{
    private final Map<String, Module> modules;
    private final CubeCore core;

    public ModuleManager(CubeCore core)
    {
        this.modules = new THashMap<String, Module>();
        this.core = core;
    }

    public ModuleManager registerModule(Module module)
    {
        if (module == null)
        {
            throw new IllegalArgumentException("The module must not be null!");
        }
        String moduleName = module.getModuleName();
        this.modules.put(moduleName, module);

        Set<String> dependencies = module.getDependencies();
        if (dependencies != null)
        {
            Module dependency;
            for (String dependencyName : dependencies)
            {
                dependency = this.getModule(dependencyName);
                if (dependency != null)
                {
                    dependency.addDependingModule(moduleName);
                }
            }
        }

        return this;
    }

    public Module getModule(String name)
    {
        if (name == null)
        {
            return null;
        }

        return this.modules.get(name);
    }

    public Collection<Module> getModules()
    {
        return this.modules.values();
    }

    public ModuleManager disableModule(String name)
    {
        Module module = this.getModule(name);
        if (module != null)
        {
            Set<String> dependingModules = module.getDependingModules();
            for (String moduleName : dependingModules)
            {
                if (!name.equals(moduleName))
                {
                    this.disableModule(name);
                }
            }
            this.core.getPluginManager().disablePlugin(module);
            this.modules.remove(name);
        }
        return this;
    }

    public ModuleManager disableModules()
    {
        for (String module : this.modules.keySet())
        {
            this.disableModule(module);
        }
        return this;
    }

    public void clean()
    {
        this.disableModules();
        this.modules.clear();
    }
}
