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

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Inject;

@Plugin(id = "cubeengine-core", name = "LibCube", version = "1.0.0",
        description = "Core Library for CubeEngine plugins",
        url = "http://cubeengine.org",
        authors = {"Anselm 'Faithcaio' Brehme", "Phillip Schichtel"})
public class LibCube
{
    private final File path;
    private final Logger pluginLogger;
    private ModuleManager mm;

    @Inject
    public LibCube(@ConfigDir(sharedRoot = true) Path path, Logger logger, Injector injector, PluginContainer container)
    {
        this.path = path.resolve("cubeengine").toFile();
        this.pluginLogger = logger;
        this.mm = new ModuleManager(this.path, logger, this, container, injector);
    }

    @Listener
    public void onConstructed(GamePreInitializationEvent event)
    {
        this.mm.init();
    }

    public ModuleManager getModuleManager()
    {
        return mm;
    }
}
