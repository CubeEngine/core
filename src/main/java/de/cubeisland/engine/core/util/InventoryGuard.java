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
package de.cubeisland.engine.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;

public class InventoryGuard implements Listener
{
    private final Inventory inventory;
    private final HashSet<User> users;
    private Module module;

    private boolean blockAllIn = false;
    private boolean blockAllOut = false;

    private final HashSet<GuardedItemStack> blockIn = new HashSet<>();
    private final HashSet<GuardedItemStack> blockOut = new HashSet<>();
    private final HashSet<GuardedItemStack> noBlockIn = new HashSet<>();
    private final HashSet<GuardedItemStack> noBlockOut = new HashSet<>();
    private final HashSet<Runnable> onClose = new HashSet<>();
    private final HashSet<Runnable> onChange = new HashSet<>();

    private boolean ignoreRepaircost = true;

    public InventoryGuard(Core core, Inventory inventory, User[] users)
    {
        this.inventory = inventory;
        this.users = new HashSet<>(Arrays.asList(users));
    }

    public void setIgnoreRepaircost(boolean set)
    {
        this.ignoreRepaircost = set;
    }

    public void submitInventory(Module module, boolean openInventory)
    {
        this.module = module;
        this.module.getCore().getEventManager().registerListener(this.module, this);
        if (openInventory)
        {
            for (User user : users)
            {
                user.openInventory(this.inventory);
            }
        }
    }

    public void blockAll(boolean in)
    {
        if (in)
        {
            blockAllIn = true;
        }
        else
        {
            blockAllOut = true;
        }
    }

