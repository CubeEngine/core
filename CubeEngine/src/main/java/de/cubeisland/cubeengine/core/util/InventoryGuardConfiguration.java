package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class InventoryGuardConfiguration implements Listener
{
    private final Core core;
    private final Inventory inventory;
    private final HashSet<User> users;
    private Module module;

    private boolean blockAllIn = false;
    private boolean blockAllOut = false;

    private HashSet<ItemStack> blockIn = new HashSet<ItemStack>();
    private HashSet<ItemStack> blockOut = new HashSet<ItemStack>();
    private HashSet<ItemStack> noBlockIn = new HashSet<ItemStack>();
    private HashSet<ItemStack> noBlockOut = new HashSet<ItemStack>();
    private HashSet<Runnable> onClose = new HashSet<Runnable>();
    private HashSet<Runnable> onChange = new HashSet<Runnable>();

    public InventoryGuardConfiguration(Core core, Inventory inventory, User[] users)
    {
        this.core = core;
        this.inventory = inventory;
        this.users = new HashSet<User>(Arrays.asList(users));
    }

    public void submitInventory(Module module, boolean openInventory)
    {
        this.module = module;
        this.module.registerListener(this);
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

    public void filter(boolean in, boolean block, ItemStack[] items)
    {
        List<ItemStack> list = Arrays.asList(items);
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
            User user = this.module.getUserManager().getExactUser((Player)event.getPlayer());
            if (user != null && this.users.contains(user))
            {
                this.users.remove(user);
                if (this.users.isEmpty())
                {
                    this.module.unregisterListener(this); // no user left to check
                }
                for (Runnable runner : this.onClose)
                {
                    runner.run();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if ((event.getInventory().equals(this.inventory)
                || (event.getInventory().getHolder() != null
                    && event.getInventory().getHolder().getInventory().equals(this.inventory)))
            && event.getWhoClicked() instanceof Player)
        {
            User user = this.module.getUserManager().getExactUser((Player)event.getWhoClicked());
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
                  || (clickTop && (cursor != null && cursor.getTypeId() != 0))) // click top with item on cursor
                {// -> PutIntoTop
                    if (this.blockAllIn) // block in
                    {
                        if (this.hasAllowIn(cursor)) // except
                        {
                            if (invent == null || invent.getTypeId() == 0 || this.hasAllowOut(invent)) // no out OR allow out
                            {
                                for (Runnable runner : this.onChange)
                                {
                                    runner.run();
                                }
                                return;
                            }
                        }
                    }
                    else
                    {
                        if (!this.hasDenyIn(cursor))
                        {
                            if (invent == null || invent.getTypeId() == 0|| this.hasAllowOut(invent)) // no out OR allow out
                            {
                                for (Runnable runner : this.onChange)
                                {
                                    runner.run();
                                }
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
                        if (this.hasAllowOut(invent))
                        {
                            for (Runnable runner : this.onChange)
                            {
                                runner.run();
                            }
                            return;
                        }
                    }
                    else
                    {
                        if (!this.hasDenyOut(invent))
                        {
                            for (Runnable runner : this.onChange)
                            {
                                runner.run();
                            }
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

    private boolean hasDenyOut(ItemStack itemStack)
    {
        for (ItemStack item : this.blockOut)
        {
            if (item.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    private boolean hasDenyIn(ItemStack itemStack)
    {
        for (ItemStack item : this.blockIn)
        {
            if (item.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    private boolean hasAllowIn(ItemStack itemStack)
    {
        for (ItemStack item : this.noBlockIn)
        {
            if (item.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    private boolean hasAllowOut(ItemStack itemStack)
    {
        for (ItemStack item : this.noBlockOut)
        {
            if (item.isSimilar(itemStack))
                return true;
        }
        return false;
    }

    public void addOnClose(Runnable run) {
        this.onClose.add(run);
    }

    public void addOnChange(Runnable run) {
        this.onChange.add(run);
    }
}
