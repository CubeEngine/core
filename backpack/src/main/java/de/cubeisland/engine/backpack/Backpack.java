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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.backpack.converter.NBTItemStackConverter;
import de.cubeisland.engine.core.config.codec.NBTCodec;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.exception.ModuleLoadError;
import de.cubeisland.engine.core.util.McUUID;
import de.cubeisland.engine.worlds.Multiverse;
import de.cubeisland.engine.worlds.Worlds;

public class Backpack extends Module
{
    protected Path singleDir;
    protected Path groupedDir;
    protected Path globalDir;
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
        this.singleDir = this.getFolder().resolve("single");
        this.groupedDir = this.getFolder().resolve("grouped");
        this.globalDir = this.getFolder().resolve("global");

        this.updateToUUID();
        manager = new BackpackManager(this);
    }

    private void updateToUUID()
    {
        try
        {
            Map<String, List<Path>> toRename = new HashMap<>();
            for (Path dir : Files.newDirectoryStream(singleDir))
            {
                addPaths(toRename, dir);
            }
            for (Path dir : Files.newDirectoryStream(groupedDir))
            {
                addPaths(toRename, dir);
            }
            addPaths(toRename, globalDir);
            if (!toRename.isEmpty())
            {
                getLog().info("Updating Backpacks for {} to UUID", toRename.size());
                for (Entry<String, UUID> entry : McUUID.getUUIDForNames(toRename.keySet()).entrySet())
                {
                    for (Path path : toRename.get(entry.getKey()))
                    {
                        if (entry.getValue() != null)
                        {
                            Files.move(path, path.getParent().resolve(entry.getValue().toString()));
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ModuleLoadError(e);
        }
    }

    private void addPaths(Map<String, List<Path>> toRename, Path dir) throws IOException
    {
        if (!Files.isDirectory(dir))
        {
            return;
        }
        for (Path path : Files.newDirectoryStream(dir))
        {
            if (Files.isDirectory(path))
            {
                String name = path.getFileName().toString();
                if (!McUUID.UUID_PATTERN.matcher(name).find())
                {
                    List<Path> paths = toRename.get(name);
                    if (paths == null)
                    {
                        paths = new ArrayList<>();
                        toRename.put(name, paths);
                    }
                    paths.add(path);
                }
            }
        }
    }


    public World getMainWorld(World world)
    {
        Module worlds = this.getCore().getModuleManager().getModule("worlds");
        if (worlds != null && worlds instanceof Worlds)
        {
            Multiverse multiverse = ((Worlds)worlds).getMultiverse();
            return multiverse.getUniverseFrom(world).getMainWorld();
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
