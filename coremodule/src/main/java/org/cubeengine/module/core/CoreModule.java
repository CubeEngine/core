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
package org.cubeengine.module.core;

import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.filesystem.FileManager;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.i18n.I18nLanguageLoader;
import org.spongepowered.api.Sponge;

@ModuleInfo(name = "CoreModule", description = "The core module of CubeEngine")
public class CoreModule extends Module
{
    @Inject
    @Enable
    public void onEnable(CommandManager cm, FileManager fm, I18n i18n)
    {
        ((I18nLanguageLoader)i18n.getBackend().getLanguageLoader()).provideLanguages(this);
        i18n.registerModule(this);
        fm.dropResources(CoreResource.values());

        // depends on: server, module manager, ban manager
        cm.addCommand(new ModuleCommands(this, getModularity(), Sponge.getPluginManager(), cm, fm, i18n));
        cm.addCommand(new CoreCommands(this, i18n));
    }
}
