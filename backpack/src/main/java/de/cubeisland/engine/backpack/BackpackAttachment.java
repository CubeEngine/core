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
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.DAT;

public class BackpackAttachment extends UserAttachment
{
    protected final Map<World, Map<String, BackpackInventories>> backpacks = new HashMap<>();
    protected final Map<World, Map<String, BackpackInventories>> groupedBackpacks = new HashMap<>();
    protected final Map<String, BackpackInventories> globalBackpacks = new HashMap<>();

    public void loadGlobalBackpacks()
    {
        Backpack module = (Backpack)this.getModule();
        this.loadBackpacks(module.globalDir, globalBackpacks);
    }
    // backpack/global/<playername>/backpackname

    // backpack/<worldname>/<playername>/backpackname
    // OR backpack/grouped/<mainworldname>/<playername>/<backpackname>
    // OR backpack/single/<worldname>/...
    // without worlds module groups work as default universe would build world world_nether & world_the_end are grouped
    public void loadBackpacks(World world)
    {
        this.loadGlobalBackpacks();
        if (world == null)
        {
            return;
        }
        Backpack module = (Backpack)this.getModule();
        Path dir = module.singleDir.resolve(world.getName());
        if (Files.isDirectory(dir))
        {
            Map<String, BackpackInventories> map = this.backpacks.get(world);
            if (map == null)
            {
                map = new HashMap<>();
                this.backpacks.put(world, map);
            }
            this.loadBackpacks(dir, map);
        }
        World mainWorld = ((Backpack)this.getModule()).getMainWorld(world);
        dir = module.groupedDir.resolve(mainWorld.getName());
        if (Files.isDirectory(dir))
        {
            Map<String, BackpackInventories> map = this.groupedBackpacks.get(mainWorld);
            if (map == null)
            {
                map = new HashMap<>();
                this.groupedBackpacks.put(mainWorld, map);
            }
            this.loadBackpacks(dir, map);
        }
    }

    private Path getGlobalBackpack(String name)
    {
        Backpack module = (Backpack)this.getModule();
        Path path = module.globalDir.resolve(this.getHolder().getUniqueId().toString());
        try
        {
            Files.createDirectories(path);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e); // TODO better exeption
        }
        return path.resolve(name + DAT.getExtention());
    }

    private Path getSingleBackpack(String name, String worldName)
    {
        Backpack module = (Backpack)this.getModule();
        Path path = module.singleDir.resolve(worldName).resolve(this.getHolder().getUniqueId().toString());
        try
        {
            Files.createDirectories(path);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e); // TODO better exeption
        }
        return path.resolve(name + DAT.getExtention());
    }

    private Path getGroupedBackpack(String name, String worldName)
    {
        Backpack module = (Backpack)this.getModule();
        Path path = module.groupedDir.resolve(worldName).resolve(this.getHolder().getUniqueId().toString());
        try
        {
            Files.createDirectories(path);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e); // TODO better exeption
        }
        return path.resolve(name + DAT.getExtention());
    }

    protected void loadBackpacks(Path dir, Map<String, BackpackInventories> map)
    {
        Path playerDir = dir.resolve(this.getHolder().getUniqueId().toString());
        if (Files.isDirectory(playerDir))
        {
            try
            {
                for (Path path : Files.newDirectoryStream(playerDir, DAT.getExtention()))
                {
                    String name = StringUtils.stripFileExtension(path.getFileName().toString());
                    BackpackData load = this.getModule().getCore().getConfigFactory().load(BackpackData.class, path.toFile());
                    BackpackInventories bpInv = map.get(name);
                    if (bpInv == null)
                    {
                        map.put(name, new BackpackInventories((Backpack)this.getModule(), load));
                    }
                    else
                    {
                        bpInv.data = load;
                    }
                }
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e); // TODO better exception
            }
        }
    }

    public BackpackInventories getBackpack(String name, World world)
    {
        BackpackInventories backpack = this.globalBackpacks.get(name);
        if (backpack != null)
        {
            return backpack;
        }
        if (world == null) return null;
        Map<String, BackpackInventories> map = this.backpacks.get(world);
        if (map != null)
        {
             backpack = map.get(name);
        }
        if (backpack != null)
        {
            return backpack;
        }
        World mainWorld = ((Backpack)this.getModule()).getMainWorld(world);
        map = this.groupedBackpacks.get(mainWorld);
        if (map != null)
        {
            backpack = map.get(name);
        }
        return backpack;
    }

    public void createBackpack(String name, World forWorld, boolean blockIn, Integer pages, Integer size)
    {
        Path file = this.getSingleBackpack(name, forWorld.getName());
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(file.toFile());
        data.save();
        Map<String, BackpackInventories> backpacks = this.backpacks.get(forWorld);
        if (backpacks == null)
        {
            backpacks = new HashMap<>();
            this.backpacks.put(forWorld, backpacks);
        }
        backpacks.put(name, new BackpackInventories((Backpack)getModule(), data));
    }

    public void createGroupedBackpack(String name, World forWorld, boolean blockIn, Integer pages, Integer size)
    {
        Path path = this.getGroupedBackpack(name, forWorld.getName());
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(path.toFile());
        data.save();
        Map<String, BackpackInventories> backpacks = this.backpacks.get(forWorld);
        if (backpacks == null)
        {
            backpacks = new HashMap<>();
            this.backpacks.put(forWorld, backpacks);
        }
        backpacks.put(name, new BackpackInventories((Backpack)getModule(), data));
    }

    public void createGlobalBackpack(String name, boolean blockIn, Integer pages, Integer size)
    {
        Path file = this.getGlobalBackpack(name);
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(file.toFile());
        data.save();
        globalBackpacks.put(name, new BackpackInventories((Backpack)getModule(), data));
    }
}
