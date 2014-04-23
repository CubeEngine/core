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
package de.cubeisland.engine.log.action.block.entity.explosion;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;

import static de.cubeisland.engine.log.action.block.ListenerBlock.logAttachedBlocks;
import static de.cubeisland.engine.log.action.block.ListenerBlock.logFallingBlocks;
import static org.bukkit.Material.AIR;

/**
 * A Listener for {@link ExplosionAction}
 * <p>Events:
 * {@link EntityExplodeEvent}
 * <p>All Actions:
 * {@link ExplodeCreeper}
 * {@link ExplodeTnt}
 * {@link ExplodeWither}
 * {@link ExplodeFireball}
 * {@link ExplodeEnderdragon}
 * {@link ExplodeEntity}
 */
public class ListenerExplode extends LogListener
{
    public ListenerExplode(Log module)
    {
        super(module, ExplodeCreeper.class, ExplodeTnt.class, ExplodeWither.class, ExplodeFireball.class,
              ExplodeEnderdragon.class, ExplodeEntity.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        final List<Block> blocks = event.blockList();
        if (blocks.isEmpty())
        {
            return;
        }

        final Entity entity = event.getEntity();
        final World world;
        if (entity != null)
        {
            world = entity.getWorld();
        }
        else
        {
            world = blocks.get(0).getWorld();
        }

        Player player = null;
        Class<? extends ExplosionAction> actionClazz;
        if (entity instanceof Creeper)
        {
            actionClazz = ExplodeCreeper.class;
            if (((Creeper)entity).getTarget() instanceof Player)
            {
                player = (Player)((Creeper)entity).getTarget();
            }
        }
        else if (entity instanceof TNTPrimed)
        {
            actionClazz = ExplodeTnt.class;
            if (((TNTPrimed)entity).getSource() instanceof Player)
            {
                player = (Player)((TNTPrimed)entity).getSource();
            }
        }
        else if (entity instanceof WitherSkull)
        {
            actionClazz = ExplodeWither.class;
            if (((WitherSkull)entity).getShooter() instanceof Wither
                && ((Wither)((WitherSkull)entity).getShooter()).getTarget() instanceof Player)
            {
                player = (Player)((Wither)((WitherSkull)entity).getShooter()).getTarget();
            }
        }
        else if (entity instanceof Fireball)
        {
            // TODO other shooter than ghast
            actionClazz = ExplodeFireball.class;
            if (((Fireball)entity).getShooter() instanceof Ghast)
            {
                LivingEntity target = BukkitUtils.getTarget((Ghast)((Fireball)entity).getShooter());
                if (target != null && target instanceof Player)
                {
                    player = (Player)target;
                }
            }
        }
        else if (entity instanceof EnderDragon)
        {
            actionClazz = ExplodeEnderdragon.class;
            LivingEntity target = BukkitUtils.getTarget((LivingEntity)entity);
            if (target != null && target instanceof Player)
            {
                player = (Player)target;
            }
        }
        else
        {
            if (this.isActive(ExplodeEntity.class, world))
            {
                for (Block block : blocks)
                {
                    ExplodeEntity eAction = this.newAction(ExplodeEntity.class);
                    eAction.setLocation(block.getLocation());
                    eAction.setEntity(entity);
                    this.logAction(eAction);

                    // TODO attached / falling / ignore blocks exploded
                    logAttachedBlocks(this, module.getCore().getEventManager(), block, eAction);
                    logFallingBlocks(this, module.getCore().getEventManager(), block, eAction);
                }
            }
            return;
        }

        if (this.isActive(actionClazz, world))
        {
            for (Block block : blocks)
            {
                if ((block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR_BLOCK))
                    && block.getData() >= 8)
                {
                    continue; // ignore upper door halves
                }
                ExplosionAction action = this.newAction(actionClazz);
                this.setAndLog(action, entity, block, player);
                // TODO attached / falling / ignore blocks exploded
                logAttachedBlocks(this, module.getCore().getEventManager(), block, action);
                logFallingBlocks(this, module.getCore().getEventManager(), block, action);
            }
        }
    }

    private void setAndLog(ExplosionAction action, Entity entity, Block block, Player player)
    {
        action.setLocation(block.getLocation());
        action.setOldBlock(block.getState());
        action.setNewBlock(AIR);
        action.setEntity(entity);
        action.setPlayer(player);
        this.logAction(action);
    }
}
