package de.cubeisland.cubeengine.log.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Bed;
import org.bukkit.material.Diode;
import org.bukkit.material.Lever;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import static de.cubeisland.cubeengine.core.bukkit.BlockUtil.isNonFluidProofBlock;
import static de.cubeisland.cubeengine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.cubeengine.core.util.BlockUtil.DIRECTIONS;
import static de.cubeisland.cubeengine.log.storage.LogManager.*;
import static de.cubeisland.cubeengine.log.storage.LogManager.FIREBALL;
import static de.cubeisland.cubeengine.log.storage.LogManager.LAVA_BUCKET;
import static de.cubeisland.cubeengine.log.storage.LogManager.WATER_BUCKET;
import static org.bukkit.Material.*;

public class BlockListener implements Listener
{
    private LogManager manager;
    private Log module;

    private Map<Location,Pair<Player,Integer>> plannedFallingBlocks = new HashMap<Location, Pair<Player,Integer>>();

    public BlockListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        BlockState blockState = event.getBlock().getState();
        if (blockState.getType().equals(AIR))
        {
            return; // breaking air !? -> no logging
        }
        if (blockState instanceof Sign)
        {
            String[] lines = ((Sign)blockState).getLines();
            try
            {
                this.logBlockChange(blockState.getLocation(), BLOCK_BREAK, event.getPlayer(), blockState, this.module.getObjectMapper().writeValueAsString(lines));
            }
            catch (JsonProcessingException e)
            {
                throw new IllegalStateException("Could not parse sign-text!",e);
            }
        }
        else if (blockState instanceof NoteBlock)
        {
            this.logBlockChange(blockState.getLocation(), BLOCK_BREAK, event.getPlayer(), blockState, String.valueOf(((NoteBlock) blockState).getRawNote()));
        }
        else if (blockState instanceof Jukebox)
        {
            this.logBlockChange(blockState.getLocation(), BLOCK_BREAK, event.getPlayer(), blockState, ((Jukebox) blockState).getPlaying().name());
        }
        else if (blockState instanceof InventoryHolder)
        {
           this.logItemDropsFromDestroyedContainer((InventoryHolder) blockState, blockState.getLocation(), event.getPlayer());
        }
        else
        {
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
            this.logBlockChange(BLOCK_BREAK,blockState, AIR,event.getPlayer());
        }
        this.logRelatedBlocks(blockState,event.getPlayer(),BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY)
        && !event.getBlockPlaced().getType().equals(Material.STATIONARY_WATER))
        {
            if (this.manager.isIgnored(event.getPlayer().getWorld(),BLOCK_BREAK)) return;
            this.logBlockChange(BLOCK_BREAK,event.getBlock().getRelative(BlockFace.UP).getState(), AIR,event.getPlayer());
        }
        this.logBlockChange(LogManager.BLOCK_PLACE,event.getBlockReplacedState(),event.getBlockPlaced().getState(),event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (event.getNewState().getType().equals(Material.FIRE))
        {
            if (this.manager.isIgnored(event.getBlock().getWorld(),FIRE_SPREAD)) return;
            this.logBlockChange(LogManager.FIRE_SPREAD,event.getBlock().getState(),event.getNewState(),null);
        }
        else
        {
            if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_SPREAD)) return;
            this.logBlockChange(LogManager.BLOCK_SPREAD,event.getBlock().getState(),event.getNewState(),null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_FORM)) return;
        this.logBlockChange(LogManager.BLOCK_FORM,event.getBlock().getState(),event.getNewState(),null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_FADE,event.getBlock().getType())) return;
        this.logBlockChange(LogManager.BLOCK_FADE,event.getBlock().getState(),event.getNewState(),null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),LEAF_DECAY)) return;
        this.logBlockChange(LogManager.LEAF_DECAY,event.getBlock().getState(), AIR,null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if (!this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_BURN))
        {
            BlockState blockState = event.getBlock().getState();
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
            this.logBlockChange(LogManager.BLOCK_BURN,blockState, AIR,null);
        }
        if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_BREAK)) return;
        this.logRelatedBlocks(event.getBlock().getState(),null,BLOCK_BURN);
    }

    private boolean clearPlanned = false;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        BlockState state = event.getBlock().getState();
        if (!clearPlanned)
        {
            clearPlanned = true;
            this.module.getCore().getTaskManager().scheduleSyncDelayedTask(this.module, new Runnable() {
                @Override
                public void run() {
                    clearPlanned = false;
                    BlockListener.this.plannedFallingBlocks.clear();
                }
            });
        }
        if (state.getType().equals(Material.SAND)||state.getType().equals(Material.GRAVEL)||state.getType().equals(Material.ANVIL))
        { // falling blocks
            if (this.manager.isIgnored(state.getWorld(),BLOCK_FALL)) return;
            if (event.getBlock().getRelative(BlockFace.DOWN).getType().equals(AIR))
            {
                Location loc = state.getLocation();
                Pair<Player,Integer> cause = this.plannedFallingBlocks.get(loc);
                if (cause != null)
                {
                    this.logBlockChange(loc, BLOCK_FALL, cause.getLeft(), state, "cause:"+cause.getRight());
                    this.plannedFallingBlocks.remove(loc);
                }
            }
        }
        else // attached block missing
        {
            if (this.manager.isIgnored(state.getWorld(),BLOCK_BREAK)) return;
            if (state.getData() instanceof Attachable)
            {
                Attachable attachable = (Attachable) state.getData();
                if (attachable.getAttachedFace() == null) return;
                Block blockAttachedTo = event.getBlock().getRelative(attachable.getAttachedFace());
                if (blockAttachedTo != null)
                {
                    switch (blockAttachedTo.getType())
                    {
                        case AIR:
                        case FIRE:
                        case WATER:
                        case STATIONARY_WATER:
                        case LAVA:
                        case STATIONARY_LAVA:
                            Location loc = state.getLocation();
                            Pair<Player,Integer> cause = this.plannedFallingBlocks.get(loc);
                            if (cause != null)
                            {
                                try
                                {
                                    String data;
                                    if (state instanceof Sign)
                                    {
                                        data = this.manager.mapper.writeValueAsString(Arrays.asList(cause.getRight(),((Sign) state).getLines()));
                                    }
                                    else
                                    {
                                        data = this.manager.mapper.writeValueAsString(cause.getRight());
                                    }
                                    this.logBlockChange(loc, BLOCK_BREAK, cause.getLeft(), state, data);
                                    this.plannedFallingBlocks.remove(loc);
                                }
                                catch (JsonProcessingException e)
                                {
                                    throw new IllegalStateException("Could not parse data on BlockPhysicsEvent");
                                }
                            }
                        default:
                            return;
                    }

                }
            }
            else // block on bottom missing
            {
                Block blockAttachedTo = event.getBlock().getRelative(BlockFace.DOWN);
                switch (blockAttachedTo.getType())
                {
                    case AIR:
                    case FIRE:
                    case WATER:
                    case STATIONARY_WATER:
                    case LAVA:
                    case STATIONARY_LAVA:
                        Location loc = state.getLocation();
                        Pair<Player,Integer> cause = this.plannedFallingBlocks.get(loc);
                        if (cause != null)
                        {
                            try
                            {
                                String data;
                                if (state instanceof Sign)
                                {
                                    data = this.manager.mapper.writeValueAsString(Arrays.asList(cause.getRight(),((Sign) state).getLines()));
                                }
                                else
                                {
                                    data = this.manager.mapper.writeValueAsString(cause.getRight());
                                }
                                this.logBlockChange(loc, BLOCK_BREAK, cause.getLeft(), state, data);
                                this.plannedFallingBlocks.remove(loc);
                            }
                            catch (JsonProcessingException e)
                            {
                                throw new IllegalStateException("Could not parse data on BlockPhysicsEvent");
                            }
                        }
                    default:
                        return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        BlockState blockState = event.getBlock().getState();
        blockState.setType(Material.FIRE);
        switch (event.getCause())
        {
            case FIREBALL:
                if (this.manager.isIgnored(blockState.getWorld(),FIREBALL)) return;
                this.logBlockChange(FIREBALL,event.getBlock().getState(),blockState,null);
                break;
            case LAVA:
                if (this.manager.isIgnored(blockState.getWorld(),LAVA_IGNITE)) return;
                this.logBlockChange(LAVA_IGNITE,event.getBlock().getState(),blockState,null);
                break;
            case LIGHTNING:
                if (this.manager.isIgnored(blockState.getWorld(),LIGHTNING)) return;
                this.logBlockChange(LIGHTNING,event.getBlock().getState(),blockState,null);
                break;
            case FLINT_AND_STEEL:
                if (this.manager.isIgnored(blockState.getWorld(),LIGHTER)) return;
                this.logBlockChange(LIGHTER,event.getBlock().getState(),blockState,event.getPlayer());
                break;
            case ENDER_CRYSTAL:
            case EXPLOSION:
                if (this.manager.isIgnored(blockState.getWorld(),OTHER_IGNITE)) return;
                this.logBlockChange(OTHER_IGNITE,event.getBlock().getState(),blockState,null);
                break;
            // SPREAD is done is BlockSpreadEvent
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event)
    {
        //TODO check if this is working correctly
        if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_SHIFT)) return;
        boolean first = true;
        for (Block block : event.getBlocks())
        {
            if (block.getType().equals(AIR)) continue;
            BlockState oldState = block.getState();
            BlockState movedTo = block.getRelative(event.getDirection()).getState();
            movedTo.setType(oldState.getType());
            movedTo.setRawData(oldState.getRawData());
            if (first)
            {
                first = false;
                //TODO perhaps newState not Air but orientated pistonextension
                this.logBlockChange(BLOCK_SHIFT,oldState, AIR); // pushing
            }
            this.logBlockChange(BLOCK_SHIFT,movedTo.getBlock().getState(),movedTo, null,null); // pushed
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event)
    {
        //TODO check if this is working correctly
        if (!event.isSticky() || this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_SHIFT)) return;
        BlockState retractingBlock = event.getRetractLocation().getBlock().getState();
        if (retractingBlock.getType().equals(AIR)) return;
        BlockState retractedBlock = event.getBlock().getRelative(event.getDirection()).getState();
        retractedBlock.setType(retractingBlock.getType());
        retractedBlock.setRawData(retractingBlock.getRawData());
        //TODO perhaps newState not Air but orientated pistonextension
        this.logBlockChange(BLOCK_SHIFT,retractingBlock, AIR); // pulling
        this.logBlockChange(BLOCK_SHIFT,retractedBlock.getBlock().getState(),retractedBlock, null,null); // pulled
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event)
    {
        // TODO CE-344 Lava currently impossible to log sometimes
        BlockState toBlock = event.getToBlock().getState();
        final boolean canFlow = toBlock.getType().equals(AIR) || isNonFluidProofBlock(toBlock.getType());
        if (!canFlow)
        {
            return;
        }
        BlockState fromBlock = event.getBlock().getState();
        BlockState newToBlock = event.getToBlock().getState();
        Material fromMat = event.getBlock().getType();
        int action;
        if (fromMat.equals(Material.LAVA) || fromMat.equals(Material.STATIONARY_LAVA))
        {
            if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
            {
                if (event.getFace().equals(BlockFace.DOWN))
                {
                    newToBlock.setType(Material.STONE);
                    newToBlock.setRawData((byte) 0);
                }
                else
                {
                    newToBlock.setType(Material.COBBLESTONE);
                    newToBlock.setRawData((byte) 0);
                }
                action = BLOCK_FORM;
            }
            else if (toBlock.getType().equals(Material.REDSTONE_WIRE) && BlockUtil.isSurroundedByWater(event.getToBlock()))
            {
                newToBlock.setType(Material.OBSIDIAN);
                newToBlock.setRawData((byte)0);
                action = BLOCK_FORM;
            }
            else if (fromBlock.getRawData() <= 4 && BlockUtil.isSurroundedByWater(event.getToBlock()))
            {
                newToBlock.setType(Material.COBBLESTONE);
                newToBlock.setRawData((byte)0);
                action = BLOCK_FORM;
            }
            else if (toBlock.getType().equals(AIR))
            {
                newToBlock.setType(Material.LAVA);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
                action = LAVA_FLOW;
            }
            else
            {
                if (toBlock.getType().equals(Material.LAVA) || toBlock.getType().equals(Material.STATIONARY_LAVA))
                {
                    return; // changing lava-level do not log
                }
                action = LAVA_BREAK;
                newToBlock.setType(Material.LAVA);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            if (this.manager.isIgnored(event.getBlock().getWorld(),action,toBlock.getType())) return;
            this.logBlockChange(action,toBlock,newToBlock,null);
        }
        else if (fromMat.equals(Material.WATER) || fromMat.equals(Material.STATIONARY_WATER))
        {
            if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
            {
                int sources = 0;
                for (BlockFace face : DIRECTIONS)
                {
                    Block nearBlock = event.getToBlock().getRelative(face);
                    if (nearBlock.getType().equals(Material.STATIONARY_WATER) && nearBlock.getData() == 0)
                    {
                        sources++;
                    }
                }
                if (sources >= 2) // created new source block
                {
                    if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_FORM)) return;
                    newToBlock.setType(Material.STATIONARY_WATER);
                    newToBlock.setRawData((byte)0);
                    this.logBlockChange(BLOCK_FORM,toBlock,newToBlock,null);
                }// else only changing water-level do not log
                return;
            }
            else if (newToBlock.getType().equals(Material.LAVA) || newToBlock.getType().equals(Material.STATIONARY_LAVA) && newToBlock.getRawData() <= 2)
            {
                newToBlock.setType(Material.COBBLESTONE);
                newToBlock.setRawData((byte)0);
            }
            else
            {
                for (final BlockFace face : BLOCK_FACES)
                {
                    if (face.equals(BlockFace.UP))continue;
                    final Block nearBlock = event.getToBlock().getRelative(face);
                    if (nearBlock.getType().equals(Material.LAVA) && nearBlock.getState().getRawData() <=4 || nearBlock.getType().equals(Material.STATIONARY_LAVA))
                    {
                        BlockState oldNearBlock = nearBlock.getState();
                        BlockState newNearBlock = nearBlock.getState();
                        newNearBlock.setType(nearBlock.getData() == 0 ? OBSIDIAN : COBBLESTONE);
                        newNearBlock.setRawData((byte)0);
                        if (this.manager.isIgnored(event.getBlock().getWorld(),BLOCK_FORM,newNearBlock.getType())) continue;
                        this.logBlockChange(BLOCK_FORM,oldNearBlock,newNearBlock,null);
                    }
                }
                newToBlock.setType(Material.WATER);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            if (toBlock.getType().equals(AIR))
            {
                if (this.manager.isIgnored(event.getBlock().getWorld(),WATER_FLOW)) return;
            }
            else
            {
                if (this.manager.isIgnored(event.getBlock().getWorld(),WATER_BREAK)) return;
            }
            this.logBlockChange(toBlock.getType().equals(AIR) ? WATER_FLOW : WATER_BREAK, toBlock, newToBlock, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        Player player = event.getPlayer();
        if (player == null)
        {
            if (this.manager.isIgnored(event.getWorld(),NATURAL_GROW)) return;
        }
        else
        {
            if (this.manager.isIgnored(event.getWorld(),PLAYER_GROW)) return;
        }
        for (BlockState newBlock : event.getBlocks())
        {
            BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
            if (!(oldBlock.getTypeId() == newBlock.getTypeId() && oldBlock.getRawData() == newBlock.getRawData()))
            {
                this.logBlockChange(player == null ? NATURAL_GROW : PLAYER_GROW, oldBlock, newBlock, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),SIGN_CHANGE)) return;
        String[] oldLines = ((Sign)event.getBlock().getState()).getLines();
        try
        {
            if (oldLines[0].isEmpty() && oldLines[1].isEmpty() && oldLines[2].isEmpty() && oldLines[3].isEmpty()) // only log new lines
            {
                if (event.getLines()[0].isEmpty() && event.getLines()[1].isEmpty() && event.getLines()[2].isEmpty() && event.getLines()[3].isEmpty())
                {
                    return; // nothing to log empty sign
                }
                this.manager.queueLog(event.getBlock().getLocation(), SIGN_CHANGE, event.getPlayer(),
                        this.module.getObjectMapper().writeValueAsString(event.getLines()));
            }
            else //log both
            {
                this.manager.queueLog(event.getBlock().getLocation(), SIGN_CHANGE, event.getPlayer(),
                                      this.module.getObjectMapper().writeValueAsString(Arrays.asList(oldLines, event.getLines())));
            }
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalStateException("Could not parse sign-text!",e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),ENTITY_BREAK)) return;
        BlockState state = event.getBlock().getState();
        state = this.adjustBlockForDoubleBlocks(state);
        this.logBlockChange(ENTITY_BREAK,state,null,null,event.getEntityType().name());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event)
    {
        if(event.getEntityType().equals(EntityType.SHEEP))
        {
            if (this.manager.isIgnored(event.getBlock().getWorld(),SHEEP_EAT))return;
            this.logBlockChange(SHEEP_EAT, event.getBlock().getState(), event.getTo());
        }
        else if(event.getEntity() instanceof Enderman)
        {
            if (event.getTo().equals(AIR))
            {
                if (this.manager.isIgnored(event.getBlock().getWorld(),ENDERMAN_PICKUP)) return;
                this.logBlockChange(ENDERMAN_PICKUP, event.getBlock().getState(), event.getTo());
            }
            else
            {
                if (this.manager.isIgnored(event.getBlock().getWorld(),ENDERMAN_PLACE)) return;
                this.logBlockChange(ENDERMAN_PLACE, event.getBlock().getState(), event.getTo());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event)
    {
        if (this.manager.isIgnored(event.getBlock().getWorld(),ENTITY_FORM,event.getNewState().getType())) return;
        this.logBlockChange(ENTITY_FORM,event.getBlock().getState(),event.getNewState(),null,event.getEntity().getType().name());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        int action;
        Player player = null;
        if (event.getEntity() instanceof TNTPrimed)
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),TNT_EXPLODE)) return;
            action = TNT_EXPLODE;
        }
        else if (event.getEntity() instanceof Creeper)
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),CREEPER_EXPLODE)) return;
            final Entity target = ((Creeper)event.getEntity()).getTarget();
            player = target instanceof Player ? ((Player)target) : null;
            action = CREEPER_EXPLODE;
        }
        else if (event.getEntity() instanceof Fireball)
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),FIREBALL_EXPLODE)) return;
            action = FIREBALL_EXPLODE;
        }
        else if (event.getEntity() instanceof EnderDragon)
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),ENDERDRAGON_EXPLODE)) return;
            action = ENDERDRAGON_EXPLODE;
        }
        else if (event.getEntity() instanceof WitherSkull)
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),WITHER_EXPLODE)) return;
            action = WITHER_EXPLODE;
        }
        else
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),ENTITY_EXPLODE)) return;
            action = ENTITY_EXPLODE;
        }

        for (Block block : event.blockList())
        {
            if ((block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR_BLOCK))
                    && block.getData() >= 8)
            {
                continue;
            }
            this.logBlockChange(action,block.getState(), AIR,player);
            this.logRelatedBlocks(block.getState(),player,action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (event.getBucket().equals(Material.WATER_BUCKET))
        {
            if (this.manager.isIgnored(state.getWorld(),WATER_BUCKET)) return;
            this.logBlockChange(WATER_BUCKET,state,Material.STATIONARY_WATER, event.getPlayer());
        }
        else if (event.getBucket().equals(Material.LAVA_BUCKET))
        {
            if (this.manager.isIgnored(state.getWorld(),LAVA_BUCKET)) return;
            this.logBlockChange(WATER_BUCKET, state, Material.STATIONARY_LAVA, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        BlockState blockState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (blockState.getType().equals(Material.WATER) || blockState.getType().equals(Material.STATIONARY_WATER))
        {
            if (this.manager.isIgnored(event.getBlockClicked().getWorld(),BUCKET_FILL)) return;
            this.logBlockChange(BUCKET_FILL,blockState, AIR,event.getPlayer());
        }
        else if (blockState.getType().equals(Material.LAVA) || blockState.getType().equals(Material.STATIONARY_LAVA))
        {
            if (this.manager.isIgnored(event.getBlockClicked().getWorld(),BUCKET_FILL)) return;
            this.logBlockChange(BUCKET_FILL,blockState, AIR,event.getPlayer());
        }
        else // milk
        {
            if (this.manager.isIgnored(event.getBlockClicked().getWorld(),MILK_FILL)) return;
            this.manager.queueLog(blockState.getLocation(),MILK_FILL,event.getPlayer(),"milk");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            Location location = event.getClickedBlock().getLocation();
            BlockState state = event.getClickedBlock().getState();
            switch (state.getType())
            {
                case FURNACE:
                case DISPENSER:
                case CHEST:
                case ENDER_CHEST:
                case ENCHANTMENT_TABLE:
                case ANVIL:
                case BREWING_STAND:
                case TRAPPED_CHEST:
                case HOPPER:
                case DROPPER:
                    if (this.manager.isIgnored(state.getWorld(),CONTAINER_ACCESS)) return;
                    this.manager.queueLog(location,CONTAINER_ACCESS,event.getPlayer(),state.getType().name());
                    break;
                case WOODEN_DOOR:
                case TRAP_DOOR:
                case FENCE_GATE:
                    if (this.manager.isIgnored(state.getWorld(),DOOR_USE)) return;
                    state = this.adjustBlockForDoubleBlocks(state);
                    this.manager.queueLog(state.getLocation(),DOOR_USE,event.getPlayer(),state.getType().name());
                    break;
                case LEVER:
                    if (this.manager.isIgnored(state.getWorld(),LEVER_USE)) return;
                    Lever leverData = (Lever) state.getData();
                    leverData.setPowered(!leverData.isPowered());
                    this.logBlockChange(location, LEVER_USE, event.getPlayer(), state, state.getType().name(), leverData.getData());
                    break;
                case REDSTONE_COMPARATOR_ON:
                case REDSTONE_COMPARATOR_OFF:
                    if (this.manager.isIgnored(state.getWorld(),COMPARATOR_CHANGE)) return;
                    BlockState newState = event.getClickedBlock().getState();
                    newState.setType(newState.getType().equals(REDSTONE_COMPARATOR_ON) ? REDSTONE_COMPARATOR_OFF : REDSTONE_COMPARATOR_ON);
                    this.logBlockChange(location, COMPARATOR_CHANGE, event.getPlayer(), state, newState, null);
                    break;
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    if (this.manager.isIgnored(state.getWorld(),BUTTON_USE)) return;
                    this.manager.queueLog(location,BUTTON_USE,event.getPlayer(),state.getType().name());
                    break;
                case LOG: // placing cocoa-pods
                    if (itemInHand.getType().equals(Material.INK_SACK) && itemInHand.getDurability() == 3) // COCOA-Beans
                    {
                        if (this.manager.isIgnored(state.getWorld(),BLOCK_PLACE)) return;
                        BlockState blockPlaced = event.getClickedBlock().getRelative(event.getBlockFace()).getState();
                        blockPlaced.setType(Material.COCOA);
                        blockPlaced.setRawData((byte) 1);
                        this.logBlockChange(BLOCK_PLACE,null,blockPlaced,event.getPlayer());
                    }
                    break;
                case CROPS:
                case GRASS:
                case MELON_STEM:
                case PUMPKIN_STEM:
                case SAPLING:
                case CARROT:
                case POTATO:
                    if (itemInHand.getType().equals(Material.INK_SACK) && itemInHand.getDurability() == 15)
                    {
                        if (this.manager.isIgnored(state.getWorld(),BONEMEAL_USE)) return;
                        this.manager.queueLog(location,BONEMEAL_USE,event.getPlayer(),state.getType().name());
                    }
                    break;
                case RAILS:
                case DETECTOR_RAIL:
                case POWERED_RAIL:
                    if (itemInHand.getType().equals(Material.MINECART)
                    || itemInHand.getType().equals(Material.STORAGE_MINECART)
                    || itemInHand.getType().equals(Material.POWERED_MINECART)
                    || itemInHand.getType().equals(Material.HOPPER_MINECART)
                    || itemInHand.getType().equals(Material.EXPLOSIVE_MINECART)) // BOAT is done down below
                    {
                        if (this.manager.isIgnored(state.getWorld(),VEHICLE_PLACE)) return;
                        Block block = event.getClickedBlock().getRelative(BlockFace.UP);
                        this.manager.getEntityListener().preplanVehiclePlacement(block.getLocation(),event.getPlayer());
                    }
                    break;
                case TNT:
                    if(itemInHand.getType().equals(Material.FLINT_AND_STEEL))
                    {
                        if (this.manager.isIgnored(state.getWorld(),TNT_PRIME)) return;
                        this.logBlockChange(TNT_PRIME,event.getClickedBlock().getState(), AIR,event.getPlayer());
                    }
                    break;
                case CAKE_BLOCK:
                    if (this.manager.isIgnored(state.getWorld(),CAKE_EAT)) return;
                    if (event.getPlayer().getFoodLevel() < 20 && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
                    {
                        byte cakeData = (byte) (event.getClickedBlock().getData() +1);
                        if (cakeData == 6)
                        {
                            this.logBlockChange(CAKE_EAT,event.getClickedBlock().getState(), AIR,event.getPlayer());
                        }
                        else
                        {
                            this.logBlockChange(location,CAKE_EAT,event.getPlayer(),state,state.getType().name(),cakeData);
                        }
                    }
                    break;
                case NOTE_BLOCK:
                    if (this.manager.isIgnored(state.getWorld(),NOTEBLOCK_CHANGE)) return;
                    NoteBlock noteBlock = (NoteBlock) event.getClickedBlock().getState();
                    byte clicks = (byte) (noteBlock.getRawNote() + 1);
                    if (clicks == 25)
                    {
                        clicks = 0;
                    }
                    this.logBlockChange(location, NOTEBLOCK_CHANGE, event.getPlayer(), state, state.getType().name(), clicks);
                    break;
                case JUKEBOX:
                    break;
                case DIODE_BLOCK_ON:
                case DIODE_BLOCK_OFF:
                    if (this.manager.isIgnored(state.getWorld(),REPEATER_CHANGE)) return;
                    Diode diode = (Diode) event.getClickedBlock().getState().getData();
                    int delay = diode.getDelay() + 1;
                    if (delay == 5)
                    {
                        delay = 1;
                    }
                    diode.setDelay(delay);
                    this.logBlockChange(location, REPEATER_CHANGE, event.getPlayer(), state, state.getType().name(), diode.getData());
                    break;
                default:
                    break;
            }

            if (itemInHand.getType().equals(Material.MONSTER_EGG))
            {
                if (this.manager.isIgnored(state.getWorld(),MONSTER_EGG_USE)) return;
                this.manager.queueLog(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),MONSTER_EGG_USE,event.getPlayer(),
                        this.manager.getEntityListener().serializeItemStack(itemInHand));
            }
            else if (itemInHand.getType().equals(Material.FIREWORK))
            {
                if (this.manager.isIgnored(state.getWorld(),FIREWORK_USE)) return;
                this.manager.queueLog(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),FIREWORK_USE,event.getPlayer(),null);
            }
            else if (itemInHand.getType().equals(Material.BOAT))
            {
                if (this.manager.isIgnored(state.getWorld(),VEHICLE_PLACE)) return;
                Block block = event.getClickedBlock().getRelative(BlockFace.UP);
                this.manager.getEntityListener().preplanVehiclePlacement(block.getLocation(),event.getPlayer());
            }
        }
        else if (event.getAction().equals(Action.PHYSICAL))
        {
            switch (event.getClickedBlock().getType())
            {
                case SOIL:
                    if (this.manager.isIgnored(event.getClickedBlock().getWorld(),CROP_TRAMPLE)) return;
                    this.logBlockChange(CROP_TRAMPLE,event.getClickedBlock().getRelative(BlockFace.UP).getState(), AIR,event.getPlayer());
                    break;
                case WOOD_PLATE:
                case STONE_PLATE:
                    if (this.manager.isIgnored(event.getClickedBlock().getWorld(),PLATE_STEP)) return;
                    this.manager.queueLog(event.getClickedBlock().getLocation(),PLATE_STEP,event.getPlayer(),event.getClickedBlock().getType().name());
                    break;
            }
        }
    }

    private BlockState adjustBlockForDoubleBlocks(BlockState blockState)
    {
        if (blockState.getType().equals(Material.WOOD_DOOR) || blockState.getType().equals(Material.IRON_DOOR_BLOCK))
        {
            if (blockState.getRawData() == 8 || blockState.getRawData() == 9)
            {
                return blockState.getBlock().getRelative(BlockFace.DOWN).getState();
            }
        }
        else if (blockState instanceof Bed)
        {
            if (((Bed)blockState).isHeadOfBed())
            {
                return blockState.getBlock().getRelative(((Bed)blockState).getFacing().getOppositeFace()).getState();
            }
        }
        return blockState;
    }

    private void logRelatedBlocks(BlockState blockState, Player player, int reason)
    {
        if (blockState.getType().equals(OBSIDIAN))
        {
            Block block = blockState.getBlock();
            for (BlockFace face : BLOCK_FACES)
            {
                if (block.getRelative(face).getType().equals(PORTAL))
                {
                    this.logBlockChange(BLOCK_BREAK,block.getRelative(face).getState(),AIR,player); // logging one is enough
                    break;
                }
            }
        }
        Block onTop = blockState.getBlock().getRelative(BlockFace.UP);
        while (onTop.getType().equals(Material.SAND)||onTop.getType().equals(Material.GRAVEL)||onTop.getType().equals(Material.ANVIL))
        {
            if (!this.manager.isIgnored(blockState.getWorld(),BLOCK_FALL))
            {
                this.plannedFallingBlocks.put(onTop.getLocation(),new Pair<Player, Integer>(player,reason));
                onTop = onTop.getRelative(BlockFace.UP);
            }
            else
            {
                break;
            }
        }
        if (!blockState.getType().isSolid() && !blockState.getType().equals(Material.SUGAR_CANE_BLOCK))
        {
            return; // cannot have attached
        }
        if (!this.manager.isIgnored(blockState.getWorld(),BLOCK_BREAK))
        {
            for (Block block : BlockUtil.getAttachedBlocks(blockState.getBlock()))
            {
                this.plannedFallingBlocks.put(block.getLocation(),new Pair<Player, Integer>(player,reason));
            }
            for (Block block : BlockUtil.getDetachableBlocksOnTop(blockState.getBlock()))
            {
                this.plannedFallingBlocks.put(block.getLocation(),new Pair<Player, Integer>(player,reason));
            }
        }
        if (this.manager.isIgnored(blockState.getWorld(),HANGING_BREAK)) return;
        Location location = blockState.getLocation();
        Location entityLocation = blockState.getLocation();
        User user = this.module.getCore().getUserManager().getExactUser(player);
        for (Entity entity : blockState.getBlock().getChunk().getEntities())
        {
            if (entity instanceof Hanging && location.distanceSquared(entity.getLocation(entityLocation)) < 4)
            {
                this.manager.getEntityListener().preplanBreakedHanging(entity.getLocation(),user == null ? -reason : user.key);
            }
        }
    }

    private void logItemDropsFromDestroyedContainer(InventoryHolder containerBlock, Location location, Player player)
    {
        if (this.manager.isIgnored(location.getWorld(),ITEM_DROP)) return;
        ItemStack[] contents;
        if (containerBlock.getInventory() instanceof DoubleChestInventory)
        {
            DoubleChestInventory inventory = (DoubleChestInventory) containerBlock.getInventory();
            if (((Chest)inventory.getLeftSide().getHolder()).getLocation().equals(location))
            {
                contents = inventory.getLeftSide().getContents();
            }
            else
            {
                contents = inventory.getRightSide().getContents();
            }
        }
        else
        {
            contents = containerBlock.getInventory().getContents();
        }
        for (ItemStack itemStack : contents)
        {
            if (itemStack == null || itemStack.getType().equals(AIR))
            {
                continue;
            }
            String itemData = this.manager.getEntityListener().serializeItemStack(itemStack);
            this.manager.queueLog(location,ITEM_DROP,player,itemData);
        }
    }

    private void logBlockChange(int action, BlockState oldState, BlockState newState, Player player, String additional)
    {
        this.logBlockChange(oldState.getLocation(), action, player, oldState, newState, additional);
    }

    private void logBlockChange(int action, BlockState oldState, BlockState newState, Player player)
    {
        this.logBlockChange(action,oldState,newState,player,null);
    }

    private void logBlockChange(int action, BlockState oldState, Material to, Player player)
    {
        this.logBlockChange(oldState.getLocation(), action, player, oldState, to.name(), (byte) 0);
    }

    private void logBlockChange(int action, BlockState oldState, Material to)
    {
        this.logBlockChange(action,oldState,to,null);
    }

    public void logBlockChange(Location location, int action, Player player, BlockState oldState, String newBlock, Byte newData)
    {
        this.logBlockChange(location, action, this.getUserKey(player), oldState, newBlock, newData);
    }

    public void logBlockChange(Location location, int action, Player player, BlockState oldState, BlockState newState, String additional)
    {
        this.manager.queueLog(location,action,this.getUserKey(player),
                oldState.getType().name(),oldState.getRawData(),
                newState.getType().name(),newState.getRawData(),additional);
    }

    public void logBlockChange(Location location, int action, Long causer, BlockState oldState, String newBlock, Byte newData)
    {
        this.manager.queueLog(location, action, causer, oldState.getType().name(), oldState.getRawData(), newBlock, newData, null);
    }

    public void logBlockChange(Location location, int action, Player causer, BlockState oldState, String additionalData)
    {
        this.manager.queueLog(location,action,this.getUserKey(causer),oldState.getType().name(),oldState.getRawData(), AIR.name(),(byte)0,additionalData);
    }

    private Long getUserKey(Player player)
    {
        if (player== null) return null;
        return this.module.getCore().getUserManager().getExactUser(player).key;
    }
}
