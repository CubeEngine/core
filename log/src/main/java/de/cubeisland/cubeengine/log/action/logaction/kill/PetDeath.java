package de.cubeisland.cubeengine.log.action.logaction.kill;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.EntityData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.KILL;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * pet-death
 * <p>Events: {@link KillActionType}</p>
 */
public class PetDeath extends SimpleLogActionType
{
    public PetDeath(Log module)
    {
        super(module, "pet-death", PLAYER, ENTITY, KILL);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityData killed =  logEntry.getEntityFromData();
        if (Match.entity().isTameable(killed.entityType))
        {
            JsonNode json = logEntry.getAdditional();
            if (json.get("owner") != null)
            {
                User owner = this.um.getExactUser(json.get("owner").asText());
                if (logEntry.hasCauserUser())
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &agot slaughtered by &2%s%s&a!",
                                       time ,killed,
                                        owner.getDisplayName(),
                                        logEntry.getCauserUser().getDisplayName(),loc);
                }
                else if (logEntry.hasCauserEntity())
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &acould not escape &6%s%s&a!",
                                        time,killed,
                                        owner.getDisplayName(),
                                        logEntry.getCauserEntity(),loc);
                }
                else // something else
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &adied%s&a!",
                                        time,killed,
                                        owner.getDisplayName(),loc);
                }
                return;
            }
        }
        user.sendTranslated("&6%s &adied! &4(Pet without owner)", logEntry.getEntityFromData());
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PET_DEATH_enable;
    }

}
