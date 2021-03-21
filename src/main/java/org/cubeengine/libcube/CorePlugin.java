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
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;

public abstract class CorePlugin
{
    private final LibCube libCube;

    public CorePlugin(Path path, Logger logger, Injector injector, PluginContainer container)
    {
        libCube = new LibCube(Sponge.game(), path, logger, injector, container);
    }

    public LibCube getCore() {
        return libCube;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event)
    {
        this.libCube.getModuleManager().registerCommands(event, this.libCube.getContainer(), this.libCube);
    }
}
