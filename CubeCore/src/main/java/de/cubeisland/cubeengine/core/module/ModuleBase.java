package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import gnu.trove.set.hash.THashSet;
import java.util.Collections;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a Module of CubeEngine
 * 
 * @author Phillip Schichtel
 */
public abstract class ModuleBase extends JavaPlugin implements Module
{
    private final String name;
    private final Set<String> dependingModules;
    private final CubeCore core;

    public ModuleBase(String name)
    {
        this.name = name;
        this.dependingModules = new THashSet<String>();
        this.core = CubeCore.getInstance();
    }

    @Override
    public void onEnable()
    {
        this.getModuleManager().registerModule(this);
    }

    public String getModuleName()
    {
        return this.name;
    }

    public void addDependingModule(String name)
    {
        this.dependingModules.add(name);
    }

    public Set<String> getDependingModules()
    {
        return this.dependingModules;
    }

    public Set<String> getDependencies()
    {
        return Collections.<String>emptySet();
    }

    public CubeCore getCore()
    {
        return this.core;
    }

    public ModuleManager getModuleManager()
    {
        return this.core.getModuleManager();
    }
}
