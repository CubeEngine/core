package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.EntityData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * spawing entities with spawneggs
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.interaction.RightClickActionType RightClickActionType}</p>
 */
public class MonsterEggUse extends SimpleLogActionType
{
    public MonsterEggUse(Log module)
    {
        super(module, "monsteregg-use", true, PLAYER, ENTITY);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityType entityType = EntityType.fromId(logEntry.getItemData().dura); // Dura is entityTypeId
        user.sendTranslated("%s&2%s &aspawned &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            new EntityData(entityType,null),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world
            && logEntry.getItemData().dura == other.getItemData().dura;
   }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).MONSTER_EGG_USE_enable;
    }

}
