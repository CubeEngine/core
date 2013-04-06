package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.ItemDrop;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENVIRONEMENT;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;
import static org.bukkit.Material.*;

/**
 * Blocks broken by a player directly OR blocks broken indirectly.
 * <p>Events: {@link BlockBreakEvent}, {@link BlockPhysicsEvent}</p>
 * <p>External Actions: {@link ItemDrop} when Breaking InventoryHolder,
 * {@link BlockActionType#logAttachedBlocks BlockBreak and HangingBreak} when attached Blocks will fall
 * {@link BlockActionType#logFallingBlocks BlockFall} when relative Blocks will fall
 */
public class BlockBreak extends BlockActionType
{
    public BlockBreak(Log module)
    {
        super(module, "block-break", BLOCK, PLAYER, ENVIRONEMENT);
    }

    //OldBlockData NoteBlock: RawNote
    //Json: (sign)(playing)
    //Doors / Beds only logged bottom / feet
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!this.isActive(event.getBlock().getWorld())) return;
        BlockState blockState = event.getBlock().getState();
        if (blockState.getType().equals(AIR))
        {
            return; // breaking air !? -> no logging
        }
        ObjectNode json = null;
        BlockData blockData = BlockData.of(blockState);
        if (blockState instanceof NoteBlock) // adjust data (which is always 0 to note)
        {
            blockData.data = ((NoteBlock)blockState).getRawNote();
        }
        else if (blockState instanceof Sign)
        {
            json = this.om.createObjectNode();
            ArrayNode sign = json.putArray("sign");
            for (String line :  ((Sign)blockState).getLines())
            {
                sign.add(line);
            }
        }
        else if (blockState instanceof Jukebox)
        {
            json = this.om.createObjectNode();
            json.put("playing", ((Jukebox)blockState).getPlaying().name());
        }
        else if (blockState instanceof InventoryHolder)
        {
            ItemDrop itemDrop = this.manager.getActionType(ItemDrop.class);
            Location location = blockState.getLocation();
            if (itemDrop.isActive(location.getWorld()))
            {
                itemDrop.logDropsFromChest((InventoryHolder)blockState,location,event.getPlayer());
            }
        }
        else
        {
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
        }
        this.logBlockChange(blockState.getLocation(),event.getPlayer(),blockData,AIR,json == null ? null : json.toString());
        if (blockState.getType().equals(OBSIDIAN)) // portal?
        {
            Block block = blockState.getBlock();
            for (BlockFace face : BLOCK_FACES)
            {
                if (block.getRelative(face).getType().equals(PORTAL))
                {
                    Block portal = block.getRelative(face);
                    this.logBlockChange(portal.getLocation(),event.getPlayer(),BlockData.of(portal.getState()),AIR,null);
                    break;
                }
            }
        }
        this.logAttachedBlocks(blockState, event.getPlayer());
        this.logFallingBlocks(blockState, event.getPlayer());
    }

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
            blockAttachedTo = event.getBlock().getRelative(BlockFace.DOWN);
        }
        if (blockAttachedTo != null && !blockAttachedTo.getType().isSolid())
        {
            Location loc = oldState.getLocation();
            Pair<Entity,BlockActionType> cause = this.plannedPyhsics.remove(loc);
            if (cause != null)
            {
                ObjectNode json = this.om.createObjectNode();
                json.put("break-cause", cause.getRight().getID());
                if (oldState instanceof Sign)
                {
                    ArrayNode sign = json.putArray("sign");
                    for (String line : ((Sign)oldState).getLines())
                    {
                        sign.add(line);
                    }
                }
                this.logBlockChange(loc, cause.getLeft(), oldData, AIR, json.toString());
            }
            else
            {
                System.out.print("Unplanned BlockPhysicsEvent! (BlockBreak) "+oldState.getType().name()); //TODO remove
            }
        }
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Pair<Entity,BlockActionType>> plannedPyhsics = new ConcurrentHashMap<Location, Pair<Entity, BlockActionType>>();
    public void preplanBlockPhyiscs(Location location, Entity player, BlockActionType reason)
    {
        plannedPyhsics.put(location,new Pair<Entity, BlockActionType>(player,reason));
        if (!clearPlanned)
        {
            clearPlanned = true;
            BlockBreak.this.logModule.getCore().getTaskManager().scheduleSyncDelayedTask(logModule, new Runnable() {
                @Override
                public void run() {
                    clearPlanned = false;
                    BlockBreak.this.plannedPyhsics.clear();
                }
            });
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = 1+logEntry.getAttached().size();
            user.sendTranslated("%s&2%s &abroke &6%dx %s&a%s!",
                                time,
                                logEntry.getCauserUser().getDisplayName(),
                                amount,
                                logEntry.getOldBlock(),
                                loc);
        }
        else {
            user.sendTranslated("%s&2%s &abroke &6%s&a%s!",
                                time,
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),
                                loc);
        }
    }


}
