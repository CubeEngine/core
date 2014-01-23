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
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import de.cubeisland.engine.core.user.UserAttachment;

public class BackpackAttachment extends UserAttachment
{
    protected Map<World, Map<String, BackpackInventories>> backpacks = new HashMap<>();
    protected Map<World, Map<String, BackpackInventories>> groupedBackpacks = new HashMap<>();
    protected Map<String, BackpackInventories> globalBackpacks = new HashMap<>();

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
        File dir = new File(module.singleDir, world.getName());
        if (dir.exists() && dir.isDirectory())
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
        dir = new File(module.groupedDir, mainWorld.getName());
        if (dir.exists())
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

    private File getGlobalBackpack(String name)
    {
        Backpack module = (Backpack)this.getModule();
        File dir = new File(module.globalDir, this.getHolder().getName());
        dir.mkdir();
        return new File(dir, name + ".dat");
    }

    private File getSingleBackpack(String name, String worldName)
    {
        Backpack module = (Backpack)this.getModule();
        File dir = new File(module.singleDir, worldName);
        dir.mkdir();
        dir = new File(dir, this.getHolder().getName());
        dir.mkdir();
        return new File(dir, name + ".dat");
    }

    private File getGroupedBackpack(String name, String worldName)
    {
        Backpack module = (Backpack)this.getModule();
        File dir = new File(module.groupedDir, worldName);
        dir.mkdir();
        dir = new File(dir, this.getHolder().getName());
        dir.mkdir();
        return new File(dir, name + ".dat");
    }

    protected void loadBackpacks(File dir, Map<String, BackpackInventories> map)
    {
        File playerDir = new File(dir, this.getHolder().getName());
        if (playerDir.exists() && playerDir.isDirectory())
        {
            for (File file : playerDir.listFiles())
            {
                if (!file.isDirectory() && file.getName().endsWith(".dat"))
                {
                    String name = file.getName().substring(0, file.getName().lastIndexOf(".dat"));
                    BackpackData load = this.getModule().getCore().getConfigFactory().load(BackpackData.class, file);
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
        File file = this.getSingleBackpack(name, forWorld.getName());
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(file);
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
        File file = this.getGroupedBackpack(name, forWorld.getName());
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(file);
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
        File file = this.getGlobalBackpack(name);
        BackpackData data = this.getModule().getCore().getConfigFactory().create(BackpackData.class);
        data.allowItemsIn = !blockIn;
        data.pages = pages;
        data.size = size;
        data.setFile(file);
        data.save();
        globalBackpacks.put(name, new BackpackInventories((Backpack)getModule(), data));
    }
}
