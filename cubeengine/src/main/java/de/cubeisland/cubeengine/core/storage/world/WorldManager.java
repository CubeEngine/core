package de.cubeisland.cubeengine.core.storage.world;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class WorldManager
{
    private final BukkitCore core;
    private final WorldStorage storage;
    private final Map<World, WorldModel> worlds;
    private final TLongObjectHashMap<World> worldIds;

    public WorldManager(BukkitCore core)
    {
        this.core = core;
        this.storage = new WorldStorage(core.getDB());
        this.worlds = new THashMap<World, WorldModel>();
        this.worldIds = new TLongObjectHashMap<World>();
        this.loadWorlds();
    }

    private void loadWorlds()
    {
        Collection<WorldModel> models = this.storage.getAll();
        List<World> loadedWorlds = this.core.getServer().getWorlds();
        for (WorldModel model : models)
        {
            World world = Bukkit.getWorld(UUID.fromString(model.worldUUID));
            if (loadedWorlds.contains(world))
            {
                loadedWorlds.remove(world);
                this.worlds.put(world, model);
                this.worldIds.put(model.key, world);
            }
        }
        if (!loadedWorlds.isEmpty()) // new worlds?
        {
            for (World world : loadedWorlds)
            {
                WorldModel model = new WorldModel(world);
                this.storage.store(model);
                this.worlds.put(world, model);
                this.worldIds.put(model.key, world);
            }
        }
    }

    public synchronized long getWorldId(World world)
    {
        WorldModel model = this.worlds.get(world);
        if (model == null)
        {
            model = new WorldModel(world);
            this.storage.store(model);
            this.worlds.put(world,model);
            this.worldIds.put(model.key,world);
        }
        return model.key;
    }

    public synchronized Long getWorldId(String name)
    {
        World world = this.core.getServer().getWorld(name);
        if (world == null)
        {
            return null;
        }
        WorldModel model = this.worlds.get(world);
        return model == null ? null : model.key;
    }

    public synchronized long[] getAllWorldIds()
    {
        return this.worldIds.keys();
    }

    public synchronized World getWorld(long id)
    {
        return this.worldIds.get(id);
    }

    public World getWorld(String name)
    {
        assert CubeEngine.isMainThread(): "Must be executed from main thread!";
        return this.core.getServer().getWorld(name);
    }

    public World getWorld(UUID uid)
    {
        assert CubeEngine.isMainThread(): "Must be executed from main thread!";
        return this.core.getServer().getWorld(uid);
    }

    public Set<World> getWorlds()
    {
        assert CubeEngine.isMainThread(): "Must be executed from main thread!";
        return new THashSet<World>(this.core.getServer().getWorlds());
    }

    private class WorldStorage extends SingleKeyStorage<Long, WorldModel>
    {
        private static final int REVISION = 3;

        public WorldStorage(Database database)
        {
            super(database, WorldModel.class, REVISION);
            this.initialize();
        }

    }
}
