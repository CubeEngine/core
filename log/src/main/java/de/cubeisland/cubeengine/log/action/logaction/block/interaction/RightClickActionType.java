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
package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Diode;
import org.bukkit.material.Lever;

import de.cubeisland.cubeengine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType.BlockData;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BlockPlace;
import de.cubeisland.cubeengine.log.action.logaction.interact.FireworkUse;
import de.cubeisland.cubeengine.log.action.logaction.interact.MonsterEggUse;
import de.cubeisland.cubeengine.log.action.logaction.interact.VehiclePlace;
import de.cubeisland.cubeengine.log.storage.ItemData;

import static org.bukkit.Material.*;

/**
 * Container-ActionType for interaction
 * <p>Events: {@link PlayerInteractEvent}</p>
 * <p>External Actions:
 * {@link ContainerAccess},
 * {@link DoorUse},
 * {@link LeverUse},
 * {@link ComparatorChange},
 * {@link ButtonUse},
 * {@link BlockPlace} for CocoaPods,
 * {@link BonemealUse},
 * {@link VehiclePlace} preplanned,
 * {@link TntPrime},
 * {@link CakeEat},
 * {@link NoteBlockChange},
 * {@link RepeaterChange},
 * {@link MonsterEggUse},
 * {@link FireworkUse},
 * {@link CropTrample},
 * {@link PlateStep}
 */
