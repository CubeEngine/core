/*
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
package org.cubeengine.libcube;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Optional;

public abstract class CubeEnginePlugin {

    @Inject PluginContainer plugin;
    @Inject private Injector injector;
    @ConfigDir(sharedRoot = false) @Inject File dataFolder;
    private PluginContainer lib;
    private Class<?> module;
    private ModuleManager mm;

    public CubeEnginePlugin(Class module)
    {
        this.lib = Sponge.getPluginManager().getPlugin("cubeengine-core").get();
        this.module = module;
    }

    @Listener
    public void onConstruction(GameConstructionEvent event)
    {
        LibCube libCube = (LibCube) lib.getInstance().get();
        this.mm = libCube.getModuleManager();
        this.mm.registerAndCreate(this.module, this.plugin, this.injector);
        this.mm.getLoggerFor(module).info("Module " + module.getSimpleName() + " loaded!");
    }

    @Listener
    public void onInit(GameInitializationEvent event)
    {
        Object module = mm.getModule(this.module);
        if (module == null)
        {
            mm.getLoggerFor(this.module).error("Failed to load module for {}", plugin.getId());
            return;
        }
        for (Field field : ModuleManager.getAnnotatedFields(module, InjectService.class))
        {
            Optional<?> provided = Sponge.getServiceManager().provide(field.getType());
            if (!provided.isPresent())
            {
                mm.getLoggerFor(this.module).warn("Missing Service");
            }
            else
            {
                try
                {
                    field.setAccessible(true);
                    field.set(module, provided.get());
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public abstract String sourceVersion();
}
