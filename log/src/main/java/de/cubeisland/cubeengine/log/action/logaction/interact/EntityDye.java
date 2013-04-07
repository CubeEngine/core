package de.cubeisland.cubeengine.log.action.logaction.interact;

import java.util.EnumSet;

import org.bukkit.DyeColor;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Dyeing sheeps or wolfcollars
 * <p>Events: {@link InteractEntityActionType}</p>
 */
public class EntityDye extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY);
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "entity-dye";
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


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENTITY_DYE_enable;
    }
}
