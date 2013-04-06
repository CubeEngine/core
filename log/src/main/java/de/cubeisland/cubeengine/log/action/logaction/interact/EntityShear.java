package de.cubeisland.cubeengine.log.action.logaction.interact;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Shearing sheeps or mooshrooms
 * <p>Events: {@link PlayerShearEntityEvent}</p>
 */
public class EntityShear extends SimpleLogActionType
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
        return "entity-shear";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            this.logSimple(event.getEntity().getLocation(),event.getPlayer(),event.getEntity(),
                           this.serializeData(null, event.getEntity(),null));
        }
        else
        {
            System.out.print("Sheared something: "+event.getEntity()); //TODO remove
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a sheared &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            logEntry.getEntityFromData(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.data == other.data
            && logEntry.world == other.world;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENTITY_SHEAR_enable;
    }
}
