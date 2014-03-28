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
package de.cubeisland.engine.log.action.newaction.block.player.destroy;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking a block
 * <p>SubTypes:
 * {@link PlayerNoteBlockBreak}
 * {@link PlayerSignBreak}
 * {@link PlayerJukeboxBreak}
 * {@link PlayerContainerBreak}
 */
public class PlayerBlockBreak extends PlayerBlockActionType<PlayerBlockListener>
{
    // return "block-break";
    // return this.lm.getConfig(world).block.BLOCK_BREAK_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerBlockBreak
            && this.player.equals(((PlayerBlockBreak)action).player)
            && ((PlayerBlockBreak)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} broke {name#block} x{amount}",
                                       this.player.name, this.oldBlock.name(), this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} broke {name#block}", this.player.name, this.oldBlock.name());
    }

    // TODO physics
    /*

 **
 * Blocks broken by a player directly OR blocks broken indirectly.
 * <p>Events: {@link BlockBreakEvent}, {@link BlockPhysicsEvent}</p>
 * <p>External Actions: {@link ItemDrop} when Breaking InventoryHolder,
 * {@link BlockActionType#logAttachedBlocks BlockBreak and HangingBreak} when attached Blocks will fall
 * {@link BlockActionType#logFallingBlocks BlockFall} when relative Blocks will fall
 *

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        if (!this.isActive(event.getBlock().getWorld())) return;
        BlockState oldState = event.getBlock().getState();
        BlockData oldData = BlockData.of(oldState);
        Block blockAttachedTo;
        if (oldState.getData() instanceof Attachable)
        {
            Attachable attachable = (Attachable) oldState.getData();
            if (attachable.getAttachedFace() == null) return; // is not attached !?
            blockAttachedTo = event.getBlock().getRelative(attachable.getAttachedFace());
        }
        else // block on bottom missing
        {
            if (!BlockUtil.isDetachableFromBelow(oldState.getType()))
            {
                return;
            }
            blockAttachedTo = event.getBlock().getRelative(BlockFace.DOWN);
        }
        if (blockAttachedTo == null) return;
        if (!blockAttachedTo.getType().isSolid())
        {
            Location loc = oldState.getLocation();
            Pair<Entity,BlockActionType> cause = this.plannedPyhsics.remove(loc);
            if (cause != null)
            {
                oldState = this.adjustBlockForDoubleBlocks(oldState);
                oldData.data = oldState.getRawData();

                if (oldState instanceof Sign)
                {
                    ObjectNode json = this.om.createObjectNode();
                    ArrayNode sign = json.putArray("oldSign");
                    for (String line : ((Sign)oldState).getLines())
                    {
                        sign.add(line);
                    }
                    cause.getRight().logBlockChange(loc, cause.getLeft(), oldData, AIR, json.toString());
                    return;
                }
                cause.getRight().logBlockChange(loc, cause.getLeft(), oldData, AIR, null);
            }
        }
    }

    private volatile boolean clearPlanned = false;
    private final Map<Location,Pair<Entity,BlockActionType>> plannedPyhsics = new ConcurrentHashMap<>();
    public void preplanBlockPhyiscs(Location location, Entity player, BlockActionType reason)
    {
        plannedPyhsics.put(location,new Pair<>(player,reason));
        if (!clearPlanned)
        {
            clearPlanned = true;
            BlockBreak.this.module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    BlockBreak.this.plannedPyhsics.clear();
                }
            });
        }
    }
     */
}
