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
package org.cubeengine.libcube.service.inventoryguard;

import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryGuard
{
    private EventManager em;
    private TaskManager tm;
    private Inventory inventory;
    private Container container;
    private final HashSet<UUID> users;

    private boolean blockAllIn = false;
    private boolean blockAllOut = false;

    private final HashSet<GuardedItemStack> blockIn = new HashSet<>();
    private final HashSet<GuardedItemStack> blockOut = new HashSet<>();
    private final HashSet<GuardedItemStack> noBlockIn = new HashSet<>();
    private final HashSet<GuardedItemStack> noBlockOut = new HashSet<>();
    private final HashSet<Runnable> onClose = new HashSet<>();
    private final HashSet<Runnable> onChange = new HashSet<>();

    private boolean ignoreRepaircost = true;
    private Class owner;

    public InventoryGuard(EventManager em, TaskManager tm, Inventory inventory, UUID[] users)
    {
        this.em = em;
        this.tm = tm;
        this.inventory = inventory instanceof Container ? inventory.first().parent() : inventory;
        this.container = inventory instanceof Container ? (Container) inventory : null;
        this.users = new HashSet<>(Arrays.asList(users));
    }

    public void setIgnoreRepaircost(boolean set)
    {
        this.ignoreRepaircost = set;
    }

    public void submitInventory(Class owner, boolean openInventory)
    {
        this.owner = owner;
        em.registerListener(owner, this);
        if (openInventory)
        {
            for (UUID user : users)
            {
                Optional<Player> player = Sponge.getServer().getPlayer(user);
                if (player.isPresent())
                {
                    this.container = player.get().openInventory(this.inventory, Cause.of(NamedCause.source(player))).orElse(null);
                    // TODO check if not opened
                }
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

    @Listener
    public void onInventoryClose(InteractInventoryEvent.Close event, @First Player player)
    {
        if ((event.getTargetInventory().equals(this.container)))
        {
            if (this.users.contains(player.getUniqueId()))
            {
                this.users.remove(player.getUniqueId());
                if (this.users.isEmpty())
                {
                    em.removeListener(owner, this); // no user left to check
                }
                this.onClose.forEach(Runnable::run);
            }
        }
    }

    @Listener
    public void onInventoryInteract(ClickInventoryEvent event)
    {
        if (!event.getTargetInventory().equals(this.container) && !event.getTargetInventory().first().parent().equals(this.inventory))
        {
            return;
        }
        if (!blockAllIn && !blockAllOut && blockIn.isEmpty() && blockOut.isEmpty())
        {
            return;
        }
        System.out.print("Event:\n");
        boolean cancel = false;

        for (SlotTransaction transaction : event.getTransactions())
        {
            ItemStack origStack = transaction.getOriginal().createStack();
            ItemStack finalStack = transaction.getFinal().createStack();
            String origString = origStack.getItem().equals(ItemTypes.NONE) ? origStack.getItem().getId() :origStack.getItem().getId() + " " + origStack.getQuantity();
            String finalString = finalStack.getItem().equals(ItemTypes.NONE) ? finalStack.getItem().getId() :finalStack.getItem().getId() + " " + finalStack.getQuantity();
            System.out.print(origString + "->" + finalString + "\n");

            System.out.println("SI: " + transaction.getSlot().getProperty(SlotIndex.class, "slotindex").map(si -> si.getValue()).orElse(-1) + " " + transaction.getSlot().parent().capacity());
            boolean upper = !(transaction.getSlot().parent() instanceof PlayerInventory); // TODO this will not work when viewing another playerinventory
           // Inventory next = event.getTargetInventory().iterator().next() == transaction.getSlot().parent().notify();

            if (upper)
            {
                if (checkTransaction(event, transaction, origStack, finalStack))
                {
                    cancel = true;
                }
                else
                {
                    runOnChange();
                }
            }
            // else lower inventory was affected ; actually we don't care (yet)
        }
        if (cancel)
        {
            System.out.print("Cancelled\n");
        }
        System.out.print("\n");
    }

    private boolean checkTransaction(ClickInventoryEvent event, SlotTransaction transaction, ItemStack origStack, ItemStack finalStack)
    {
        if (!transaction.getOriginal().equals(transaction.getFinal()))
        {
            if (ItemTypes.NONE.equals(transaction.getOriginal().getType())) // Putting Item in Top Inventory
            {
                if (hasBlockIn(event, origStack, finalStack)) return true;
            }
            else if (ItemTypes.NONE.equals(transaction.getFinal().getType())) // Taking Item out of Top Inventory
            {
                if (hasBlockOut(event, origStack, finalStack)) return true;
            }
            else // Swapping Item in Top Inventory
            {
                if (hasBlockIn(event, origStack, finalStack)) return true;
                if (hasBlockOut(event, origStack, finalStack)) return true;
            }
        }
        return false;
    }

    private boolean hasBlockOut(ClickInventoryEvent event, ItemStack origStack, ItemStack finalStack)
    {
        if (blockAllOut)
        {
            if (!this.hasAllowOut(origStack, finalStack))
            {
                event.setCancelled(true);
                return true;
            }
        }
        else
        {
            if (!this.hasDenyOut(origStack))
            {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private boolean hasBlockIn(ClickInventoryEvent event, ItemStack origStack, ItemStack finalStack)
    {
        if (blockAllIn)
        {
            if (!this.hasAllowIn(finalStack, origStack))
            {
                event.setCancelled(true);
                return true;
            }
        }
        else
        {
            if (!this.hasDenyIn(finalStack))
            {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private void runOnChange()
    {
        for (Runnable runner : this.onChange)
        {
            tm.runTask(owner, runner);
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

    private boolean hasAllowIn(ItemStack itemStackToGoIn, ItemStack itemStackAtPosition)
    {
        for (GuardedItemStack guardedItem : this.noBlockIn)
        {
            if (guardedItem.isSimilar(itemStackToGoIn, this.ignoreRepaircost))
            {
                if (guardedItem.amount == 0) return true;
                int amountIn = this.inventory.query(itemStackAtPosition).totalItems();
                System.out.print("AllowInCheck: " + amountIn + "\n");
                return amountIn <= guardedItem.amount;
            }
        }
        return false;
    }


    private boolean hasAllowOut(ItemStack itemStackToOut, ItemStack item)
    {
        for (GuardedItemStack guardedItem : this.noBlockOut)
        {
            if (guardedItem.isSimilar(itemStackToOut, this.ignoreRepaircost))
            {
                if (guardedItem.amount == 0) return true;
                int amountIn = this.inventory.query(itemStackToOut).totalItems();
                System.out.print("AllowOutCheck: " + amountIn + "\n");
                return amountIn >= guardedItem.amount;
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
