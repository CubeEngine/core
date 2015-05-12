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
package de.cubeisland.engine.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import com.google.inject.Inject;
import de.cubeisland.engine.modularity.asm.AsmModularity;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.graph.Node;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PostInitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

@Plugin(id = "CubeEngine", name = "CubeEngine", version = "1.0.0")
public class CubeEngineSpongePlugin
{
    @Inject private PluginContainer instance;
    @Inject private Game game;
    @Inject private org.slf4j.Logger pluginLogger;
    @ConfigDir(sharedRoot = false) @Inject File dataFolder;

    private final Modularity modularity = new AsmModularity();

    @Subscribe
    public void preInit(PreInitializationEvent event)
    {
        // During this state, the plugin gets ready for initialization.
        // Access to a default logger instance and access to information regarding preferred configuration file locations is available.

        pluginLogger.info("Start CubeEngine...");

        Path loadPath = dataFolder.toPath().resolve("temp");
        Path modules = dataFolder.toPath().resolve("modules");
        try
        {
            pluginLogger.info("Copy Modules");
            // copy modules to tmp folder
            if ((Files.isDirectory(modules) || !Files.exists(modules))
                && (Files.isDirectory(loadPath) || !Files.exists(loadPath)))
            {
                if (Files.isDirectory(loadPath))
                {
                    Files.walkFileTree(loadPath, new RecursiveDirectoryDeleter());
                }
                Files.deleteIfExists(loadPath);
                Files.createDirectories(modules);
                Files.createDirectories(loadPath);
                for (Path file : Files.newDirectoryStream(modules, entry -> Files.isRegularFile(entry)
                    && (entry.getFileName().toString().endsWith(".jar") || entry.getFileName().toString().endsWith(".class"))))
                {
                    Files.copy(file, loadPath.resolve(file.getFileName()));
                }
            }
        }
        catch (IOException e)
        {}

        pluginLogger.info("Load Modules");
        modularity.load(loadPath.toFile());
        pluginLogger.info("done.");
        modularity.getServiceManager().registerService(Game.class, game);
        modularity.getServiceManager().registerService(Modularity.class, modularity);
        modularity.getServiceManager().registerService(Logger.class, pluginLogger);
        modularity.getServiceManager().registerService(File.class, dataFolder);
        modularity.registerProvider(Path.class, new ModulePathProvider(dataFolder));
    }

    @Subscribe
    public void init(InitializationEvent event)
    {
        // During this state, the plugin should finish any work needed in order to be functional.
        // Global event handlers and command registration are handled during initialization.

        game.getServer().getConsole().sendMessage(Texts.of(TextColors.RED, TextStyles.BOLD, "Hi i am the CubeEngine"));

        pluginLogger.info("Start Modules");
        modularity.startAll();
        pluginLogger.info("Finished starting Modules");

        game.getCommandDispatcher().register(this, CommandSpec.builder().setDescription(Texts.of(
                                                 "Reloads the CubeEngine")).setExecutor(
            (commandSource, commandContext) -> {
                // TODO add reloadAll() to Modularity
                modularity.getGraph().getRoot().getSuccessors().forEach(modularity::unload);
                modularity.startAll();
                return CommandResult.success();
            }).build(), "reload");
    }

    @Subscribe
    public void postInit(PostInitializationEvent event)
    {
        // By this state, inter-plugin communication should be ready to occur.
        // Plugins providing an API should be ready to accept basic requests.

        // TODO register our services in Sponge
    }
}
