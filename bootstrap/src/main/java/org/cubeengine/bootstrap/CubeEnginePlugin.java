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
package org.cubeengine.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.inject.Inject;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.service.ServiceProvider;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import static de.cubeisland.engine.modularity.asm.AsmInformationLoader.newModularity;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
Ideas:

Multiverse using custom UserStorageService
Enchanted Weapons - Add modifiers via crafting
Drop Special loot - Loot tables for entities and blocks
Spawn Special Mobs naturally
more money sinks
Service for Blockstorage - save data on any block - if TE save on it else separatly
Broadcast Command help / show commandhelp to player
debug permission to show source msg on hover
chat module, when using + at the end of a chat-message buffer it
per player pvp toggle cmd
make Matchers static again if possible
glow effect on module-items using empty enchantmentlist

*/

@Plugin(id = "org.cubeengine", name = "CubeEngine", version = "1.0.0", description = "Bootstrapper for the CubeEngine Module Environment", url = "http://cubeengine.org", authors = {"Anselm 'Faithcaio' Brehme", "Phillip Schichtel"})
public class CubeEnginePlugin
{
    @Inject private PluginContainer instance;
    @Inject private Game game;
    @Inject private org.slf4j.Logger pluginLogger;
    @ConfigDir(sharedRoot = false) @Inject File dataFolder;

    private final Modularity modularity = newModularity();

    @Listener
    public void preInit(GamePreInitializationEvent event)
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

        modularity.register(PluginContainer.class, instance);
        modularity.register(Game.class, game);
        modularity.register(Logger.class, pluginLogger);
        modularity.register(File.class, dataFolder);

        modularity.registerProvider(Path.class, new ModulePathProvider(dataFolder));

        long delta = System.currentTimeMillis();
        pluginLogger.info("Load Modules");
        modularity.loadFromClassPath("de/cubeisland/engine", "org/cubeengine");
        modularity.load(loadPath.toFile(), "de/cubeisland/engine", "org/cubeengine");
        pluginLogger.info("done in {} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - delta));
        delta = System.currentTimeMillis();
        pluginLogger.info("Set up Modules");

        try
        {
            modularity.setupModules();
            pluginLogger.info("Finished setting up Modules in {} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - delta));
        }
        catch (Exception e)
        {
            pluginLogger.error("An Error occured while starting the modules!", e);
        }
    }

    @Listener
    public void init(GameInitializationEvent event)
    {
        // During this state, the plugin should finish any work needed in order to be functional.
        // Global event handlers and command registration are handled during initialization.

        pluginLogger.info("Enable Modules");
        long delta = System.currentTimeMillis();
        modularity.enableModules();
        pluginLogger.info("Finished enabling Modules in {} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - delta));
    }

    @Listener
    public void postInit(GamePostInitializationEvent event)
    {
        // By this state, inter-plugin communication should be ready to occur.
        // Plugins providing an API should be ready to accept basic requests.
        game.getServer().getConsole().sendMessage(Text.of(TextColors.RED, TextStyles.BOLD, "Hi i am the CubeEngine"));

        game.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Reloads the CubeEngine"))
                .executor((commandSource, commandContext) -> {
                        modularity.disableModules();
                        modularity.enableModules();
                        return CommandResult.success();
                    })
                .build(), "reload");

        // TODO register our services in Sponge
    }

    @Listener
    public void onReload(GameReloadEvent event)
    {

    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event)
    {
        modularity.disableModules();
    }

    @Listener
    public void onCmd(SendCommandEvent e)
    {
        String command = e.getCommand();
    }



}