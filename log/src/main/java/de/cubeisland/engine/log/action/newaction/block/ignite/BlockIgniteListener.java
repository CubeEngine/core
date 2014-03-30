/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.newaction.block.ignite;

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
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static org.bukkit.Material.FIRE;

/**
 * A Listener for {@link BlockIgniteAction} Actions
 * <p>Events:
 * {@link BlockIgniteEvent}
 * {@link BlockSpreadEvent}
 * <p>All Actions:
 * {@link FireballIgnite}
 * {@link LavaIgnite}
 * {@link LightningIgnite}
 * {@link LighterIgnite}
 * {@link OtherIgnite}
 * {@link FireSpread}
 */
public class BlockIgniteListener extends LogListener
{
    public BlockIgniteListener(Log module)
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
        case SPREAD:
            return;
        default:
            this.module.getLog().warn("Unknown IgniteCause! {}", event.getCause().name());
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
        if (event.getNewState().getType() == FIRE)
        {
            Block oldBlock = event.getBlock();
            FireSpread action = this.newAction(FireSpread.class, oldBlock.getWorld());
            if (action != null)
            {
                action.setOldBlock(oldBlock.getState());
                action.setNewBlock(event.getNewState());
                action.setLocation(oldBlock.getLocation());
                action.setSource(event.getSource().getLocation());
                this.logAction(action);
            }
        }
    }
}
