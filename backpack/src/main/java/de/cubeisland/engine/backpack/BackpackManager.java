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

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;

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
        // TODO backpacks of other players
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventory backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            sender.sendTranslated("&cYou don't have a backpack named &6%s&c in this world!", name);
            return;
        }
        backPack.openInventory(sender);
    }

    public void createBackpack(CommandSender sender, User forUser, String name, World forWorld, boolean global, boolean single, boolean blockInput, Integer pages)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackInventory backPack = attachment.getBackpack(name, forWorld);
        if (backPack == null)
        {
            if (global)
            {
                attachment.createGlobalBackpack(name, blockInput, pages);
                sender.sendTranslated("&aCreated global backpack &6%s&a for &2%s", name, forUser.getName());
            }
            else if (single)
            {
                attachment.createBackpack(name, forWorld, blockInput, pages);
                sender.sendTranslated("&aCreated singleworld backpack &6%s&a for &2%s", name, forUser.getName());
            }
            else
            {
                attachment.createGroupedBackpack(name, forWorld, blockInput, pages);
                sender.sendTranslated("&aCreated grouped backpack &6%s&a in &6%s&a for &2%s", name, forWorld.getName(), forUser.getName());
            }
        }
        else
        {
            if (sender == forUser)
            {
                sender.sendTranslated("&cA backpack named &6%s&c already exists in &6%s", name, forWorld.getName());
            }
            else
            {
                sender.sendTranslated("&2%s&c already had a backpack named &6%s&c in &6%s", forUser.getName(), name, forWorld.getName());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player
            && event.getInventory().getHolder() instanceof BackpackInventory)
        {
            if (event.getSlotType() == SlotType.OUTSIDE)
            {
                if (event.isLeftClick())
                {
                    ((BackpackInventory)event.getInventory().getHolder()).showNextPage((Player)event.getWhoClicked());
                }
                else if (event.isRightClick())
                {
                    ((BackpackInventory)event.getInventory().getHolder()).showPrevPage((Player)event.getWhoClicked());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getPlayer() instanceof Player
            && event.getInventory().getHolder() instanceof BackpackInventory)
        {
            ((BackpackInventory)event.getInventory().getHolder()).closeInventory((Player)event.getPlayer());
        }
    }
}
