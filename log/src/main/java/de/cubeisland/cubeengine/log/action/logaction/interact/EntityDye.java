package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.DyeColor;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.*;

/**
 * Dyeing sheeps or wolfcollars
 * <p>Events: {@link InteractEntityActionType}</p>
 */
public class EntityDye extends SimpleLogActionType
{
    public EntityDye(Log module)
    {
        super(module, "entity-dye", PLAYER, ENTITY);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        JsonNode json = logEntry.getAdditional();
        DyeColor color = DyeColor.valueOf(json.get("nColor").asText());
        user.sendTranslated("%s&2%s&a dyed a &6%s&a in &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            logEntry.getEntityFromData(),
                            color.name(), loc); //TODO get Pretty name for color
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world
            && logEntry.data == other.data; //same entity
    }
}
