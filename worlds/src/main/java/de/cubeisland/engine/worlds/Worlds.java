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
package de.cubeisland.engine.worlds;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.core.config.codec.NBTCodec;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.worlds.commands.WorldCommands;
import de.cubeisland.engine.worlds.config.WorldsConfig;
import de.cubeisland.engine.worlds.converter.DiffcultyConverter;
import de.cubeisland.engine.worlds.converter.EnvironmentConverter;
import de.cubeisland.engine.worlds.converter.GameModeConverter;
import de.cubeisland.engine.worlds.converter.InventoryConverter;
import de.cubeisland.engine.worlds.converter.PotionEffectConverter;
import de.cubeisland.engine.worlds.converter.WorldTypeConverter;

public class Worlds extends Module
{
    private WorldsPermissions perms;

    public Multiverse getMultiverse()
    {
        return multiverse;
    }

    private Multiverse multiverse;

    public WorldsConfig getConfig()
    {
        return config;
    }

    private WorldsConfig config;


    @Override
    public void onLoad()
    {
        ConverterManager manager = this.getCore().getConfigFactory().getDefaultConverterManager();
        manager.registerConverter(Difficulty.class, new DiffcultyConverter());
        manager.registerConverter(Environment.class, new EnvironmentConverter());
        manager.registerConverter(GameMode.class, new GameModeConverter());
        manager.registerConverter(WorldType.class, new WorldTypeConverter());
///*TODO remove saving into yml too
        manager.registerConverter(Inventory.class, new InventoryConverter(Bukkit.getServer()));
        manager.registerConverter(PotionEffect.class, new PotionEffectConverter());
//*/
        NBTCodec codec = this.getCore().getConfigFactory().getCodecManager().getCodec(NBTCodec.class);
        manager = codec.getConverterManager();
        manager.registerConverter(Inventory.class, new InventoryConverter(Bukkit.getServer()));
        manager.registerConverter(PotionEffect.class, new PotionEffectConverter());
    }

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(WorldsConfig.class);
        multiverse = new Multiverse(this);
        this.getCore().getCommandManager().registerCommand(new WorldCommands(this, multiverse));
        perms = new WorldsPermissions(this);
    }

    public WorldsPermissions perms()
    {
        return perms;
    }
}
