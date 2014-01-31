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
package de.cubeisland.engine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType.BlockData;

/**
 * Container-ActionType for ignitions
 * <p>Events: {@link BlockIgniteEvent}</p>
 * <p>External Actions:
 * {@link FireballIgnite},
 * {@link LavaIgnite},
 * {@link LightningIgnite},
 * {@link Lighter},
 * {@link OtherIgnite},
 */
public class IgniteActionType extends ActionTypeContainer
{
    public IgniteActionType()
    {
        super("IGNITE");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        BlockState blockState = event.getBlock().getState();
        switch (event.getCause())
        {
        case FIREBALL:
            if (((Fireball)event.getIgnitingEntity()).getShooter() instanceof LivingEntity)
            {
                this.logIgnite(this.manager.getActionType(FireballIgnite.class),blockState,
                               BukkitUtils.getTarget((LivingEntity)((Fireball)event.getIgnitingEntity()).getShooter()));
            }
            else {
                // TODO other shooter
            }
            break;
        case LAVA:
            this.logIgnite(this.manager.getActionType(LavaIgnite.class),blockState,null);
            break;
        case LIGHTNING:
            this.logIgnite(this.manager.getActionType(LightningIgnite.class),blockState,null);
            break;
        case FLINT_AND_STEEL:
            this.logIgnite(this.manager.getActionType(Lighter.class),blockState,event.getPlayer());
            break;
        case ENDER_CRYSTAL:
        case EXPLOSION:
            this.logIgnite(this.manager.getActionType(OtherIgnite.class),blockState,null);
            break;
        }
    }

    public void logIgnite(BlockActionType igniteType, BlockState state, Entity causer)
    {
        if (igniteType.isActive(state.getWorld()))
        {
            BlockData data = BlockData.of(state);
            data.material = Material.FIRE;
            igniteType.logBlockChange(state.getLocation(),causer,BlockData.of(state),data,null);
        }
    }
}
