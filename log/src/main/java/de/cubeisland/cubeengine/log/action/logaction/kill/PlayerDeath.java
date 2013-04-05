package de.cubeisland.cubeengine.log.action.logaction.kill;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

public class PlayerDeath extends SimpleLogActionType
{
    public PlayerDeath(Log module)
    {
        super(module, 0x74, "player-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getCauserUser() != null)
        {
            user.sendTranslated("&2%s &agot slaughtered by &2%s&a!",
                                logEntry.getUser().getDisplayName(),
                                logEntry.getCauserUser().getDisplayName());
        }
        else if (logEntry.getCauserEntity() != null)
        {
            user.sendTranslated("&2%s &acould not escape &6%s&a!",
                                logEntry.getUser().getDisplayName(),
                                this.getPrettyName(logEntry.getCauserEntity()));
        }
        else // something else
        {
            JsonNode json = logEntry.getAdditional();
            DamageCause dmgC = DamageCause.valueOf(json.get("dmgC").asText());
            user.sendTranslated("&2%s &adied! &f(&6%s&f)",
                                logEntry.getUser().getDisplayName(),
                                this.getPrettyName(dmgC));
        }
    }
}
