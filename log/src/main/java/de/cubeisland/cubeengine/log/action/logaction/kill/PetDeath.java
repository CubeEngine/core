package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

public class PetDeath extends SimpleLogActionType
{
    public PetDeath(Log module)
    {
        super(module, 0x77, "pet-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (Match.entity().isTameable(logEntry.getEntity()))
        {
            JsonNode json = logEntry.getAdditional();
            if (json.get("owner") != null)
            {
                User owner = this.um.getExactUser(json.get("owner").asText());
                if (logEntry.getCauserUser() != null)
                {
                    user.sendTranslated("&aThe &6%s&a of &2%s &agot slaughtered by &2%s&a!",
                                        this.getPrettyName(logEntry.getEntity()),
                                        owner.getDisplayName(),
                                        logEntry.getCauserUser().getDisplayName());
                }
                else if (logEntry.getCauserEntity() != null)
                {
                    user.sendTranslated("&aThe &6%s&a of &2%s &acould not escape &6%s&a!",
                                        this.getPrettyName(logEntry.getEntity()),
                                        owner.getDisplayName(),
                                        this.getPrettyName(logEntry.getCauserEntity()));
                }
                else // something else
                {
                    user.sendTranslated("&aThe &6%s&a of &2%s &adied!",
                                        this.getPrettyName(logEntry.getEntity()),
                                        owner.getDisplayName());
                }
                return;
            }
        }
        user.sendTranslated("&6%s &adied! &4(Pet without owner)", this.getPrettyName(logEntry.getEntity()));
    }
}