public class RightClickActionType extends ActionTypeContainer
{
    public RightClickActionType()
    {
        super("RIGHT_CLICK");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        //TODO put item into itemframe
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
                ContainerAccess containerAccess = this.manager.getActionType(ContainerAccess.class);
                if (containerAccess.isActive(state.getWorld()))
                {
                    containerAccess.queueLog(location,event.getPlayer(),state.getType().name(),0L,null,null,null);
                }
                break;
            case WOODEN_DOOR:
            case TRAP_DOOR:
            case FENCE_GATE:
                DoorUse doorUse = this.manager.getActionType(DoorUse.class);
                if (doorUse.isActive(state.getWorld()))
                {
                    state = doorUse.adjustBlockForDoubleBlocks(state);
                    doorUse.logBlockChange(location,event.getPlayer(), BlockData.of(state),BlockData.of(state),null);
                }
                break;
            case LEVER:
                LeverUse leverUse = this.manager.getActionType(LeverUse.class);
                if (leverUse.isActive(state.getWorld()))
                {
                    BlockData newData = BlockData.of(state);
                    Lever leverData = (Lever) state.getData();
                    leverData.setPowered(!leverData.isPowered());
                    newData.data = leverData.getData();
                    leverUse.logBlockChange(location,event.getPlayer(),BlockData.of(state),newData,null);
                }
                break;
            case REDSTONE_COMPARATOR_ON:
            case REDSTONE_COMPARATOR_OFF:
                ComparatorChange comparatorChange = this.manager.getActionType(ComparatorChange.class);
                if (comparatorChange.isActive(state.getWorld()))
                {
                    BlockData newData = BlockData.of(state);
                    newData.material = (newData.material.equals(REDSTONE_COMPARATOR_ON) ?  REDSTONE_COMPARATOR_OFF : REDSTONE_COMPARATOR_ON);
                    comparatorChange.logBlockChange(location,event.getPlayer(),BlockData.of(state),newData,null);
                }
                break;
            case STONE_BUTTON:
            case WOOD_BUTTON:
                ButtonUse buttonUse = this.manager.getActionType(ButtonUse.class);
                if (buttonUse.isActive(state.getWorld()))
                {
                    buttonUse.logBlockChange(location,event.getPlayer(),BlockData.of(state),BlockData.of(state),null);
                }
                break;
            case LOG: // placing cocoa-pods
                if (itemInHand.getType().equals(Material.INK_SACK) && itemInHand.getDurability() == 3) // COCOA-Beans
                {
                    BlockPlace blockPlace = this.manager.getActionType(BlockPlace.class);
                    if (blockPlace.isActive(state.getWorld()))
                    {
                        BlockData newData = BlockData.of(state);
                        newData.material = COCOA;
                        newData.data = 1;
                        blockPlace.logBlockChange(location,event.getPlayer(),BlockData.of(state),newData,null);
                    }
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
                    // TODO THIS IS BULLSHIT I need an event for bonemealUse...
                    BonemealUse bonemealUse = this.manager.getActionType(BonemealUse.class);
                    if (bonemealUse.isActive(state.getWorld()))
                    {
                        BlockData newBlockData = BlockData.of(state);
                        //TODO adjust growth stage
                        //TODO do not log if fully grown
                        bonemealUse.logBlockChange(location, event.getPlayer(),BlockData.of(state),newBlockData,null);
                    }
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
                    VehiclePlace vehiclePlace = this.manager.getActionType(VehiclePlace.class);
                    if (vehiclePlace.isActive(state.getWorld()))
                    {
                        Block block = event.getClickedBlock().getRelative(BlockFace.UP);
                        vehiclePlace.preplanVehiclePlacement(block.getLocation(), event.getPlayer());
                    }
                }
                break;
            case TNT:
                if(itemInHand.getType().equals(Material.FLINT_AND_STEEL))
                {
                    TntPrime tntPrime = this.manager.getActionType(TntPrime.class);
                    if (tntPrime.isActive(state.getWorld()))
                    {
                        tntPrime.logBlockChange(location,event.getPlayer(),BlockData.of(state),AIR,null);
                    }
                }
                break;
            case CAKE_BLOCK:
                if (event.getPlayer().getFoodLevel() < 20 && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
                {
                    CakeEat cakeEat = this.manager.getActionType(CakeEat.class);
                    if (cakeEat.isActive(state.getWorld()))
                    {
                        byte cakeData = (byte) (event.getClickedBlock().getData() +1);
                        if (cakeData == 6)
                        {
                            cakeEat.logBlockChange(location,event.getPlayer(),BlockData.of(state),AIR,null);
                        }
                        else
                        {
                            BlockData newData = BlockData.of(state);
                            newData.data = cakeData;
                            cakeEat.logBlockChange(location,event.getPlayer(),BlockData.of(state),newData,null);
                        }
                    }
                }
                break;
            case NOTE_BLOCK:
                NoteBlockChange noteblockChange = this.manager.getActionType(NoteBlockChange.class);
                if (noteblockChange.isActive(state.getWorld()))
                {
                    NoteBlock noteBlock = (NoteBlock) event.getClickedBlock().getState();
                    byte clicks = (byte) (noteBlock.getRawNote() + 1);
                    if (clicks == 25)
                    {
                        clicks = 0;
                    }
                    BlockData newData = BlockData.of(state);
                    newData.data = clicks;
                    noteblockChange.logBlockChange(location, event.getPlayer(), BlockData.of(state), newData, null);
                }
                break;
            case JUKEBOX:
                break;
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
                RepeaterChange repeaterChange = this.manager.getActionType(RepeaterChange.class);
                if (repeaterChange.isActive(state.getWorld()))
                {
                    BlockData oldData= BlockData.of(state);
                    Diode diode = (Diode) state.getData();
                    Integer delay = diode.getDelay() + 1;
                    if (delay == 5)
                    {
                        delay = 1;
                    }
                    diode.setDelay(delay);
                    BlockData newData = BlockData.of(state);
                    repeaterChange.logBlockChange(location,event.getPlayer(),oldData,newData,null);
                }
                break;
            default:
                break;
            }

            if (itemInHand.getType().equals(Material.MONSTER_EGG))
            {
                MonsterEggUse monsterEggUse = this.manager.getActionType(MonsterEggUse.class);
                if (monsterEggUse.isActive(state.getWorld()))
                {
                    monsterEggUse.logSimple(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),
                                           event.getPlayer(),new ItemData(itemInHand).serialize(this.om));
                }
            }
            else if (itemInHand.getType().equals(Material.FIREWORK))
            {
                System.out.print(itemInHand);//TODO remove
                FireworkUse fireworkUse = this.manager.getActionType(FireworkUse.class);
                if (fireworkUse.isActive(state.getWorld()))
                { //TODO perhaps serialize itemdata?
                    fireworkUse.queueLog(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation()
                                        ,event.getPlayer(),null,null,null,null,null);
                }
            }
            else if (itemInHand.getType().equals(Material.BOAT))
            {
                VehiclePlace vehiclePlace = this.manager.getActionType(VehiclePlace.class);
                if (vehiclePlace.isActive(state.getWorld()))
                {
                    vehiclePlace.preplanVehiclePlacement(event.getClickedBlock().getRelative(BlockFace.UP).getLocation()
                                                        ,event.getPlayer());
                }
            }
        }
        else if (event.getAction().equals(Action.PHYSICAL))
        {
            switch (event.getClickedBlock().getType())
            {
            case SOIL:
                CropTrample cropTrample = this.manager.getActionType(CropTrample.class);
                if (cropTrample.isActive(event.getClickedBlock().getWorld()))
                {
                    BlockState cropState = event.getClickedBlock().getRelative(BlockFace.UP).getState();
                    switch (cropState.getType())
                    {
                        case CROPS:
                        case GRASS:
                        case MELON_STEM:
                        case PUMPKIN_STEM:
                        case SAPLING:
                        case CARROT:
                        case POTATO:
                            cropTrample.logBlockChange(cropState.getLocation(),event.getPlayer(),BlockData.of(cropState),AIR,null);
                    }
                    cropTrample.logBlockChange(event.getClickedBlock().getLocation(), event.getPlayer(), BlockData.of(event.getClickedBlock().getState()),DIRT,null);
                }
                break;
            case WOOD_PLATE:
            case STONE_PLATE:
                PlateStep plateStep = this.manager.getActionType(PlateStep.class);
                if (plateStep.isActive(event.getClickedBlock().getWorld()))
                {
                    BlockData blockData = BlockData.of(event.getClickedBlock().getState());
                    plateStep.logBlockChange(event.getClickedBlock().getLocation(),event.getPlayer(),
                                             blockData, blockData, null);
                }
                break;
            }
        }
    }
}
