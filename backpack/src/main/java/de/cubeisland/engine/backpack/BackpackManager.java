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
package de.cubeisland.engine.backpack;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class BackpackManager implements Listener
{
    private final Backpack module;

    public BackpackManager(Backpack module)
    {
        this.module = module;
        this.module.getCore().getCommandManager().registerCommand(new BackpackCommands(module, this));
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    public void openBackpack(User sender, User forUser, World forWorld, String name)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventories backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            if (sender != forUser)
            {
                sender.sendTranslated(NEGATIVE, "{user} does not have a backpack named {input#backpack} in this world!", forUser, name);
                return;
            }
            sender.sendTranslated(NEGATIVE, "You don't have a backpack named {input#backpack} in this world!", name);
            return;
        }
        backPack.openInventory(sender);
    }

    public void createBackpack(CommandSender sender, User forUser, String name, World forWorld, boolean global, boolean single, boolean blockInput, Integer pages, Integer size)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventories backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            if (global)
            {
                attachment.createGlobalBackpack(name, blockInput, pages, size);
                sender.sendTranslated(POSITIVE, "Created global backpack {input#backpack} for {user}", name, forUser);
            }
            else if (single)
            {
                attachment.createBackpack(name, forWorld, blockInput, pages, size);
                sender.sendTranslated(POSITIVE, "Created singleworld backpack {input#backpack} in {world} for {user}", name, forWorld, forUser);
            }
            else
            {
                attachment.createGroupedBackpack(name, forWorld, blockInput, pages, size);
                sender.sendTranslated(POSITIVE, "Created grouped backpack {input#backpack} in {world} for {user}", name, forWorld, forUser);
            }
        }
        else
        {
            if (sender == forUser)
            {
                sender.sendTranslated(NEGATIVE, "A backpack named {input#backpack} already exists in {world}", name, forWorld);
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "{user} already had a backpack named {input#backpack} in {world}", forUser, name, forWorld);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player
            && event.getInventory().getHolder() instanceof BackpackHolder)
        {
            if (event.getSlotType() == SlotType.OUTSIDE)
            {
                if (event.isLeftClick())
                {
                    ((BackpackHolder)event.getInventory().getHolder()).getBackpack().showNextPage((Player)event.getWhoClicked());
                }
                else if (event.isRightClick())
                {
                    ((BackpackHolder)event.getInventory().getHolder()).getBackpack().showPrevPage((Player)event.getWhoClicked());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getPlayer() instanceof Player
            && event.getInventory().getHolder() instanceof BackpackHolder)
        {
            ((BackpackHolder)event.getInventory().getHolder()).getBackpack().closeInventory((Player)event.getPlayer());
        }
    }

    public void giveItem(CommandSender sender, User forUser, World forWorld, String name, ItemStack itemToGive)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventories backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            if (sender != forUser)
            {
                sender.sendTranslated(NEGATIVE, "{user} does not have a backpack named {input#backpack} in this world!", forUser, name);
                return;
            }
            sender.sendTranslated(NEGATIVE, "You don't have a backpack named {input#backpack} in this world!", name);
            return;
        }
        backPack.addItem(itemToGive);
        sender.sendTranslated(POSITIVE, "Item added to backpack!");
        if (sender != forUser && forUser.isOnline())
        {
            forUser.sendTranslated(POSITIVE, "You received items in your backpack {input#backpack}", name);
        }
    }

    public void modifyBackpack(CommandSender sender, User forUser, String name, World forWorld, Integer pages, Boolean blockInput, Integer size)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventories backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            if (sender != forUser)
            {
                sender.sendTranslated(NEGATIVE, "{user} does not have a backpack named {input#backpack} in this world!", forUser, name);
                return;
            }
            sender.sendTranslated(NEGATIVE, "You don't have a backpack named {input#backpack} in this world!", name);
            return;
        }
        if (pages != null)
        {
            if (backPack.data.contents.size() > pages * backPack.data.size)
            {
                sender.sendTranslated(NEGATIVE, "Could not change page amount! Not enough space!");
            }
            else
            {
                if (backPack.data.pages > pages) // compact inventory
                {
                    Collection<ItemStack> values = backPack.data.contents.values();
                    backPack.data.contents = new HashMap<>();
                    int i = 0;
                    for (ItemStack value : values)
                    {
                        backPack.data.contents.put(i++, value);
                    }
                }
                backPack.data.pages = pages;
                sender.sendTranslated(POSITIVE, "Pages changed!");
            }
        }
        if (size != null)
        {
            if (backPack.data.contents.size() > size * backPack.data.pages)
            {
                sender.sendTranslated(NEGATIVE, "Could not change page size! Not enough space!");
            }
            else
            {
                if (backPack.data.size > size) // compact inventory
                {
                    Collection<ItemStack> values = backPack.data.contents.values();
                    backPack.data.contents = new HashMap<>();
                    int i = 0;
                    for (ItemStack value : values)
                    {
                        backPack.data.contents.put(i++, value);
                    }
                }
                backPack.data.size = size;
                sender.sendTranslated(POSITIVE, "Page-size changed!");
            }
        }
        if (blockInput != null)
        {
            backPack.data.allowItemsIn = !blockInput;
            if (blockInput)
            {
                sender.sendTranslated(POSITIVE, "Items are not allowed to go in!");
            }
            else
            {
                sender.sendTranslated(POSITIVE, "Items are allowed to go in!");
            }
        }
        backPack.data.save();
    }
}
