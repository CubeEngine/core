package de.cubeisland.cubeengine.log.action.logaction.spawn;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * natural spawning
 * <p>Events: {@link EntitySpawnActionType}</p>
 */
public class NaturalSpawn extends SimpleLogActionType

{
    public NaturalSpawn(Log module)
    {
        super(module, true, ENTITY);
    }

    @Override
    public String getName()
    {
        return "natural-spawn";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &aspawned naturally%s&a!",
                            time,logEntry.getCauserEntity(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world
            && logEntry.location.equals(other.location);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).NATURAL_SPAWN_enable;
    }
}
