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

import java.nio.file.Path;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.cubeengine.processor.Core;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.plugin.PluginContainer;

/*
Sponge gripes

commands: support for handling incomplete commands

? CommandContext or Cause helper isPlayer? isConsole?
? CommandContext or Cause helper getLocale
async tab-complete
commands: console command audience is not SystemSubject
commands: flags repeat
commands: errors are not shown?
ValueParser get if used in completion
List of quoted String as one parameter

translations for various types (TranslationTextComponent LanguageMap)
enchantment data allowed?
Adventure: Audience with permissions
Keys.IS_REPAIRABLE

inventory transaction drop excess items

SpawnEgg getEntityType and getFor EntityType

PlayerChatEvent no way to get original player receiving

event listener still called if cancelled

InteractBlockEvent.Secondary cancel on doors do not resend the other block.
InteractEntityEvent.Secondary cancel does not reset item in hand on client
InteractBlockEvent.Secondary cancel does not reset item in hand on client

CommandMapping does not contain all aliases (sub-commands)

Piglin Aggro does not call retarget event

resourekey completion does NOT ignore default namespace prefix making tab-completion annoying

Trident

protector interactprimary block

get blocks colliding with aabb on world somewhere

userManager.exists(name)?
Creator Context in events is not Entity reference anymore
Custom Data on User cannot work atm.
 */
@Core
public class LibCube
{
    private final Path path;
    private PluginContainer container;
    private ModuleManager mm;

    @Inject
    public LibCube(Game game, @ConfigDir(sharedRoot = true) Path path, Injector injector, PluginContainer container)
    {
        this.path = path.resolve("cubeengine");
        this.container = container;
        this.mm = new ModuleManager(game, this.path, this, container, injector);
    }

    public ModuleManager getModuleManager()
    {
        return mm;
    }

    public PluginContainer getContainer()
    {
        return container;
    }
}
