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
package de.cubeisland.engine.log.action.logaction.block.explosion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType.BlockData;

import static org.bukkit.Material.AIR;

/**
 * Container-ActionType for explosions
 * <p>Events: {@link EntityExplodeEvent}</p>
 * <p>External Actions:
 * {@link TntExplode},
 * {@link CreeperExplode},
 * {@link FireballExplode},
 * {@link EnderdragonExplode},
 * {@link WitherExplode},
 * {@link EntityExplode},
 */
public class ExplodeActionType extends ActionTypeContainer
{
    public ExplodeActionType()
    {
        super("EXPLOSION");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        Entity entity = event.getEntity();
        if (entity == null)
        {
            return;
        }
        BlockActionType actionType;
        Player player = null;
        if (entity instanceof TNTPrimed)
        {
            actionType = this.manager.getActionType(TntExplode.class);
            Entity source = ((TNTPrimed)entity).getSource();
            if (source != null && source instanceof Player)
            {
                  player = (Player)source;
            }
        }
        else if (entity instanceof Creeper)
        {
            actionType = this.manager.getActionType(CreeperExplode.class);
            Entity target = ((Creeper)entity).getTarget();
            player = target instanceof Player ? ((Player)target) : null;
        }
        else if (entity instanceof Fireball)
        {
            actionType = this.manager.getActionType(FireballExplode.class);
            LivingEntity shooter = ((Fireball)entity).getShooter();
            LivingEntity target = BukkitUtils.getTarget(shooter);
            if (target != null && target instanceof Player)
            {
                player = (Player)target;
            }
        }
        else if (entity instanceof EnderDragon)
        {
            actionType = this.manager.getActionType(EnderdragonExplode.class);
            EnderDragon dragon = (EnderDragon)entity;
            LivingEntity target = BukkitUtils.getTarget(dragon);
            if (target != null && target instanceof Player)
            {
                player = (Player)target;
            }
        }
        else if (entity instanceof WitherSkull)
        {
            actionType = this.manager.getActionType(WitherExplode.class);
            if (((WitherSkull)entity).getShooter() instanceof Wither)
            {
                LivingEntity target = ((Wither)((WitherSkull)entity).getShooter()).getTarget();
                if (target instanceof Player)
                {
                    player = (Player)target;
                }
            }
        }
        else
        {
            actionType = this.manager.getActionType(EntityExplode.class);
        }
        if (actionType.isActive(entity.getWorld()))
        {
            for (Block block : event.blockList())
            {
                if ((block.getType().equals(Material.WOODEN_DOOR)
                    || block.getType().equals(Material.IRON_DOOR_BLOCK))
                    && block.getData() >= 8)
                {
                    continue; // ignore upper door_halfs
                }
                actionType.logBlockChange(block.getLocation(),player, BlockData.of(block.getState()),AIR,null);
                actionType.logAttachedBlocks(block.getState(), player);
                actionType.logFallingBlocks(block.getState(), player);
            }
        }
    }
}
