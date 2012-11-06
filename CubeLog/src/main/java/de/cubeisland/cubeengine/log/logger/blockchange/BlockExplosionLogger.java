package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.logger.SubLogConfig;
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
        /*
         * Explosion.ExplosionConfig c = this.getConfiguration();
         * if ((event.getEntity() == null &&
         * !c.actions.get(LogAction.EXPLOSION_MISC.toString()))
         * || (event.getEntity() instanceof TNTPrimed &&
         * !c.actions.get(LogAction.EXPLOSION_TNT.toString()))
         * || (event.getEntity() instanceof Creeper &&
         * !c.actions.get(LogAction.EXPLOSION_CREEPER.toString()))
         * || (event.getEntity() instanceof Fireball &&
         * !c.actions.get(LogAction.EXPLOSION_GHASTFIREBALL.toString()))
         * || (event.getEntity() instanceof EnderDragon &&
         * !c.actions.get(LogAction.EXPLOSION_ENDERDRAGON.toString())))
         *
         * // TODO dont forget to check this later on||
         * (!c.actions.get(LogAction.EXPLOSION_MISC))
         * {
         * return;
         * } */
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

        @Override
        public String getName()
        {
            return "block-explode";
        }
    }
}
