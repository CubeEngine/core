package de.cubeisland.cubeengine.core.storage.world;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldManager extends SingleKeyStorage<Long, WorldModel>
{
    private static final int REVISION = 3;
    private Map<World, WorldModel> worlds = new THashMap<World, WorldModel>();
    private TLongObjectHashMap<World> worldIds = new TLongObjectHashMap<World>();

    public WorldManager(Database database)
    {
        super(database, WorldModel.class, REVISION);
        this.initialize();
        this.loadWorlds();
    }

    private void loadWorlds()
    {
        Collection<WorldModel> models = this.getAll();
        List<World> loadedWorlds = Bukkit.getWorlds();
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
                this.store(model);
                this.worlds.put(world, model);
                this.worldIds.put(model.key, world);
            }
        }
    }

    public Long getWorldId(World world)
    {
        WorldModel model = this.worlds.get(world);
        return model == null ? null : model.key;
    }

    public Long getWorldId(String worldName)
    {
        World world = Bukkit.getWorld(worldName);
        if (world == null)
        {
            return null;
        }
        WorldModel model = this.worlds.get(world);
        return model == null ? null : model.key;
    }

    public long[] getAllWorldIds()
    {
        return this.worldIds.keys();
    }

    public World getWorld(Long worldId)
    {
        if (worldId == null)
        {
            return null;
        }
        return this.worldIds.get(worldId);
    }
}
