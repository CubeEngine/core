package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Kill extends LogListener
{
    public Kill(Log module)
    {
        super(module, new KillConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity entity = (LivingEntity)event.getEntity();
            if (entity.getHealth() - event.getDamage() <= 0)
            {
                if (event instanceof EntityDamageByEntityEvent)
                {
                    lm.logKill(event.getCause(), ((EntityDamageByEntityEvent)event).getDamager(),
                        event.getEntity(), event.getEntity().getLocation());

                }
                else
                {
                    lm.logKill(event.getCause(), null, event.getEntity(), event.getEntity().getLocation());
                }
            }
        }
    }

    public static class KillConfig extends LogSubConfiguration
    {
        public KillConfig()
        {
            this.actions.put(LogAction.KILL, false);
            this.actions.put(LogAction.ICEFORM, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "kill";
        }
    }
}