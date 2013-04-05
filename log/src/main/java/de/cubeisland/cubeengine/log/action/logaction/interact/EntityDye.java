package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.DyeColor;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

public class EntityDye extends SimpleLogActionType
{
    public EntityDye(Log module)
    {
        super(module, 0x88, "entity-dye");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        JsonNode json = logEntry.getAdditional();
        DyeColor color = DyeColor.valueOf(json.get("nColor").asText());
        user.sendTranslated("%s&2%s&a dyed a &6%s&a in &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(logEntry.getEntity()),
                            this.getPrettyName(color), loc);
    }
}
