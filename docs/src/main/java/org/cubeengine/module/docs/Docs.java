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

import org.cubeengine.logscribe.Log;
import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.libcube.CubeEngineModule;
import org.cubeengine.libcube.InjectService;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.processor.Module;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Module
public class Docs extends CubeEngineModule
{
    @Inject private Reflector reflector;
    @InjectService private PermissionService ps;
    @Inject private CommandManager cm;
    @Inject private PermissionManager pm;
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
        if ("true".equals(System.getenv("CUBEENGINE_DOCS_SHUTDOWN")))
        {
            Sponge.getServer().shutdown();
        }
    }

    private void generateDocumentation()
    {
        Log log = mm.getLoggerFor(Docs.class);
        Map<String, ModuleDocs> docs = new TreeMap<>();
        for (Map.Entry<Class, PluginContainer> entry : mm.getModulePlugins().entrySet())
        {
            docs.put(entry.getValue().getId(), new ModuleDocs(entry.getValue(), entry.getKey(), reflector, pm, ps, cm, mm));
        }

        log.info("Generating Module Docs...");

        try
        {
            String generated = MARKDOWN.getGenerator().generateList(docs, modulePath, mm);

            Path file = modulePath.resolve("README" + MARKDOWN.getFileExtension());
            try
            {
                Files.write(file, generated.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            Path moduleDocsPath = modulePath.resolve("modules");
            if (Files.exists(moduleDocsPath))
            {
                Files.walk(moduleDocsPath).sorted(Comparator.reverseOrder()).forEach(Docs::deleteFile);
            }

            Files.createDirectories(moduleDocsPath);


            for (Map.Entry<String, ModuleDocs> entry : docs.entrySet())
            {
                entry.getValue().generate(moduleDocsPath, MARKDOWN, mm.getLoggerFor(getClass()));
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }


        log.info("Done Generating Module Docs!");
    }

    private static void deleteFile(Path path)
    {
        try
        {
            Files.delete(path);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Alias("gd")
    @Command(desc = "Generates documentation")
    public void generateDocs(CommandSource ctx)
    {
        this.generateDocumentation();
    }

}
