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
package de.cubeisland.engine.log.action.block;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.PistonExtensionMaterial;

import de.cubeisland.engine.core.bukkit.EventManager;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;
import de.cubeisland.engine.log.action.block.player.PlayerBlockGrow;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerBlockBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerSignBreak;
import de.cubeisland.engine.log.action.hanging.HangingPreBreakEvent;
import de.cubeisland.engine.log.action.hanging.HangingBreak;

import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.DOWN;
import static org.bukkit.block.BlockFace.UP;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockFadeEvent}
 * {@link BlockBurnEvent}
 * {@link BlockFormEvent}
 * {@link BlockSpreadEvent}
 * {@link LeavesDecayEvent}
 * {@link StructureGrowEvent}
 * {@link BlockPistonExtendEvent}
 * {@link BlockPistonRetractEvent}
 * <p>All Actions:
 * {@link BlockBurn}
 * {@link BlockFade}
 * {@link BlockForm}
 * {@link BlockSpread}
 * {@link BlockDecay}
 * {@link BlockGrow}
 * {@link BlockShift}
 * {@link BlockFall}
 * <p>External Actions:
 * {@link de.cubeisland.engine.log.action.block.player.PlayerBlockGrow}
 * {@link de.cubeisland.engine.log.action.block.player.destroy.PlayerBlockBreak}
 * {@link PlayerSignBreak}
 */
public class ListenerBlock extends LogListener
{
    private static final PistonExtensionMaterial PISTON_HEAD = new PistonExtensionMaterial(PISTON_EXTENSION);
    private final Map<Location, ActionBlock> plannedFall = new HashMap<>();
    private final Map<Location, ActionBlock> plannedPyhsics = new HashMap<>();
    private volatile boolean clearPlanned = false;

    public ListenerBlock(Log module)
    {
        super(module, BlockBreak.class, BlockFade.class,
              BlockForm.class, BlockSpread.class, BlockDecay.class, BlockGrow.class, BlockShift.class,
              PlayerBlockGrow.class, PlayerBlockBreak.class, PlayerSignBreak.class, BlockFall.class);
    }

