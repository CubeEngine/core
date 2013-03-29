package de.cubeisland.cubeengine.log.listeners;

import java.util.HashMap;
import java.util.Map;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import static de.cubeisland.cubeengine.core.util.InventoryUtil.getMissingSpace;

public class ContainerListener implements Listener
{

    private LogManager manager;
    private Log module;

    private TLongObjectHashMap<TObjectIntHashMap<ItemData>> inventoryChanges = new TLongObjectHashMap<TObjectIntHashMap<ItemData>>();

    public ContainerListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getPlayer() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser((Player)event.getPlayer());
            TObjectIntHashMap<ItemData> itemDataMap = this.inventoryChanges.get(user.key);
            if (itemDataMap != null)
            {
                Location location = this.getLocationForHolder(event.getInventory().getHolder());
                if (location == null) return;
                for (ItemData itemData : itemDataMap.keySet())
                {
                    int amount = itemDataMap.get(itemData);
                    if (amount == 0) continue;
                    String additional = this.serializeItemData(itemData);
                    this.manager.queueContainerLog(location, amount < 0 ? ITEM_REMOVE : ITEM_INSERT, user.key, itemData.material, itemData.dura, event
                        .getInventory().getType().name(), additional);
                }
            }
            this.inventoryChanges.remove(user.key);
        }
    }

    public String serializeItemData(ItemData itemData)
    {
        try {
            return this.module.getLogManager().mapper.writeValueAsString(itemData.serialize());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse itemData!",e);
        }
    }

    private Location getLocationForHolder(InventoryHolder holder)
    {
        if (holder instanceof Entity)
        {
            return ((Entity)holder).getLocation();
        }
        else if (holder instanceof DoubleChest)
        {
            return ((DoubleChest)holder).getLocation();//TODO get a blocklocation
            //TODO get the correct chest
        }
        else if (holder instanceof BlockState)
        {
            return ((BlockState)holder).getLocation();
        }
        if (holder == null)
        {
            this.module.getLog().log(LogLevel.DEBUG,"Inventory Holder is null! Logging is impossible.");
        }
        else
        {
            this.module.getLog().log(LogLevel.DEBUG,"Unknown InventoryHolder:" + holder.toString());
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (event.getPlayer() instanceof Player)
        {
            if (this.manager.isIgnored(event.getPlayer().getWorld(),ITEM_CHANGE_IN_CONTAINER,event.getInventory().getHolder())) return;
            User user = this.module.getCore().getUserManager().getExactUser((Player)event.getPlayer());
            this.inventoryChanges.put(user.key,new TObjectIntHashMap<ItemData>());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getSlot() == -999)
        {
            return;
        }
        if (event.getWhoClicked() instanceof Player)
        {
            final User user = this.module.getCore().getUserManager().getExactUser((Player)event.getWhoClicked());
            if (!this.inventoryChanges.containsKey(user.key)) return;
            final World world = event.getWhoClicked().getWorld();
            if (this.manager.isIgnored(world, ITEM_INSERT)
             && this.manager.isIgnored(world, ITEM_REMOVE)) return;
            Inventory inventory = event.getInventory();
            InventoryHolder holder = inventory.getHolder();
            if (this.manager.isIgnored(world,ITEM_CHANGE_IN_CONTAINER,holder)) return;
            ItemStack inventoryItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            //TODO remove this debug
            System.out.print(user.getName()+"------------Click Event----------- in "+holder);
            System.out.print("Cursor: "+ cursorItem + " | Inventory: "+ inventoryItem);
            System.out.print((event.getRawSlot() < event.getView().getTopInventory().getSize() ? "TOP " : "BOT ")
            + (event.isShiftClick() ? "SHIFT-" : "") + (event.isRightClick() ? "RIGHT" : "LEFT"));
            if (inventoryItem.getType().equals(Material.AIR) && cursorItem.getType().equals(Material.AIR))
            {
                return; // nothing to log
            }
            if (event.getRawSlot() < event.getView().getTopInventory().getSize()) // click in top inventory
            {
                if (event.isShiftClick()) // top & shift -> remove items
                {
                    if (inventoryItem.getType().equals(Material.AIR)
                    || this.manager.isIgnored(world, ITEM_REMOVE))
                    {
                        return;
                    }
                    int missingSpace = getMissingSpace(event.getView().getTopInventory(),inventoryItem);
                    int amountTake = inventoryItem.getAmount() - missingSpace;
                    if (amountTake > 0)
                    {
                        this.prepareForLogging(user, new ItemData(inventoryItem),-amountTake);
                    }
                }
                else
                {
                    if (cursorItem.getType().equals(Material.AIR)) // remove items
                    {
                        if (this.manager.isIgnored(world, ITEM_REMOVE)) return;
                        int remove = event.isLeftClick() ? inventoryItem.getAmount() : (inventoryItem.getAmount() + 1) / 2;
                        this.prepareForLogging(user, new ItemData(inventoryItem),-remove);
                    }
                    else if (inventoryItem.getType().equals(Material.AIR)) // put items
                    {
                        if (this.manager.isIgnored(world, ITEM_INSERT)) return;
                        int put = event.isLeftClick() ? cursorItem.getAmount() : 1;
                        if (holder instanceof BrewingStand) // handle BrewingStands separatly
                        {
                            if (event.getRawSlot() == 3)
                            {
                                if (!BukkitUtils.canBePlacedInBrewingstand(cursorItem.getType())) // can be put
                                {
                                    return;
                                }
                            }
                            else if (cursorItem.getType().equals(Material.POTION) || cursorItem.getType().equals(Material.GLASS_BOTTLE)) // bottle slot
                            {
                                put = 1;
                            }
                            else
                            {
                                return;
                            }
                        }
                        this.prepareForLogging(user, new ItemData(cursorItem),put);
                    }
                    else
                    {
                        if (inventoryItem.isSimilar(cursorItem))
                        {
                            int put = event.isLeftClick() ? inventoryItem.getAmount() : 1;
                            if (put > inventoryItem.getMaxStackSize() - inventoryItem.getAmount()) //if stack to big
                            {
                                put = inventoryItem.getMaxStackSize() - inventoryItem.getAmount(); //set to missing to fill
                            }
                            if (put == 0)
                                return;
                            if (put > 0)
                            {
                                if (this.manager.isIgnored(world, ITEM_INSERT)) return;
                            }
                            else
                            {
                                if (this.manager.isIgnored(world, ITEM_REMOVE)) return;
                            }
                            this.prepareForLogging(user, new ItemData(inventoryItem),put);
                        }
                        else
                        {
                            if (holder instanceof BrewingStand) // handle BrewingStands separatly
                            {
                                if (event.getRawSlot() == 3)
                                {
                                    if (!BukkitUtils.canBePlacedInBrewingstand(cursorItem.getType())) // can be put
                                    {
                                        return;
                                    }
                                }
                                else if (cursorItem.getType().equals(Material.POTION) || cursorItem.getType().equals(Material.GLASS_BOTTLE)) // bottle slot
                                {
                                    if (cursorItem.getAmount() > 1) return; // nothing happens when more than 1
                                    // else swap items
                                }
                                else
                                {
                                    return;
                                }
                            }
                            if (!this.manager.isIgnored(world, ITEM_INSERT))
                            {
                                this.prepareForLogging(user, new ItemData(cursorItem),cursorItem.getAmount());
                            }
                            if (!this.manager.isIgnored(world, ITEM_REMOVE))
                            {
                                this.prepareForLogging(user, new ItemData(inventoryItem),-inventoryItem.getAmount());
                            }
                        }
                    }
                }
            }
            else if (event.isShiftClick())// click in bottom inventory AND shift -> put | ELSE no container change
            {
                if (inventoryItem.getType().equals(Material.AIR)
                        || this.manager.isIgnored(world, ITEM_INSERT))
                {
                    return;
                }
                if (holder instanceof BrewingStand)
                {
                    BrewerInventory brewerInventory = (BrewerInventory) event.getView().getTopInventory();
                    if (BukkitUtils.canBePlacedInBrewingstand(inventoryItem.getType()))
                    {
                        if (inventoryItem.isSimilar(brewerInventory.getIngredient())) // could fit into inventory
                        {
                            ItemStack brewerItem = brewerInventory.getIngredient();
                            int amountPutIn = inventoryItem.getAmount();
                            if (brewerItem.getAmount() + inventoryItem.getAmount() > inventoryItem.getMaxStackSize())
                            {
                                amountPutIn = inventoryItem.getMaxStackSize() - brewerItem.getAmount();
                                if (amountPutIn <= 0) return;
                            }
                            this.prepareForLogging(user,new ItemData(inventoryItem), amountPutIn);
                        }
                    }
                    else if (inventoryItem.getType().equals(Material.POTION))
                    {
                        for (int i = 0 ; i <= 2; ++i)
                        {
                            ItemStack item = brewerInventory.getItem(i);
                            if (item == null) // space for a potion?
                            {
                                this.prepareForLogging(user,new ItemData(inventoryItem), inventoryItem.getAmount());
                                return;
                            }
                            // else no space found
                        }
                    }
                    else if (inventoryItem.getType().equals(Material.GLASS_BOTTLE))
                    {
                        int bottlesFound = 0;
                        int bottleSlots = 0;
                        for (int i = 0 ; i <= 2; ++i)
                        {
                            ItemStack item = brewerInventory.getItem(i);
                            if (item == null) // space for the stack ?
                            {
                                this.prepareForLogging(user,new ItemData(inventoryItem), inventoryItem.getAmount());
                                return;
                            }
                            else if (item.getType().equals(Material.GLASS_BOTTLE))
                            {
                                bottleSlots++;
                                bottlesFound += item.getAmount();
                            }
                        }
                        if (bottleSlots > 0)
                        {
                            int space = Material.GLASS_BOTTLE.getMaxStackSize() * bottleSlots - bottlesFound;
                            if (space <= 0) return;
                            int putInto = inventoryItem.getAmount();
                            if (putInto > space)
                            {
                                putInto = space;
                            }
                            this.prepareForLogging(user,new ItemData(inventoryItem), putInto);
                        }
                    }
                }
                else if (holder instanceof Furnace)
                {
                    FurnaceInventory furnaceInventory= (FurnaceInventory) event.getView().getTopInventory();
                    int putInto = 0;
                    if (BukkitUtils.isSmeltable(inventoryItem))
                    {
                        ItemStack item = furnaceInventory.getSmelting();
                        if (item == null)
                        {
                            putInto = inventoryItem.getAmount();
                        }
                        else if (inventoryItem.isSimilar(item))
                        {
                            int space = inventoryItem.getMaxStackSize() - item.getAmount();
                            if (space <= 0) return;
                            putInto = inventoryItem.getAmount();
                            if (putInto > space)
                            {
                                putInto = space;
                            }
                        }
                    }
                    else if (BukkitUtils.isFuel(inventoryItem))
                    {
                        ItemStack item = furnaceInventory.getFuel();
                        if (item == null)
                        {
                            putInto = inventoryItem.getAmount();
                        }
                        else if (inventoryItem.isSimilar(item))
                        {
                            int space = inventoryItem.getMaxStackSize() - item.getAmount();
                            if (space <= 0) return;
                            putInto = inventoryItem.getAmount();
                            if (putInto > space)
                            {
                                putInto = space;
                            }
                        }
                    }
                    if (putInto == 0) return;
                    this.prepareForLogging(user,new ItemData(inventoryItem), putInto);
                }
                else
                {
                    event.getView().getTopInventory().getContents();

                    int missingSpace = getMissingSpace(event.getView().getTopInventory(),inventoryItem);
                    int amountPut = inventoryItem.getAmount() - missingSpace;
                    if (amountPut > 0)
                    {
                        this.prepareForLogging(user, new ItemData(inventoryItem),amountPut);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event)
    {
        Inventory source = event.getSource();
        Inventory target = event.getDestination();
        if (target == null)
        {
            return; // TODO waiting for https://bukkit.atlassian.net/browse/BUKKIT-3916
        }
        Location sourceLocation = this.getLocationForHolder(source.getHolder());
        if (sourceLocation == null)
        {
            return;
        }
        Location targetLocation = this.getLocationForHolder(target.getHolder());
        if (targetLocation == null)
        {
            return;
        }
        if (this.manager.isIgnored(targetLocation.getWorld(),ITEM_TRANSFER)) return;

        String additional = this.serializeItemData(new ItemData(event.getItem()));

        this.manager.queueContainerLog(sourceLocation, ITEM_TRANSFER, null, event.getItem().getType(),
                                       event.getItem().getDurability(), source.getType().name(), additional);
    }

    private void prepareForLogging(User user, ItemData itemData, int amount)
    {
        TObjectIntHashMap<ItemData> itemDataMap = this.inventoryChanges.get(user.key);
        if (itemDataMap == null)
        {
            itemDataMap = new TObjectIntHashMap<ItemData>();
            this.inventoryChanges.put(user.key,itemDataMap);
        }
        int oldAmount = itemDataMap.get(itemData); // if not yet set this returns 0
        itemDataMap.put(itemData,oldAmount + amount);
        System.out.print((amount < 0 ? "TAKE " : "PUT ") + itemData.material.name()+":"+itemData.dura+" x"+amount);//TODO remove this
    }
}
