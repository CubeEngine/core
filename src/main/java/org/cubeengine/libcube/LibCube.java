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
import org.apache.logging.log4j.Logger;
import org.cubeengine.libcube.service.command.ModuleCommand;
import org.cubeengine.libcube.service.command.example.ParentExampleCommand;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.cubeengine.processor.Core;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.nio.file.Path;
/*
Sponge gripes

commands: add currently executing command to context
commands: support for handling incomplete commands
createEntity with Provider
world.getName/asComponent
world docs for converting old uuids to keys
BlockRay is gone
translations for various types (TranslationTextComponent LanguageMap)
enchantment data allowed?
Entity class from EntityType?
Adventure: Audience with permissions
Adventure: Sponge managed Callbacks
Location#getRelative(Direction) (workaround location.add(direction.asBlockOffset()))
AbstractAttackEntityEvent init exception
Keys.IS_REPAIRABLE
check for valid enchantments?

InteractBlockEvent has no World/Location
ReloadEvent?
BlockType etc. is(Provider<BlockType>...):boolean?
 */
@Core
public class LibCube
{
    private final File path;
    private final Logger pluginLogger;
    private PluginContainer container;
    private ModuleManager mm;

    @ModuleCommand private ParentExampleCommand cmd;

    @Inject
    public LibCube(@ConfigDir(sharedRoot = true) Path path, Logger logger, Injector injector, PluginContainer container)
    {
        this.path = path.resolve("cubeengine").toFile();
        this.pluginLogger = logger;
        this.container = container;
        this.mm = new ModuleManager(this.path, logger, this, container, injector);
    }

    @Listener
    public void onConstructed(LoadedGameEvent event)
    {
        this.mm.init();
    }

    public ModuleManager getModuleManager()
    {
        return mm;
    }

    public PluginContainer getContainer() {
        return container;
    }
}
