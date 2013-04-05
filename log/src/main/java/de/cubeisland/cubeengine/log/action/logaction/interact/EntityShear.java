package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.storage.ActionType.ENTITY_SHEAR;

public class EntityShear extends SimpleLogActionType
{
    public EntityShear(Log module)
    {
        super(module, 0x87, "entity-shear");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            this.logSimple(event.getEntity().getLocation(),event.getPlayer(),event.getEntity(),
                           this.serializeData(null, (LivingEntity)event.getEntity(),null));
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
                            this.getPrettyName(logEntry.getEntity()),loc);
    }
}
