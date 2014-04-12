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
package de.cubeisland.engine.log.action.block.ignite;

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
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.ActionBlock;

import static org.bukkit.Material.FIRE;

/**
 * A Listener for {@link ActionBlockIgnite} Actions
 * <p>Events:
 * {@link BlockIgniteEvent}
 * {@link BlockSpreadEvent}
 * <p>All Actions:
 * {@link IgniteFireball}
 * {@link IgniteLava}
 * {@link IgniteLightning}
 * {@link IgniteLighter}
 * {@link IgniteOther}
 * {@link IgniteSpread}
 */
public class ListenerBlockIgnite extends LogListener
{
    public ListenerBlockIgnite(Log module)
    {
        super(module, IgniteFireball.class, IgniteLava.class, IgniteLighter.class, IgniteLighter.class,
              IgniteOther.class, IgniteSpread.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        BlockState oldState = event.getBlock().getState();
        ActionBlock action;
        switch (event.getCause())
        {
        case FIREBALL:
            action = this.newAction(IgniteFireball.class, oldState.getWorld());
            if (action != null)
            {
                ProjectileSource shooter = ((Fireball)event.getIgnitingEntity()).getShooter();
                if (shooter instanceof Entity)
                {
                    ((IgniteFireball)action).setShooter((Entity)shooter);
                    if (shooter instanceof Ghast)
                    {
                        LivingEntity target = BukkitUtils.getTarget((Ghast)shooter);
                        if (target instanceof Player)
                        {
                            ((IgniteFireball)action).setPlayer((Player)target);
                        }
                    }
                    else if (shooter instanceof Player)
                    {
                        ((IgniteFireball)action).setPlayer((Player)shooter);
                    }
                }
            }
            break;
        case LAVA:
            action = this.newAction(IgniteLava.class, oldState.getWorld());
            if (action != null)
            {
                ((IgniteLava)action).setSource(event.getIgnitingBlock().getLocation());
            }
            break;
        case LIGHTNING:
            action = this.newAction(IgniteLightning.class, oldState.getWorld());
            break;
        case FLINT_AND_STEEL:
            action = this.newAction(IgniteLighter.class, oldState.getWorld());
            if (event.getPlayer() != null)
            {
                ((IgniteLighter)action).setPlayer(event.getPlayer());
            }
            break;
        case ENDER_CRYSTAL:
        case EXPLOSION:
            action = this.newAction(IgniteOther.class, oldState.getWorld());
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
            IgniteSpread action = this.newAction(IgniteSpread.class, oldBlock.getWorld());
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
