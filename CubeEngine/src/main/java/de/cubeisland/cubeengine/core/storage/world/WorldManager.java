package de.cubeisland.cubeengine.core.storage.world;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import gnu.trove.map.hash.THashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldManager extends BasicStorage<WorldModel>
{
    private static final int REVISION = 3;
    private Map<World, WorldModel> worlds = new THashMap<World, WorldModel>();

    public WorldManager(Database database)
    {
        super(database, WorldModel.class, REVISION);
        this.initialize();

        this.loadWorlds();
    }

    private void loadWorlds()
    {
        Collection<WorldModel> models = this.getAll();
        List<World> loadedWorlds = Bukkit.getServer().getWorlds();
        for (WorldModel model : models)
        {
            World world = Bukkit.getServer().getWorld(UUID.fromString(model.worldUUID));
            if (loadedWorlds.contains(world))
            {
                loadedWorlds.remove(world);
                this.worlds.put(world, model);
            }
        }
        if (!loadedWorlds.isEmpty()) // new worlds?
        {
            for (World world : loadedWorlds)
            {
                WorldModel model = new WorldModel(world);
                this.store(model);
                this.worlds.put(world, model);
            }
        }
    }

    public Integer getWorldId(World world)
    {
        WorldModel model = this.worlds.get(world);
        return model == null ? null : model.key;
    }
}
