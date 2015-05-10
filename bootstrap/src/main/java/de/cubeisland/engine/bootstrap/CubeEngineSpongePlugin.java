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
import com.google.inject.Inject;
import de.cubeisland.engine.modularity.asm.AsmModularity;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.graph.Node;
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
        Path moduleFolder = dataFolder.toPath().resolve("modules"); // Can contain jars or classes of modules
        try
        {
            Files.createDirectories(moduleFolder);
        }
        catch (IOException e)
        {
            // TODO handle
        }
        pluginLogger.info("Load available Modules");
        modularity.load(moduleFolder.toFile());
        pluginLogger.info("done.");
        modularity.getServiceManager().registerService(Game.class, game); // Provide Sponge Game
    }

    @Subscribe
    public void init(InitializationEvent event)
    {
        // During this state, the plugin should finish any work needed in order to be functional.
        // Global event handlers and command registration are handled during initialization.
        game.getServer().getConsole().sendMessage(Texts.of(TextColors.RED, TextStyles.BOLD, "Hi i am the CubeEngine"));

        pluginLogger.info("Starting Modules...");
        start(modularity.getGraph().getRoot());
        pluginLogger.info("done.");
    }

    private void start(Node node)
    {
        if (node.getInformation() != null)
        {
            pluginLogger.debug("Starting " + node.getInformation().getIdentifier());
            modularity.getStarted(node.getInformation().getClassName());
        }
        node.getChildren().forEach(this::start);
    }

    @Subscribe
    public void postInit(PostInitializationEvent event)
    {
        // By this state, inter-plugin communication should be ready to occur.
        // Plugins providing an API should be ready to accept basic requests.

        // TODO register our services in Sponge
    }
}
