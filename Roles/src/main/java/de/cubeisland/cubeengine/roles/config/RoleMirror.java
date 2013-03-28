package de.cubeisland.cubeengine.roles.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.TLongObjectHashMap;

public class RoleMirror
{
    public final String mainWorld;
    private TLongObjectHashMap<Pair<Boolean, Boolean>> worlds = new TLongObjectHashMap<Pair<Boolean, Boolean>>(); //mirror roles / users
    private Roles module;

    public RoleMirror(Roles module, String mainWorld)
    {
        this.module = module;
        this.mainWorld = mainWorld;
        Long worldId = CubeEngine.getCore().getWorldManager().getWorldId(mainWorld);
        if (worldId == null)
        {
            module.getLog().log(LogLevel.WARNING, "Unkown world " + mainWorld);
        }
        else
        {
            this.worlds.put(worldId, new Pair<Boolean, Boolean>(true, true));
        }
    }

    /**
     * Single-world-mirror
     *
     * @param worldId
     */
    public RoleMirror(Roles module, long worldId)
    {
        this.worlds.put(worldId, new Pair<Boolean, Boolean>(true, true));
        this.mainWorld = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
    }

    public TLongObjectHashMap<Pair<Boolean, Boolean>> getWorlds()
    {
        return this.worlds;
    }

    public void setWorld(String worldName, boolean roles, boolean users)
    {
        Long world = CubeEngine.getCore().getWorldManager().getWorldId(worldName);
        if (world == null)
        {
            module.getLog().log(LogLevel.WARNING, "Unkown world " + worldName + "! Removing from config...");
            return;
        }
        this.worlds.put(world, new Pair<Boolean, Boolean>(roles, users));
    }
}