    public static void logAttachedBlocks(LogListener ll, EventManager em, Block block, ActionBlock action)
    {
        if (!block.getType().isSolid() && !(block.getType() == SUGAR_CANE_BLOCK))
        {
            return; // cannot have attached
        }

        for (Block aBlock : BlockUtil.getAttachedBlocks(block))
        {
            em.fireEvent(new BlockPreBreakEvent(aBlock.getLocation(), action));
        }
        for (Block dBlock : BlockUtil.getDetachableBlocksOnTop(block))
        {
            if (isNotUpperDoorHalf(dBlock)) // ignore upper door halfs
            {
                em.fireEvent(new BlockPreBreakEvent(dBlock.getLocation(), action));
            }
        }

        if (ll.isActive(HangingBreak.class, block.getWorld()))
        {
            Location location = block.getLocation();
            Location entityLocation = block.getLocation();
            for (Entity entity : block.getChunk().getEntities())
            {
                if (entity instanceof Hanging && location.distanceSquared(entity.getLocation(entityLocation)) < 4)
                {
                    em.fireEvent(new HangingPreBreakEvent(entityLocation, action));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isNotUpperDoorHalf(Block block)
    {
        return block.getData() < 8 || !(block.getType() == WOODEN_DOOR || block.getType() == IRON_DOOR_BLOCK);
    }

    public static void logFallingBlocks(LogListener ll, EventManager em, Block block, ActionBlock action)
    {
        // Falling Blocks
        if (ll.isActive(BlockFall.class, block.getWorld()))
        {
            Block relative = block.getRelative(UP);
            if (relative.getType().hasGravity() || relative.getType() == DRAGON_EGG)
            {
                em.fireEvent(new BlockPreFallEvent(relative.getLocation(), action));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        this.setAndLog(BlockBurn.class, event.getBlock().getState(), AIR);
        logAttachedBlocks(this, this.module.getCore().getEventManager(), event.getBlock(), null);
        logFallingBlocks(this, this.module.getCore().getEventManager(), event.getBlock(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        if (this.getConfig(event.getBlock().getWorld()).block.fade.ignore.contains(event.getBlock().getType()))
        {
            return;
        }
        this.setAndLog(BlockFade.class, event.getBlock().getState(), AIR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if (event instanceof EntityBlockFormEvent)
        {
            return;
        }
        this.setAndLog(BlockForm.class, event.getBlock().getState(), event.getNewState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (event.getNewState().getType() != FIRE)
        {
            this.setAndLog(BlockSpread.class, event.getBlock().getState(), event.getNewState());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        this.setAndLog(BlockDecay.class, event.getBlock().getState(), AIR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        if (event.getPlayer() == null)
        {
            if (this.isActive(BlockGrow.class, event.getWorld()))
            {
                for (BlockState newState : event.getBlocks())
                {
                    BlockState oldState = newState.getBlock().getState();
                    if (oldState.getData().equals(newState.getData()))
                    {
                        continue;
                    }
                    this.setAndLog(BlockGrow.class, oldState, newState);
                }
            }
        }
        else
        {
            if (this.isActive(PlayerBlockGrow.class, event.getWorld()))
            {
                for (BlockState newState : event.getBlocks())
                {
                    BlockState oldState = newState.getBlock().getState();
                    if (oldState.getData().equals(newState.getData()))
                    {
                        continue;
                    }
                    PlayerBlockGrow action = this.set(PlayerBlockGrow.class, oldState, newState);
                    action.setPlayer(event.getPlayer());
                    this.logAction(action);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event)
    {
        if (this.isActive(BlockShift.class, event.getBlock().getWorld()))
        {
            boolean first = true;
            if (event.getBlocks().isEmpty())
            {
                PistonExtensionMaterial pistonHead = PISTON_HEAD.clone();
                pistonHead.setSticky(event.isSticky());
                pistonHead.setFacingDirection(event.getDirection());

                Block firstBlock = event.getBlock().getRelative(event.getDirection());
                BlockState newState = firstBlock.getState();
                newState.setType(pistonHead.getItemType());
                newState.setData(pistonHead);

                BlockShift action = this.set(BlockShift.class, firstBlock.getState(), newState);
                action.setPush();
                this.logAction(action);
                return;
            }
            for (Block block : event.getBlocks())
            {
                BlockState state = block.getState();
                if (first)
                {
                    first = false;
                    PistonExtensionMaterial pistonHead = PISTON_HEAD.clone();
                    pistonHead.setSticky(event.isSticky());
                    pistonHead.setFacingDirection(event.getDirection());
                    BlockState newState = block.getState();
                    newState.setType(pistonHead.getItemType());
                    newState.setData(pistonHead);
                    BlockShift action = this.set(BlockShift.class, state, newState);
                    action.setPush();
                    this.logAction(action);
                }
                if (block.getType() == AIR)
                {
                    continue;
                }

                BlockState oldState = block.getRelative(event.getDirection()).getState();
                BlockState newState = oldState.getBlock().getState();
                newState.setType(state.getType());
                newState.setData(state.getData());
                BlockShift action = this.set(BlockShift.class, oldState, newState);
                action.setPush();
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event)
    {
        if (this.isActive(BlockShift.class, event.getBlock().getWorld()))
        {
            BlockState retractedBlock = event.getRetractLocation().getBlock().getState();
            BlockState pistonHead = event.getBlock().getRelative(event.getDirection()).getState();

            BlockState newPistonHead = pistonHead.getBlock().getState();
            if (retractedBlock.getType().isSolid())
            {
                newPistonHead.setType(retractedBlock.getType());
                newPistonHead.setData(retractedBlock.getData());
            }
            else
            {
                newPistonHead.setType(AIR);
            }

            BlockShift action = this.set(BlockShift.class, pistonHead, newPistonHead);
            action.setRetract();
            this.logAction(action);

            if (event.isSticky() && retractedBlock.getType().isSolid()) // TODO missing retractable blocks?
            {
                action = this.set(BlockShift.class, retractedBlock, null);
                action.setNewBlock(AIR);
                action.setRetract();
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFall(final BlockPhysicsEvent event)
    {
        if (!this.isActive(BlockFall.class, event.getBlock().getWorld()))
        {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (state.getType().hasGravity() || state.getType() == DRAGON_EGG)
        {
            if (event.getBlock().getRelative(DOWN).getType() == AIR)
            {
                Location loc = state.getLocation();

                BlockFall action = this.set(BlockFall.class, state, null);
                ActionBlock cause = this.plannedFall.remove(loc);
                if (cause instanceof ActionPlayerBlock)
                {
                    action.cause = this.reference((ActionPlayerBlock)cause);
                }
                action.setNewBlock(AIR);
                this.logAction(action);
                Block onTop = state.getBlock().getRelative(UP);
                if (onTop.getType().hasGravity() || onTop.getType() == DRAGON_EGG)
                {
                    this.preplanBlockFall(new BlockPreFallEvent(onTop.getLocation(), cause));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysicsBreak(final BlockPhysicsEvent event)
    {
        BlockState oldState = event.getBlock().getState();
        Block blockAttachedTo;
        if (oldState.getData() instanceof Attachable)
        {
            Attachable attachable = (Attachable)oldState.getData();
            if (attachable.getAttachedFace() == null)
            {
                return; // is not attached !?
            }
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
        if (blockAttachedTo == null)
        {
            return;
        }
        if (!blockAttachedTo.getType().isSolid())
        {
            Location loc = oldState.getLocation();
            ActionBlock cause = this.plannedPyhsics.remove(loc);
            oldState = adjustBlockForDoubleBlocks(oldState);
            if (cause instanceof ActionPlayerBlock)
            {
                PlayerBlockBreak action;
                if (oldState instanceof Sign)
                {
                    action = this.set(PlayerSignBreak.class, oldState, null);
                    if (action != null)
                    {
                        ((PlayerSignBreak)action).setLines(((Sign)oldState).getLines());
                    }
                }
                else
                {
                    action = this.set(PlayerBlockBreak.class, oldState, null);
                }
                if (action != null)
                {
                    action.setNewBlock(AIR);
                    action.player = ((ActionPlayerBlock)cause).player;
                    action.reference = this.reference(cause);
                    this.logAction(action);
                }
            }
            else
            {
                BlockBreak action;
                if (oldState instanceof Sign)
                {
                    action = this.set(SignBreak.class, oldState, null);
                    if (action != null)
                    {
                        ((SignBreak)action).setLines(((Sign)oldState).getLines());
                    }
                }
                else
                {
                    action = this.set(BlockBreak.class, oldState, null);
                }
                if (action != null)
                {
                    action.setNewBlock(AIR);
                    this.logAction(action);
                }
            }
        }
    }

    private void setAndLog(Class<? extends ActionBlock> clazz, BlockState oldState, Material newMaterial)
    {
        BlockState state = adjustBlockForDoubleBlocks(oldState);
        ActionBlock action = this.set(clazz, state, null);
        if (action != null)
        {
            action.setNewBlock(newMaterial);
            this.logAction(action);
        }
    }

    private void setAndLog(Class<? extends ActionBlock> clazz, BlockState oldState, BlockState newState)
    {
        BlockState state = adjustBlockForDoubleBlocks(oldState);
        ActionBlock action = this.set(clazz, state, newState);
        if (action != null)
        {
            this.logAction(action);
        }
    }

    private <T extends ActionBlock> T set(Class<T> clazz, BlockState oldState, BlockState newState)
    {
        T action = this.newAction(clazz, oldState.getWorld());
        if (action != null)
        {
            action.setLocation(oldState.getLocation());
            action.setOldBlock(oldState);
            if (newState != null)
            {
                action.setNewBlock(newState);
            }
        }
        return action;
    }

    @EventHandler
    public void preplanBlockFall(final BlockPreFallEvent event)
    {
        plannedFall.put(event.getLocation(), event.getAction());
        this.module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
        {
            @Override
            public void run()
            {
                plannedFall.remove(event.getLocation());
            }
        }, 3);
    }

    @EventHandler
    public void preplanBlockPhyiscs(BlockPreBreakEvent event)
    {
        plannedPyhsics.put(event.getLocation(), event.getAction());
        if (!clearPlanned)
        {
            clearPlanned = true;
            this.module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    plannedPyhsics.clear();
                }
            });
        }
    }
}
