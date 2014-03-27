package de.cubeisland.engine.log.action.newaction.block;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.log.action.logaction.block.player.PlayerGrow;
import de.cubeisland.engine.log.action.newaction.LogListener;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.FIRE;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockFadeEvent}
 * {@link BlockBurnEvent}
 * {@link BlockFormEvent}
 * {@link BlockSpreadEvent}
 * {@link LeavesDecayEvent}
 * {@link StructureGrowEvent}
 * <p>Actions:
 * {@link BlockBurn}
 * {@link BlockFade}
 * {@link BlockForm}
 * {@link BlockSpread}
 * {@link LeafDecay}
 * {@link NaturalGrow}
 * <p>External Actions:
 * {@link PlayerGrow}
 */
public class BlockListener extends LogListener
{
    public BlockListener(Module module)
    {
        super(module);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        BlockBurn action = this.newAction(BlockBurn.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            // TODO blockState = this.adjustBlockForDoubleBlocks(blockState);
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
        // TODO
        this.logAttachedBlocks(event.getBlock().getState(), null);
        this.logFallingBlocks(event.getBlock().getState(), null);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        BlockFade action = this.newAction(BlockFade.class, event.getBlock().getWorld());
        if (action != null)
        {
            // TODO if (!this.lm.getConfig(event.getBlock().getWorld()).block.fade.ignore.contains(event.getBlock().getType()))
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if (event instanceof EntityBlockFormEvent) return;
        // TODO block.form.BLOCK_FORM_ignore
        BlockForm action = this.newAction(BlockForm.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getNewState());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (!(event.getNewState().getType() == FIRE))
        {
            BlockSpread action = this.newAction(BlockSpread.class, event.getBlock().getWorld());
            if (action != null)
            {
                action.setLocation(event.getBlock().getLocation());
                action.setOldBlock(event.getBlock().getState());
                action.setNewBlock(event.getNewState());
                this.logAction(action);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        LeafDecay action = this.newAction(LeafDecay.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(AIR);
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        if (event.getPlayer() == null)
        {
            if (this.isActive(NaturalGrow.class, event.getWorld()))
            {
                for (BlockState newState : event.getBlocks())
                {
                    BlockState oldState = newState.getBlock().getState();  // TODO is this working?
                    if (oldState.getData().equals(newState.getData()))
                    {
                        continue;
                    }
                    NaturalGrow action = this.newAction(NaturalGrow.class);
                    action.setNewBlock(newState);
                    action.setOldBlock(oldState);
                    action.setLocation(newState.getLocation());
                }
            }
        }
        else
        {
            // TODO player Grow Action
        }
    }
}
