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
package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.cubeisland.cubeengine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType.BlockData;

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
        BlockActionType actionType;
        Player player = null;
        if (event.getEntity() instanceof TNTPrimed)
        {
            actionType = this.manager.getActionType(TntExplode.class);
            //((TNTPrimed)event.getEntity()).getSource()
            //TODO get player who ignited if found
        }
        else if (event.getEntity() instanceof Creeper)
        {
            actionType = this.manager.getActionType(CreeperExplode.class);
            Entity target = ((Creeper)event.getEntity()).getTarget();
            player = target instanceof Player ? ((Player)target) : null;
        }
        else if (event.getEntity() instanceof Fireball)
        {
            actionType = this.manager.getActionType(FireballExplode.class);
            //TODO get shooter if shooter is attacking player log player too
        }
        else if (event.getEntity() instanceof EnderDragon)
        {
            //TODO if is attacking player log player too
            actionType = this.manager.getActionType(EnderdragonExplode.class);
        }
        else if (event.getEntity() instanceof WitherSkull)
        {
            //TODO if is attacking player log player too
            actionType = this.manager.getActionType(WitherExplode.class);
        }
        else
        {
            actionType = this.manager.getActionType(EntityExplode.class);
        }
        if (actionType.isActive(event.getEntity().getWorld()))
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
