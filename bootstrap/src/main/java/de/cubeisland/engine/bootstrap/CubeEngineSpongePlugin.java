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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.modularity.core.service.ServiceManager;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.EntitySpawnEvent;
import org.spongepowered.api.event.message.CommandEvent;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PostInitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.spec.CommandSpec;

import static de.cubeisland.engine.modularity.asm.AsmInformationLoader.newModularity;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Plugin(id = "CubeEngine", name = "CubeEngine", version = "1.0.0")
public class CubeEngineSpongePlugin
{
    @Inject private PluginContainer instance;
    @Inject private Game game;
    @Inject private org.slf4j.Logger pluginLogger;
    @ConfigDir(sharedRoot = false) @Inject File dataFolder;

    private final Modularity modularity = newModularity();

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

        ServiceManager sm = modularity.getServiceManager();
        sm.registerService(Game.class, game);
        sm.registerService(Modularity.class, modularity);
        sm.registerService(Logger.class, pluginLogger);
        sm.registerService(File.class, dataFolder);
        modularity.registerProvider(Path.class, new ModulePathProvider(dataFolder));

        long delta = System.currentTimeMillis();
        pluginLogger.info("Load Modules");
        modularity.load(loadPath.toFile());
        pluginLogger.info("done in {} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - delta));
        delta = System.currentTimeMillis();
        pluginLogger.info("Start Modules");
        try
        {
            modularity.startModules();
            pluginLogger.info("Finished starting Modules in {} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - delta));
        }
        catch (Exception e)
        {
            pluginLogger.error("An Error occured while starting the modules!", e);
        }
    }

    @Subscribe
    public void init(InitializationEvent event)
    {
        // During this state, the plugin should finish any work needed in order to be functional.
        // Global event handlers and command registration are handled during initialization.
        game.getServer().getConsole().sendMessage(Texts.of(TextColors.RED, TextStyles.BOLD, "Hi i am the CubeEngine"));

        game.getCommandDispatcher().register(this, CommandSpec.builder().description(Texts.of(
            "Reloads the CubeEngine")).executor((commandSource, commandContext) -> {
            modularity.stopModules();
            modularity.startModules();
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

    @Subscribe
    public void onCmd(CommandEvent e)
    {
        String command = e.getCommand();
    }
}
