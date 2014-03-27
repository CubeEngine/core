package de.cubeisland.engine.log.action.newaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.projectiles.ProjectileSource;

import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static org.bukkit.Material.FIRE;

/**
 * A Listener for PlayerHanging Actions
 * <p>Events:
 * {@link BlockIgniteEvent}
 * {@link BlockSpreadEvent}
 * <p>Actions:
 * {@link FireballIgnite}
 * {@link FireSpread}
 * {@link LavaIgnite}
 * {@link LightningIgnite}
 * {@link LighterIgnite}
 * {@link OtherIgnite}
 */
public class BlockIgniteListener extends LogListener
{
    public BlockIgniteListener(Module module)
    {
        super(module);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        BlockState oldState = event.getBlock().getState();
        BlockActionType action;
        switch (event.getCause())
        {
        case FIREBALL:
            action = this.newAction(FireballIgnite.class, oldState.getWorld());
            if (action != null)
            {
                ProjectileSource shooter = ((Fireball)event.getIgnitingEntity()).getShooter();
                if (shooter instanceof Entity)
                {
                    ((FireballIgnite)action).setShooter((Entity)shooter);
                    if (shooter instanceof Ghast)
                    {
                        LivingEntity target = BukkitUtils.getTarget((Ghast)shooter);
                        if (target instanceof Player)
                        {
                            ((FireballIgnite)action).setPlayer((Player)target);
                        }
                    }
                    else if (shooter instanceof Player)
                    {
                        ((FireballIgnite)action).setPlayer((Player)shooter);
                    }
                }

            }
            break;
        case LAVA:
            action = this.newAction(LavaIgnite.class, oldState.getWorld());
            if (action != null)
            {
                ((LavaIgnite)action).setSource(event.getIgnitingBlock().getLocation());
            }
            break;
        case LIGHTNING:
            action = this.newAction(LightningIgnite.class, oldState.getWorld());
            break;
        case FLINT_AND_STEEL:
            action = this.newAction(LighterIgnite.class, oldState.getWorld());
            if (event.getPlayer() != null)
            {
                ((LighterIgnite)action).setPlayer(event.getPlayer());
            }
            break;
        case ENDER_CRYSTAL:
        case EXPLOSION:
            action = this.newAction(OtherIgnite.class, oldState.getWorld());
            break;
        default:
            return;
        }
        if (action != null)
        {
            action.setOldBlock(oldState);
            action.setNewBlock(FIRE);
            action.setLocation(oldState.getLocation());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (event.getNewState().getType().equals(Material.FIRE))
        {
            Block oldBlock = event.getBlock();
            FireSpread action = this.newAction(FireSpread.class, oldBlock.getWorld());
            if (action != null)
            {
                action.setOldBlock(oldBlock.getState());
                action.setNewBlock(event.getNewState());
                action.setLocation(oldBlock.getLocation());
                action.setSource(event.getSource().getLocation());
            }
        }
    }
}
