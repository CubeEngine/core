package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class MonsterEggUse extends LogActionType
{
    public MonsterEggUse(Log module)
    {
        super(module, 0x80, "monsteregg-use");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityType entityType = EntityType.fromId(logEntry.getItemData().dura); // Dura is entityTypeId
        user.sendTranslated("%s&2%s &aspawned &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(entityType),loc);
    }
}
