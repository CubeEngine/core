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
package de.cubeisland.engine.multiverse;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.multiverse.config.MultiverseConfig;
import de.cubeisland.engine.multiverse.converter.DiffcultyConverter;
import de.cubeisland.engine.multiverse.converter.EnvironmentConverter;
import de.cubeisland.engine.multiverse.converter.GameModeConverter;
import de.cubeisland.engine.multiverse.converter.WorldTypeConverter;

public class Multiverse extends Module
{
    private MultiverseConfig config;

    private Map<String, Universe> universes = new HashMap<>();

    @Override
    public void onLoad()
    {
        ConverterManager manager = this.getCore().getConfigFactory().getDefaultConverterManager();
        manager.registerConverter(Difficulty.class, new DiffcultyConverter());
        manager.registerConverter(Environment.class, new EnvironmentConverter());
        manager.registerConverter(GameMode.class, new GameModeConverter());
        manager.registerConverter(WorldType.class, new WorldTypeConverter());
    }

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(MultiverseConfig.class);

        File universesFolder = this.getFolder().resolve("universes").toFile();
        if (universesFolder.exists() && universesFolder.list().length != 0)
        {
            for (File universeDir : universesFolder.listFiles())
            {
                if (universeDir.isDirectory())
                {
                    this.universes.put(universeDir.getName(), new Universe(universeDir, this));
                }
            }
            Set<World> worlds = this.getCore().getWorldManager().getWorlds();
            Map<String, Set<World>> found = new HashMap<>();
            for (Entry<String, Universe> entry : this.universes.entrySet())
            {
                found.put(entry.getKey(), new HashSet<>(entry.getValue().getWorlds()));
                worlds.removeAll(entry.getValue().getWorlds());
            }
            if (!worlds.isEmpty())
            {
                CommandSender sender = this.getCore().getCommandManager().getConsoleSender();
                sender.sendTranslated("&eDiscovering unknown worlds...");
                this.searchUniverses(found, worlds, sender);
                sender.sendTranslated("&eFinishing research...");
                for (Entry<String, Set<World>> entry : found.entrySet())
                {
                    Universe universe = this.universes.get(entry.getKey());
                    Set<World> foundWorlds = entry.getValue();
                    if (universe == null)
                    {
                        File unverseDir = new File(universesFolder, entry.getKey());
                        this.universes.put(entry.getKey(), new Universe(unverseDir, this, foundWorlds));
                    }
                    else
                    {
                        foundWorlds.removeAll(universe.getWorlds());
                        if (foundWorlds.isEmpty())
                        {
                            continue;
                        }
                        universe.addWorlds(foundWorlds);
                    }
                    sender.sendTranslated("&eFound &6%d&e new worlds in the universe &6%s&e!", foundWorlds.size(), entry.getKey());
                }
            }
        }
        else
        {
            this.getLog().info("No previous Universes found! Initializing...");
            CommandSender sender = this.getCore().getCommandManager().getConsoleSender();
            sender.sendTranslated("&6Scraping together Matter...");
            Map<String, Set<World>> found = new HashMap<>();
            this.searchUniverses(found, this.getCore().getWorldManager().getWorlds(), sender);
            sender.sendTranslated("&eFinishing research...");
            for (Entry<String, Set<World>> entry : found.entrySet())
            {
                File universeDir = new File(universesFolder, entry.getKey());
                if (!universeDir.mkdirs())
                {
                    throw new IllegalStateException("Could not create folder for universe!");
                }
                this.universes.put(universeDir.getName(), new Universe(universeDir, this, entry.getValue()));
            }
            sender.sendTranslated("&eFound &6%d&e universes with &6%d&e worlds!", found.size(), this.getCore().getWorldManager().getWorlds().size());
        }
    }

    private void searchUniverses(Map<String, Set<World>> found, Collection<World> worldList, CommandSender sender)
    {
        for (World world : worldList)
        {
            String universeName;
            if (world.getName().contains("_"))
            {
                universeName = world.getName();
                universeName = universeName.substring(0, universeName.indexOf("_"));
            }
            else
            {
                universeName = world.getName();
            }
            Set<World> worlds = found.get(universeName);
            if (worlds == null)
            {
                sender.sendTranslated("&eDiscovered a new Universe! Heating up stars...");
                worlds = new HashSet<>();
                found.put(universeName, worlds);
            }
            worlds.add(world);
            switch (world.getEnvironment())
            {
            case NORMAL:
                sender.sendTranslated("&6%s&e gets formed by crushing rocks together in the universe &6%s", world.getName(), universeName);
                break;
            case NETHER:
                sender.sendTranslated("&eCooling plasma a bit to make &6%s&e in the universe &6%s", world.getName(), universeName);
                break;
            case THE_END:
                sender.sendTranslated("&eFound a cold rock named &6%s&e in the universe &6%s", world.getName(), universeName);
                break;
            }
        }
    }
}
