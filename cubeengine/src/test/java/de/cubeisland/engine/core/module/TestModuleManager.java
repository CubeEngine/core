/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.module;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.exception.CircularDependencyException;
import de.cubeisland.engine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.engine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.engine.core.module.exception.InvalidModuleException;
import de.cubeisland.engine.core.module.exception.MissingDependencyException;
import de.cubeisland.engine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.module.service.ServiceManager;
import de.cubeisland.engine.core.util.StringUtils;

public class TestModuleManager implements ModuleManager
{
    private final Core core;
    private final ModuleLoader ldr;
    private final ServiceManager serviceManager;

    public TestModuleManager(Core core)
    {
        this.core = core;
        this.ldr = null;
        this.serviceManager = new ServiceManager(core);
    }

    @Override
    public ServiceManager getServiceManager()
    {
        return this.serviceManager;
    }

    @Override
    public Module getModule(String id)
    {
        TestModule module = new TestModule();
        ModuleInfo info = new TestModuleInfo(this.core);
        module.initialize(this.core, info, null, this.ldr, null);
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
    public Module loadModule(Path moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        return this.getModule(StringUtils.stripFileExtension(moduleFile.getFileName().toString()));
    }

    @Override
    public void loadModules(Path directory)
    {}

    @Override
    public boolean enableModule(Module module)
    {
        return false;
    }

    @Override
    public void enableModules()
    {}

    @Override
    public void disableModule(Module module)
    {}

    @Override
    public void unloadModule(Module module)
    {}

    @Override
    public void reloadModule(Module module) throws ModuleException
    {
        this.reloadModule(module, false);
    }

        @Override
    public void reloadModule(Module module, boolean fromFile) throws ModuleException
    {
        if (fromFile)
        {

        }
        else
        {
            if (module instanceof Reloadable)
            {
                ((Reloadable)module).reload();
            }
            else
            {
                this.disableModule(module);
                this.enableModule(module);
            }
        }
    }

    @Override
    public int reloadModules()
    {
        return this.reloadModules(false);
    }

        @Override
    public int reloadModules(boolean fromFile)
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
