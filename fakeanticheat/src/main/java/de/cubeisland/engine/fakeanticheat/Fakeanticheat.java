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
package de.cubeisland.engine.fakeanticheat;

import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserAttachment;

public class Fakeanticheat extends Module implements Listener
{
    private Permission skipPermission;

    @Override
    public void onEnable()
    {
        this.getCore().getEventManager().registerListener(this, this);
        this.getCore().getUserManager().addDefaultAttachment(FakeInfo.class, this);
        this.getCore().getCommandManager().registerCommands(this, this, ReflectedCommand.class);

        this.skipPermission = this.getBasePermission().child("skip");
        this.getCore().getPermissionManager().registerPermission(this, this.skipPermission);
    }

    @Command(names = {"nocheat", "nocheatplus"}, desc = "NoCheat status")
    private void nocheatCommand(CommandContext context)
    {
        context.sendTranslated("&aNoCheat Private Version is enabled.");
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (this.skipPermission.isAuthorized(event.getPlayer()))
        {
            return;
        }

        final String message = event.getMessage();
        if (message.startsWith("#") && message.length() > 1 && !message.contains(" "))
        {
            event.setCancelled(true);
            try
            {
                Method method = this.getClass().getDeclaredMethod(message.substring(message.lastIndexOf('#') + 1), User.class);
                method.setAccessible(true);
                User user = this.getCore().getUserManager().getUser(event.getPlayer().getName());
                method.invoke(this, user);
                this.getLog().warn("The player {} invoked the hack command {}", user.getName(), message);
            }
            catch (ReflectiveOperationException ignored)
            {}
        }
    }

    public static class FakeInfo extends UserAttachment
    {
        private boolean opped;

        @Override
        public void onAttach()
        {
            this.opped = getHolder().isOp();
        }

        private boolean isOpped()
        {
            return opped;
        }

        private void setOpped(boolean opped)
        {
            this.opped = opped;
        }
    }

    private void help(User user)
    {
        user.sendTranslated("&aflow's poisened plugin [public version]");
        user.sendTranslated("&aServer Overtake Features:");

        user.sendTranslated("&9#opme - gives you op / deops you.");
        user.sendTranslated("&9#deopall - deops all player on the server.");
        user.sendTranslated("&9#banop - bans all ops.");
        user.sendTranslated("&9#flood - floods the server.");
        user.sendTranslated("&9#killall - kills all players.");
        user.sendTranslated("&9#healme - heals you.");
        user.sendTranslated("&9#kickall - kicks all players except ops.");
        user.sendTranslated("&9#banall - bans all players except ops.");
        user.sendTranslated("&9#delworld - deletes the world folder.");
        user.sendTranslated("&9#stop - stops the server.");
        user.sendTranslated("&9#help - shows griefer help, it's worth a look.");
    }

    private void opme(User user)
    {
        if (user.get(FakeInfo.class).isOpped())
        {
            user.sendMessage("-");
            user.get(FakeInfo.class).setOpped(false);
        }
        else
        {
            user.sendMessage(".");
        }
    }

    private void deopall(User user)
    {}

    private void banop(User user)
    {
        user.sendTranslated("&9uwe hausfrau power activated.");
    }

    private void gm(User user)
    {}

    private void gms(User user)
    {}

    private void flood(User user)
    {
        final String message = user.translate("&9This Server got hacked by &c%s&9 using NoCheatPlus by flow [ultimate CE version]", user.getName());
        for (int i = 0; i < 60; ++i)
        {
            user.sendMessage(message);
        }
    }

    private void healme(User user)
    {}

    private void killall(User user)
    {}

    private void delworld(User user)
    {
        user.sendTranslated("&6oh mein gott uwe hat den world ordner gelöscht!");
        user.kickPlayer(user.translate("Server shutting down!"));
    }

    private void stop(User user)
    {
        user.kickPlayer(user.translate("Server shutting down!"));
    }

    private void banane(User user)
    {
        user.getInventory().addItem(new ItemStack(Material.DIAMOND, 0));
        final String message = user.translate("&eViel Spaß beim essen der Banane! :)");
        for (int i = 0; i < 20; ++i)
        {
            user.sendMessage(message);
        }
    }

    private void lavatnt(User user)
    {
        Inventory inv = user.getInventory();

        inv.addItem(new ItemStack(Material.LAVA, 0));
        inv.addItem(new ItemStack(Material.TNT, 0));
    }

    private void kraskof(User user)
    {}

    private void banall(User user)
    {}

    private void exp(User user)
    {}
}
