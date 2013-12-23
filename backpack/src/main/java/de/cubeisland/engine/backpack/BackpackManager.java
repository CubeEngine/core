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

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;

public class BackpackManager
{
    private final Backpack module;

    public BackpackManager(Backpack module)
    {
        this.module = module;
        this.module.getCore().getCommandManager().registerCommand(new BackpackCommands(module, this));
    }

    public void openBackpack(User sender, String name, boolean global)
    {
        BackpackAttachment attachment = sender.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(sender.getWorld());
        BackpackData backPack = attachment.getBackPack(name, sender.getWorld(), global);
        if (backPack == null)
        {
            sender.sendTranslated("&cYou don't have a backpack named &6%s&c in this world!", name);
            return;
        }
        // TODO open backpack
    }

    public void createBackpack(CommandSender sender, User forUser, String name, World forWorld, boolean global, boolean single)
    {
        BackpackAttachment attachment = forUser.attachOrGet(BackpackAttachment.class, module);
        attachment.loadBackpacks(forWorld);
        BackpackData backPack = attachment.getBackPack(name, forWorld, global);
        if (backPack == null)
        {
            if (global)
            {
                attachment.createGlobalBackpack(name);
                sender.sendTranslated("&aCreated global backpack &6%s&a for &2%s", name, forUser.getName());
            }
            else if (single)
            {
                attachment.createBackpack(name, forWorld);
                sender.sendTranslated("&aCreated singleworld backpack &6%s&a for &2%s", name, forUser.getName());
            }
            else
            {
                attachment.createGroupedBackpack(name, forWorld);
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
}
