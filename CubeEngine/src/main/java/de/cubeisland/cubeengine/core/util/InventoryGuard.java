package de.cubeisland.cubeengine.core.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;

public class InventoryGuard implements Listener
{
    private final Core core;
    private final Inventory inventory;
    private final HashSet<User> users;
    private Module module;

    private boolean blockAllIn = false;
    private boolean blockAllOut = false;

    private HashSet<GuardedItemStack> blockIn = new HashSet<GuardedItemStack>();
    private HashSet<GuardedItemStack> blockOut = new HashSet<GuardedItemStack>();
    private HashSet<GuardedItemStack> noBlockIn = new HashSet<GuardedItemStack>();
    private HashSet<GuardedItemStack> noBlockOut = new HashSet<GuardedItemStack>();
    private HashSet<Runnable> onClose = new HashSet<Runnable>();
    private HashSet<Runnable> onChange = new HashSet<Runnable>();

    public InventoryGuard(Core core, Inventory inventory, User[] users)
    {
        this.core = core;
        this.inventory = inventory;
        this.users = new HashSet<User>(Arrays.asList(users));
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
            User user = this.module.getCore().getUserManager().getExactUser((Player)event.getPlayer());
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
    public void onInventoryClick(InventoryClickEvent event)
    {
        if ((event.getInventory().equals(this.inventory)
                || (event.getInventory().getHolder() != null
                    && event.getInventory().getHolder().getInventory().equals(this.inventory)))
            && event.getWhoClicked() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser((Player)event.getWhoClicked());
            if (user != null && this.users.contains(user))
            {
                if (event.getSlot() == -999)
                {
                    return;
                }
                ItemStack cursor = event.getCursor();
                ItemStack invent = event.getCurrentItem();
                boolean clickTop = event.getRawSlot() < event.getInventory().getSize();

                if ((!clickTop && event.isShiftClick()) // shift-click bot
                    || (clickTop && !event.isShiftClick() && (cursor != null && cursor.getTypeId() != 0))) // click top with item on cursor (but not shift->out)
                {// -> PutIntoTop
                    if (this.blockAllIn) // block in
                    {
                        if (clickTop && this.hasAllowIn(cursor,clickTop,event.isRightClick())) // except topClick allowIn on cursor AND ...
                        {
                            if (invent == null || invent.getTypeId() == 0 || this.hasAllowOut(invent, cursor, event.isShiftClick(), event.isLeftClick())) // no out OR allow out
                            {
                                this.runOnChange();
                                return;
                            }
                        }
                        else if (!clickTop && this.hasAllowIn(invent,clickTop,event.isRightClick())) // except botClick (and shift) and allow in that item
                        {
                            this.runOnChange();
                            return;
                        }
                    }
                    else
                    {
                        if (!this.hasDenyIn(cursor))
                        {
                            if (invent == null || invent.getTypeId() == 0 || this.hasAllowOut(invent, cursor, event.isShiftClick(), event.isLeftClick())) // no out OR allow out
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
                if (clickTop) // -> TakeOutOfTop
                {
                    // exchange is already handled above
                    if (this.blockAllOut)
                    {
                        if (this.hasAllowOut(invent, cursor, event.isShiftClick(), event.isLeftClick()))
                        {
                            this.runOnChange();
                            return;
                        }
                    }
                    else
                    {
                        if (!this.hasDenyOut(invent))
                        {
                            this.runOnChange();
                            return;
                        }
                    }
                    event.setCancelled(true);
                    user.updateInventory();
                    return;
                }
            }
        }
    }

    private void runOnChange()
    {
        for (Runnable runner : this.onChange)
        {
            this.module.getCore().getTaskManager().scheduleSyncDelayedTask(this.module,runner);
        }
    }

    private boolean hasDenyOut(ItemStack itemStack)
    {
        for (GuardedItemStack guardedItem : this.blockOut)
        {
            if (guardedItem.item.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    private boolean hasDenyIn(ItemStack itemStack)
    {
        for (GuardedItemStack guardedItem : this.blockIn)
        {
            if (guardedItem.item.isSimilar(itemStack))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasAllowIn(ItemStack itemStack, boolean clickTop, boolean rightClick)
    {
        for (GuardedItemStack guardedItem : this.noBlockIn)
        {
            if (guardedItem.item.isSimilar(itemStack))
            {
                if (guardedItem.amount == 0)
                {
                    return true;
                }
                int amountIn = InventoryUtil.getAmountOf(this.inventory,itemStack);
                if (clickTop)
                {
                    if (rightClick)
                    {
                        return amountIn + 1 <= guardedItem.amount;
                    }
                    else
                    {
                        return amountIn + itemStack.getAmount() <= guardedItem.amount;
                        //TODO handle if filling up stack in inventory would not go over the limited amount
                    }
                }
                else
                {
                    return amountIn + itemStack.getAmount() <= guardedItem.amount;
                }
            }
        }
        return false;
    }

    private boolean hasAllowOut(ItemStack itemStack, ItemStack cursor, boolean shift, boolean leftClick)
    {
        for (GuardedItemStack guardedItem : this.noBlockOut)
        {
            if (guardedItem.item.isSimilar(itemStack))
            {
                return true;
            }
            int amountIn = InventoryUtil.getAmountOf(this.inventory,itemStack);
            if (cursor == null || cursor.getTypeId() == 0)
            {
                if (shift || leftClick)
                {
                    return amountIn - itemStack.getAmount() >= guardedItem.amount;
                }
                else if (!leftClick)
                {
                    return amountIn - ((itemStack.getAmount()+1)/2) >= guardedItem.amount; //TODO check if this is the right amount
                }
            }
            else
            {
                return amountIn - itemStack.getAmount() >= guardedItem.amount;
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
