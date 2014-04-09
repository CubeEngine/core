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
package de.cubeisland.engine.log.action.block.player.interact;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Cake;
import org.bukkit.material.Crops;
import org.bukkit.material.Diode;
import org.bukkit.material.Dye;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.Rails;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;
import de.cubeisland.engine.log.action.vehicle.VehiclePrePlaceEvent;

import static org.bukkit.DyeColor.WHITE;
import static org.bukkit.GameMode.CREATIVE;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.UP;
import static org.bukkit.event.block.Action.PHYSICAL;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

/**
 * A Listener for PlayerHanging Actions
 * <p>Events:
 * {@link PlayerInteractEvent}
 * <p>Actions:
 * {@link UseContainer}
 * {@link UseDoor}
 * {@link UseLever}
 * {@link UseComparator}
 * {@link UseButton}
 * {@link UseCake}
 * {@link UseNoteblock}
 * {@link UseRepeater}
 * {@link UseTnt}
 * {@link UseBonemeal}
 * {@link BlockTrample}
 * {@link UsePlate}
 * <p>Fired Events:
 * {@link VehiclePrePlaceEvent}
 */
public class ListenerPlayerBlockInteract extends LogListener
{
    public ListenerPlayerBlockInteract(Log module)
    {
        super(module, UseContainer.class, UseDoor.class, UseLever.class, UseComparator.class, UseButton.class,
              UseCake.class, UseNoteblock.class, UseRepeater.class, UseTnt.class, UseBonemeal.class, BlockTrample.class,
              UsePlate.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        //TODO put item into itemframe
        if (event.getAction() == RIGHT_CLICK_BLOCK)
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            Location location = event.getClickedBlock().getLocation();
            BlockState state = event.getClickedBlock().getState();
            ActionPlayerBlock action;
            BlockState newState = state.getBlock().getState();

            if (state instanceof InventoryHolder)
            {
                action = this.newAction(UseContainer.class, state.getWorld());
            }
            else if (state.getData() instanceof Openable)
            {
                action = this.newAction(UseDoor.class, state.getWorld());
                if (action != null)
                {
                    state = adjustBlockForDoubleBlocks(state);
                    Openable openable = (Openable)state.getBlock().getState().getData();
                    openable.setOpen(!openable.isOpen());
                    newState.setData((MaterialData)openable);
                }
            }
            else if (state.getData() instanceof Lever)
            {
                action = this.newAction(UseLever.class, state.getWorld());
                if (action != null)
                {
                    Lever leverData = (Lever)state.getBlock().getState().getData();
                    leverData.setPowered(!leverData.isPowered());
                    newState.setData(leverData);
                }
            }
            else if (state.getType() == REDSTONE_COMPARATOR_ON || state.getType() == REDSTONE_COMPARATOR_OFF)
            {
                action = this.newAction(UseComparator.class, state.getWorld());
                if (action != null)
                {
                    newState.setType(
                        state.getType() == REDSTONE_COMPARATOR_ON ? REDSTONE_COMPARATOR_OFF : REDSTONE_COMPARATOR_ON);
                }
            }
            else if (state.getData() instanceof Button)
            {
                action = this.newAction(UseButton.class, state.getWorld());
                if (action != null)
                {
                    Button button = (Button)state.getBlock().getState().getData();
                    button.setPowered(true);
                    newState.setData(button);
                }
            }
            else if (state.getData() instanceof Rails)
            {
                if (itemInHand.getType() == Material.MINECART || itemInHand.getType() == Material.STORAGE_MINECART
                    || itemInHand.getType() == Material.POWERED_MINECART
                    || itemInHand.getType() == Material.HOPPER_MINECART
                    || itemInHand.getType() == Material.EXPLOSIVE_MINECART) // BOAT is done down below
                {
                    VehiclePrePlaceEvent vEvent = new VehiclePrePlaceEvent(event.getClickedBlock().getRelative(
                        UP).getLocation(), event.getPlayer());
                    this.module.getCore().getEventManager().fireEvent(vEvent);
                }
                action = null;
            }
            else if (itemInHand.getType().equals(Material.BOAT))
            {
                VehiclePrePlaceEvent vEvent = new VehiclePrePlaceEvent(event.getClickedBlock().getRelative(
                    UP).getLocation(), event.getPlayer());
                this.module.getCore().getEventManager().fireEvent(vEvent);
                action = null;
            }
            else if (state.getData() instanceof Cake)
            {
                action = null;
                if (event.getPlayer().getFoodLevel() < 20 && !(event.getPlayer().getGameMode() == CREATIVE))
                {
                    action = this.newAction(UseCake.class, state.getWorld());
                    if (action != null)
                    {
                        Cake cake = (Cake)state.getBlock().getState().getData();
                        cake.setSlicesEaten(cake.getSlicesEaten() + 1);
                        if (cake.getSlicesRemaining() == 0)
                        {
                            action.setNewBlock(AIR);
                            newState = null;
                        }
                        else
                        {
                            newState.setData(cake);
                        }
                    }
                }
            }
            else if (state instanceof NoteBlock)
            {
                action = this.newAction(UseNoteblock.class, state.getWorld());
                if (action != null)
                {
                    try
                    {
                        ((NoteBlock)newState).setNote(((NoteBlock)newState).getNote().sharped());
                    }
                    catch (IllegalArgumentException e)
                    {
                        ((NoteBlock)newState).setNote(new Note(1, Tone.F, true));
                    }
                }
            }
            else if (state.getData() instanceof Diode)
            {
                action = this.newAction(UseRepeater.class, state.getWorld());
                if (action != null)
                {
                    Diode diode = (Diode)state.getBlock().getState().getData();
                    Integer delay = diode.getDelay() + 1;
                    if (delay == 5)
                    {
                        delay = 1;
                    }
                    diode.setDelay(delay);
                    newState.setData(diode);
                }
            }
            else if (state.getType() == TNT)
            {
                action = null;
                if (itemInHand.getType() == FLINT_AND_STEEL)
                {
                    action = this.newAction(UseTnt.class, state.getWorld());
                    if (action != null)
                    {
                        action.setNewBlock(AIR);
                        newState = null;
                    }
                }
            }
            else if (state.getData() instanceof Crops || state.getType() == GRASS)
            {
                if (itemInHand.getData() instanceof Dye && ((Dye)itemInHand.getData()).getColor() == WHITE)
                {
                    // TODO THIS IS BULLSHIT I need an event for bonemealUse...
                    action = this.newAction(UseBonemeal.class, state.getWorld());
                    if (action != null)
                    {
                        //TODO adjust growth stage
                        //TODO do not log if fully grown
                        action.setNewBlock(state);

                        action.setLocation(state.getLocation());
                        action.setOldBlock(state);
                        action.setPlayer(event.getPlayer());
                        this.logAction(action);
                    }
                }
                return;
            }
            else if (state instanceof Jukebox)
            {
                action = null;// TODO
            }
            else
            {
                action = null;
            }

            if (action != null)
            {
                action.setLocation(state.getLocation());
                action.setOldBlock(state);
                if (newState != null)
                {
                    action.setNewBlock(newState);
                }
                action.setPlayer(event.getPlayer());
                this.logAction(action);
            }
        }
        else if (event.getAction() == PHYSICAL)
        {
            Block block = event.getClickedBlock();
            if (block.getType() == SOIL)
            {
                BlockTrample action = this.newAction(BlockTrample.class, block.getWorld());
                if (action != null)
                {
                    BlockState stateUp = block.getRelative(UP).getState();
                    if (stateUp.getData() instanceof Crops)
                    {
                        BlockTrample actionUp = this.newAction(BlockTrample.class, block.getWorld());
                        actionUp.setLocation(stateUp.getLocation());
                        actionUp.setOldBlock(stateUp);
                        actionUp.setNewBlock(AIR);
                        actionUp.setPlayer(event.getPlayer());
                        this.logAction(actionUp);
                    }
                    action.setLocation(block.getLocation());
                    action.setOldBlock(block.getState());
                    action.setNewBlock(DIRT);
                    action.setPlayer(event.getPlayer());
                    this.logAction(action);
                }
            }
            else if (block.getState().getData() instanceof PressurePlate)
            {
                UsePlate action = this.newAction(UsePlate.class, block.getWorld());
                if (action != null)
                {
                    //BlockState newState = block.getState();
                    //PressurePlate plate = (PressurePlate)newState.getData();
                    action.setLocation(block.getLocation());
                    action.setOldBlock(block.getState());
                    action.setNewBlock(block.getState());
                    action.setPlayer(event.getPlayer());
                    this.logAction(action);
                }
            }
        }
    }
}