    public void filter(boolean in, boolean block, List<GuardedItemStack> list)
    {
        if (in)
        {
            if (block)
            {
                this.blockIn.addAll(list);
                this.noBlockIn.removeAll(list);
            }
            else
            {
                this.noBlockIn.addAll(list);
                this.blockIn.removeAll(list);
            }
        }
        else
        {
            if (block)
            {
                this.blockOut.addAll(list);
                this.noBlockOut.removeAll(list);
            }
            else
            {
                this.noBlockOut.addAll(list);
                this.blockOut.removeAll(list);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if ((event.getInventory().equals(this.inventory)
                || (event.getInventory().getHolder() != null
                && event.getInventory().getHolder().getInventory().equals(this.inventory)))
                && event.getPlayer() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            if (user != null && this.users.contains(user))
            {
                this.users.remove(user);
                if (this.users.isEmpty())
                {
                    this.module.getCore().getEventManager().removeListener(this.module, this); // no user left to check
                }
                for (Runnable runner : this.onClose)
                {
                    runner.run();
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if ((event.getInventory().equals(this.inventory)
            || (event.getInventory().getHolder() != null
            && event.getInventory().getHolder().getInventory().equals(this.inventory)))
            && event.getWhoClicked() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getWhoClicked().getUniqueId());
            if (user != null && this.users.contains(user))
            {
                boolean affectsTop = false;
                for (Integer slot : event.getInventorySlots())
                {
                    if (slot < event.getInventory().getSize())
                    {
                        affectsTop = true;
                        break;
                    }
                }
                if (affectsTop)
                {
                    if (this.blockAllIn)
                    {
                        for (GuardedItemStack guardedItem : this.noBlockIn)
                        {
                            if (guardedItem.isSimilar(event.getOldCursor(), this.ignoreRepaircost))
                            {
                                if (guardedItem.amount == 0)
                                {
                                    this.runOnChange();
                                    return;
                                }
                                int amount = 0;
                                for (Entry<Integer, ItemStack> entry : event.getNewItems().entrySet())
                                {
                                    if (entry.getKey() < event.getInventory().getSize())
                                    {
                                        amount += entry.getValue().getAmount();
                                    }
                                }
                                int amountIn = InventoryUtil.getAmountOf(this.inventory,event.getOldCursor());
                                if (amountIn + amount <= guardedItem.amount)
                                {
                                    this.runOnChange();
                                    return;
                                }
                            }
                        }
                        event.setCancelled(true);
                        user.updateInventory();
                        return;
                    }
                    else if (this.hasDenyIn(event.getOldCursor()))
                    {
                        event.setCancelled(true);
                        user.updateInventory();
                        return;
                    }
                }
                this.runOnChange();
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onInventoryClick(InventoryClickEvent event)
    {
        if ((event.getInventory().equals(this.inventory)
                || (event.getInventory().getHolder() != null
                    && event.getInventory().getHolder().getInventory().equals(this.inventory)))
            && event.getWhoClicked() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getWhoClicked().getUniqueId());
            if (user != null && this.users.contains(user))
            {
                if (event.getAction().equals(InventoryAction.NOTHING))
                {
                    return;
                }
                switch (event.getAction())
                {
                    case UNKNOWN:
                        System.out.print("######################## Unknown Inventory Stuff!?");
                        event.setCancelled(true);
                        user.updateInventory();
                    return;
                    case NOTHING:
                    return;
                }
                ItemStack cursor = event.getCursor();
                ItemStack invent = event.getCurrentItem();
                boolean clickTop = event.getRawSlot() < event.getInventory().getSize();
                if (clickTop)
                {
                    switch (event.getAction()) // ClickTop Pickup
                    {
                        case PICKUP_ALL:
                        case PICKUP_SOME:
                        case PICKUP_HALF:
                        case PICKUP_ONE:
                        case SWAP_WITH_CURSOR:
                        case MOVE_TO_OTHER_INVENTORY:
                        case HOTBAR_MOVE_AND_READD:
                        case COLLECT_TO_CURSOR:
                        case DROP_ALL_SLOT:
                        case DROP_ONE_SLOT:
                        case HOTBAR_SWAP:
                            if (this.blockAllOut) // Block all out except allowed
                            {
                                if (!this.hasAllowOut(invent, cursor, event.getAction())) // Check allowed (& amount)
                                {
                                    event.setCancelled(true);
                                    user.updateInventory();
                                    return;
                                }
                            }
                            else if (this.hasDenyOut(invent)) // Allow all out except denied
                            {
                                event.setCancelled(true);
                                user.updateInventory();
                                return;
                            }
                    }
                    switch (event.getAction())
                    {
                        case PLACE_ALL:
                        case PLACE_ONE:
                        case PLACE_SOME:
                        case SWAP_WITH_CURSOR:
                            if (this.blockAllIn) // Block all in except allowed
                            {
                                if (!this.hasAllowIn(cursor, invent, event.getAction())) // Check allowed (& amount)
                                {
                                    event.setCancelled(true);
                                    user.updateInventory();
                                    return;
                                }
                            }
                            else if (this.hasDenyIn(cursor)) // Allow all in except denied
                            {
                                event.setCancelled(true);
                                user.updateInventory();
                                return;
                            }
                            break;
                        case HOTBAR_SWAP:
                            if (user.getInventory() == event.getView().getBottomInventory()) // inventories do match
                            {
                                if (this.blockAllIn) // Block all in except allowed
                                {
                                    if (!this.hasAllowIn(user.getInventory().getItem(event.getHotbarButton()), invent, event.getAction()))
                                    {
                                        event.setCancelled(true);
                                        user.updateInventory();
                                        return;
                                    }
                                }
                                else if (this.hasDenyIn(user.getInventory().getItem(event.getHotbarButton()))) // Allow all in except denied
                                {
                                    event.setCancelled(true);
                                    user.updateInventory();
                                    return;
                                }
                            } // else: How could that happen block it!
                    }
                    this.runOnChange();
                }
                else if (event.isShiftClick()) // Bot ShiftClick / assuming full stack movement
                {
                    if (this.blockAllIn) // Block all in except allowed
                    {
                        if (this.hasAllowIn(invent, null, event.getAction())) // Check allowed (& amount)
                        {
                            this.runOnChange();
                            return;
                        }
                    }
                    else if (!this.hasDenyIn(invent)) // Allow all in except denied
                    {
                        this.runOnChange();
                        return;
                    }
                    event.setCancelled(true);
                    user.updateInventory();
                }
                else if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR))
                {
                    if (this.blockAllOut)
                    {
                        boolean found = false;
                        for (ItemStack itemStack : this.inventory.getContents())
                        {
                            if (itemStack != null && itemStack.isSimilar(cursor))
                            {
                                found = true;
                                break;
                            }
                        }
                        if (found)
                        {
                            int amountIn = InventoryUtil.getAmountOf(this.inventory, cursor);
                            ItemStack clone = cursor.clone();
                            clone.setAmount(Math.min(amountIn, clone.getMaxStackSize()));
                            if (this.hasAllowOut(clone, null, InventoryAction.PICKUP_ALL))
                            {
                                this.runOnChange();
                                return;
                            }
                            event.setCancelled(true);
                            user.updateInventory();
                        }
                    }
                    else if (this.hasDenyOut(invent))
                    {
                        event.setCancelled(true);
                        user.updateInventory();
                    }
                }
            }
        }
    }

    private void runOnChange()
    {
        for (Runnable runner : this.onChange)
        {
            this.module.getCore().getTaskManager().runTask(this.module, runner);
        }
    }

    private boolean hasDenyOut(ItemStack itemStack)
    {
        for (GuardedItemStack guardedItem : this.blockOut)
        {
            if (guardedItem.isSimilar(itemStack, this.ignoreRepaircost))
                return true;
        }
        return false;
    }

    private boolean hasDenyIn(ItemStack itemStack)
    {
        for (GuardedItemStack guardedItem : this.blockIn)
        {
            if (guardedItem.isSimilar(itemStack, this.ignoreRepaircost))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasAllowIn(ItemStack itemStackToGoIn, ItemStack itemStackAtPosition, InventoryAction action)
    {
        for (GuardedItemStack guardedItem : this.noBlockIn)
        {
            if (guardedItem.isSimilar(itemStackToGoIn, this.ignoreRepaircost))
            {
                if (guardedItem.amount == 0) return true;
                int amountIn = InventoryUtil.getAmountOf(this.inventory,itemStackToGoIn);
                switch (action)
                {
                    case PLACE_ALL:
                    case SWAP_WITH_CURSOR:
                    case HOTBAR_SWAP:
                        return amountIn + itemStackToGoIn.getAmount() <= guardedItem.amount;
                    case PLACE_ONE:
                        return amountIn + 1 <= guardedItem.amount;
                    case PLACE_SOME:
                        if (itemStackAtPosition == null)
                        {
                            return amountIn + itemStackToGoIn.getAmount() <= guardedItem.amount; // Not sure what happens assume full stack
                        }
                        int missing = itemStackToGoIn.getMaxStackSize() - itemStackAtPosition.getAmount();
                        if (missing < 0)
                        {
                            return amountIn + itemStackToGoIn.getAmount() <= guardedItem.amount; // Not sure what happens assume full stack
                        }
                        return amountIn + missing <= guardedItem.amount; // Missing filling up
                }
            }
        }
        return false;
    }

    private boolean hasAllowOut(ItemStack itemStackToOut, ItemStack item, InventoryAction action)
    {
        for (GuardedItemStack guardedItem : this.noBlockOut)
        {
            if (guardedItem.isSimilar(itemStackToOut, this.ignoreRepaircost))
            {
                if (guardedItem.amount == 0) return true;
                int amountIn = InventoryUtil.getAmountOf(this.inventory,itemStackToOut);
                switch (action)
                {
                case PICKUP_ALL:
                case HOTBAR_MOVE_AND_READD:
                case SWAP_WITH_CURSOR:
                case HOTBAR_SWAP:
                case DROP_ALL_SLOT:
                case MOVE_TO_OTHER_INVENTORY: // assume the getSuggestions stack
                    return amountIn - itemStackToOut.getAmount() >= guardedItem.amount;
                case PICKUP_SOME:
                    if (item == null)
                    {
                        return amountIn - itemStackToOut.getAmount() >= guardedItem.amount; //no idea what happens assume full stack to be sure
                    }
                    if (itemStackToOut.getAmount() > itemStackToOut.getMaxStackSize()) // overstacked in inventory
                    {
                        // items that are over max stacksize get moved to cursor
                        return amountIn - (itemStackToOut.getAmount() - itemStackToOut.getMaxStackSize()) >= guardedItem.amount;
                    }
                    return amountIn - itemStackToOut.getAmount() >= guardedItem.amount; //no idea what happens assume full stack to be sure
                case PICKUP_HALF:
                    return amountIn - ((itemStackToOut.getAmount()+1)/2) >= guardedItem.amount;
                case PICKUP_ONE:
                case DROP_ONE_SLOT:
                    return amountIn - 1 >= guardedItem.amount;
                case COLLECT_TO_CURSOR:
                    return amountIn - Math.min(amountIn, itemStackToOut.getAmount()) >= guardedItem.amount;
                }
                System.out.print("#################################### This should be impossible");
            }
        }
        return false;
    }

    public void addOnClose(Runnable run)
    {
        this.onClose.add(run);
    }

    public void addOnChange(Runnable run)
    {
        this.onChange.add(run);
    }
}
