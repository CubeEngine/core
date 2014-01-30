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
package de.cubeisland.engine.backpack;

import java.io.File;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.backpack.converter.NBTItemStackConverter;
import de.cubeisland.engine.core.config.codec.NBTCodec;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.worlds.Multiverse;
import de.cubeisland.engine.worlds.Worlds;

public class Backpack extends Module
{
    protected File singleDir;
    protected File groupedDir;
    protected File globalDir;
    private BackpackManager manager;

    public BackpackPermissions perms()
    {
        return perms;
    }

    private BackpackPermissions perms;

    @Override
    public void onEnable()
    {
        perms = new BackpackPermissions(this);
        this.getCore().getConfigFactory().getCodecManager().getCodec(NBTCodec.class).getConverterManager().
            registerConverter(ItemStack.class, new NBTItemStackConverter());
        this.singleDir = this.getFolder().resolve("single").toFile();
        this.groupedDir = this.getFolder().resolve("grouped").toFile();
        this.globalDir = this.getFolder().resolve("global").toFile();
        this.singleDir.mkdir();
        this.groupedDir.mkdir();
        this.globalDir.mkdir();
        manager = new BackpackManager(this);
    }

    public World getMainWorld(World world)
    {
        Module worlds = this.getCore().getModuleManager().getModule("worlds");
        if (worlds != null && worlds instanceof Worlds)
        {
            Multiverse multiverse = ((Worlds)worlds).getMultiverse();
            return multiverse.getUniverse(world).getMainWorld();
        }
        else
        {
            if (world.getName().contains("_"))
            {
                String pre = world.getName().substring(0, world.getName().indexOf("_"));
                for (World aWorld : this.getCore().getWorldManager().getWorlds())
                {
                    if (aWorld.getName().equals(pre))
                    {
                        return aWorld;
                    }
                }
            }
            return world;
        }
    }
}
