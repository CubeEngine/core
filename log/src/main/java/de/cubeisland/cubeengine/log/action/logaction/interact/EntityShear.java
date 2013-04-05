package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

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
}
