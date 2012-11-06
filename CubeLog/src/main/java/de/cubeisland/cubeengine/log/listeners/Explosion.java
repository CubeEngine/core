package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Explosion extends LogListener
{
    public Explosion(Log module)
    {
        super(module, new ExplosionConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        ExplosionConfig c = this.getConfiguration();
        if ((event.getEntity() == null && !c.actions.get(LogAction.EXPLOSION_MISC.toString()))
            || (event.getEntity() instanceof TNTPrimed && !c.actions.get(LogAction.EXPLOSION_TNT.toString()))
            || (event.getEntity() instanceof Creeper && !c.actions.get(LogAction.EXPLOSION_CREEPER.toString()))
            || (event.getEntity() instanceof Fireball && !c.actions.get(LogAction.EXPLOSION_GHASTFIREBALL.toString()))
            || (event.getEntity() instanceof EnderDragon && !c.actions.get(LogAction.EXPLOSION_ENDERDRAGON.toString())))
        // TODO dont forget to check this later on|| (!c.actions.get(LogAction.EXPLOSION_MISC))
        {
            return;
        }
        lm.logExplosion(event.blockList(), event.getEntity());
    }

    public static class ExplosionConfig extends LogSubConfiguration
    {
        public ExplosionConfig()
        {
            this.actions.put(LogAction.EXPLOSION_TNT, true);
            this.actions.put(LogAction.EXPLOSION_CREEPER, true);
            this.actions.put(LogAction.EXPLOSION_GHASTFIREBALL, true);
            this.actions.put(LogAction.EXPLOSION_ENDERDRAGON, true);
            this.actions.put(LogAction.EXPLOSION_MISC, false);
            this.enabled = true;
        }
        @Option("creeper.log-as-player")
        public boolean logAsPlayer = false;

        @Override
        public String getName()
        {
            return "explosion";
        }
    }
}