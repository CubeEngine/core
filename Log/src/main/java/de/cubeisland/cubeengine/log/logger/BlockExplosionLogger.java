package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockExplosionConfig;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockExplosionLogger extends BlockLogger<BlockExplosionConfig>
{
    public BlockExplosionLogger(Log module)
    {
        super(module, BlockExplosionConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        World world = event.getEntity().getWorld();
        BlockExplosionConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (event.getEntity() instanceof TNTPrimed && !config.logTNT)
            {
                return;
            }
            else if (event.getEntity() instanceof Creeper && !config.logCreeper)
            {
                return;
            }
            else if (event.getEntity() instanceof Fireball && !config.logFireball)
            {
                return;
            }
            else if (event.getEntity() instanceof EnderDragon && !config.logDragon)
            {
                return;
            }
            else if (event.getEntity() instanceof WitherSkull && !config.logWither)
            {
                return;
            }
            else if (!config.logMisc)
            {
                return;
            }
            Entity entity = event.getEntity();
            Player player = null;
            if (config.logCreeperAsPlayer)
            {
                if (entity.getType().equals(EntityType.CREEPER))
                {
                    final Entity target = ((Creeper)entity).getTarget();
                    player = target instanceof Player ? ((Player)target) : null;
                }
            }
            for (Block block : event.blockList())
            {
                this.logBlockChange(BlockChangeCause.EXPLOSION, world, player, block.getState(), null);
            }
        }
    }

}
