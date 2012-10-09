package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author Anselm Brehme
 */
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
        if ((event.getEntity() == null && !c.actions.get(LogAction.EXPLOSION_MISC))
                || (event.getEntity() instanceof TNTPrimed && !c.actions.get(LogAction.EXPLOSION_TNT))
                || (event.getEntity() instanceof Creeper && !c.actions.get(LogAction.EXPLOSION_CREEPER))
                || (event.getEntity() instanceof Fireball && !c.actions.get(LogAction.EXPLOSION_GHASTFIREBALL))
                || (event.getEntity() instanceof EnderDragon && !c.actions.get(LogAction.EXPLOSION_ENDERDRAGON)))
        // TODO dont forget to check this later on|| (!c.actions.get(LogAction.EXPLOSION_MISC))
        {
            return;
        }
        this.lm.logExplosion(event.blockList(), event.getEntity());
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
        @Option(value="actions",genericType=Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "explosion";
        }
    }
}