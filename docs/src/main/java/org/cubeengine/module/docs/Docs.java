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
package org.cubeengine.module.docs;

import static org.cubeengine.module.docs.DocType.MARKDOWN;

import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.libcube.CubeEngineModule;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.processor.Dependency;
import org.cubeengine.processor.Module;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Module
public class Docs extends CubeEngineModule
{
    @Inject private Reflector reflector;
    @Inject private PermissionManager pm;
    @Inject private CommandManager cm;
    private Path modulePath;
    @Inject private ModuleManager mm;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event)
    {
        this.modulePath = mm.getPathFor(Docs.class);
        cm.addCommands(this, this);
    }

    @Listener
    public void onStartedServer(GameStartedServerEvent event)
    {
        this.generateDocumentation();
    }

    private void generateDocumentation()
    {
        Map<String, ModuleDocs> docs = new HashMap<>();
        for (Map.Entry<Class, PluginContainer> entry : mm.getModulePlugins().entrySet())
        {
            docs.put(entry.getValue().getId(), new ModuleDocs(entry.getValue(), entry.getKey(), reflector, pm, cm));
        }

        System.out.println("Generating Module Docs...");
        for (Map.Entry<String, ModuleDocs> entry : docs.entrySet())
        {
            entry.getValue().generate(modulePath, MARKDOWN, mm.getLoggerFor(getClass()));
        }
        System.out.println("Done Generating Module Docs!");
    }

    @Alias("gd")
    @Command(desc = "Generates documentation")
    public void generateDocs(CommandSource ctx)
    {
        this.generateDocumentation();
    }

}
