package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockExplosionLogger extends BlockLogger<BlockExplosionLogger.BlockExplodeConfig>
{
    public BlockExplosionLogger()
    {
        this.config = new BlockExplodeConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.getEntity() instanceof TNTPrimed && !this.config.logTNT)
        {
            return;
        }
        else if (event.getEntity() instanceof Creeper && !this.config.logCreeper)
        {
            return;
        }
        else if (event.getEntity() instanceof Fireball && !this.config.logFireball)
        {
            return;
        }
        else if (event.getEntity() instanceof EnderDragon && !this.config.logDragon)
        {
            return;
        }
        else if (event.getEntity() instanceof WitherSkull && !this.config.logWither)
        {
            return;
        }
        else if (!this.config.logMisc)
        {
            return;
        }
        Entity entity = event.getEntity();
        Player player = null;
        if (this.getConfig().logCreeperAsPlayer)
        {
            if (entity.getType().equals(EntityType.CREEPER))
            {
                final Entity target = ((Creeper)entity).getTarget();
                player = target instanceof Player ? ((Player)target) : null;
            }
        }
        for (Block block : event.blockList())
        {
            if (player == null)
            {
                this.logBlockChange(BlockChangeCause.EXPLOSION, player, block.getState(), null);
            }
            else
            {
                this.logBlockChange(BlockChangeCause.PLAYER, player, block.getState(), null); //TODO this is not ideal ! information about explosion is gone :(
            }
        }
    }

    public static class BlockExplodeConfig extends SubLogConfig
    {
        public BlockExplodeConfig()
        {
            this.enabled = true;
        }
        @Option(value = "log-creeper-as-player-who-triggered")
        public boolean logCreeperAsPlayer;
        @Option(value = "log-explosion-type.misc")
        public boolean logMisc;
        @Option(value = "log-explosion-type.creeper")
        public boolean logCreeper;
        @Option(value = "log-explosion-type.tnt")
        public boolean logTNT;
        @Option(value = "log-explosion-type.ender-dragon")
        public boolean logDragon;
        @Option(value = "log-explosion-type.wither")
        public boolean logWither;
        @Option(value = "log-explosion-type.ghast-fireball")
        public boolean logFireball;

        @Override
        public String getName()
        {
            return "block-explode";
        }
    }
}