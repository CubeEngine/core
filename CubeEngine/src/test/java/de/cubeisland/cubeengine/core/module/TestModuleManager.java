package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.logger.ModuleLogger;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.cubeengine.core.util.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class TestModuleManager implements ModuleManager
{
    private final Core core;
    private final ModuleLoader ldr;

    public TestModuleManager(Core core)
    {
        this.core = core;
        this.ldr = null;
    }

    @Override
    public Module getModule(String id)
    {
        TestModule module = new TestModule();
        ModuleInfo info = new TestModuleInfo();
        module.initialize(this.core, info, null, new ModuleLogger(this.core, info), this.ldr, null);
        return module;
    }

    @Override
    public <T extends Module> T getModule(Class<T> mainClass)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Module> getModules()
    {
        return Collections.emptyList(); //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Module loadModule(File moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        return this.getModule(StringUtils.stripFileExtension(moduleFile.getName()));
    }

    @Override
    public void loadModules(File directory)
    {}

    @Override
    public boolean enableModule(Module module)
    {
        return false;
    }

    @Override
    public void enableWorldGeneratorModules()
    {}

    @Override
    public void enableModules(boolean worldGenerators)
    {}

    @Override
    public void disableModule(Module module)
    {}

    @Override
    public void unloadModule(Module module)
    {}

    @Override
    public void reloadModule(Module module)
    {
        module.reload();
    }

    @Override
    public int reloadModules()
    {
        return 0;
    }

    @Override
    public void disableModules()
    {}

    @Override
    public void unloadModules()
    {}

    @Override
    public CoreModule getCoreModule()
    {
        return new CoreModule();
    }

    @Override
    public void clean()
    {}
}
