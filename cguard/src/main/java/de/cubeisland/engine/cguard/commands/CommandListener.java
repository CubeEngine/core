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
package de.cubeisland.engine.cguard.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.GuardAttachment;
import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.Triplet;

import static de.cubeisland.engine.cguard.commands.CommandListener.CommandType.*;
import static de.cubeisland.engine.cguard.storage.AccessListModel.ACCESS_ALL;
import static de.cubeisland.engine.cguard.storage.AccessListModel.ACCESS_FULL;
import static de.cubeisland.engine.cguard.storage.TableGuards.*;

public class CommandListener implements Listener
{
    private Map<String, Triplet<CommandType, String, Boolean>> map = new HashMap<>();
    private Set<String> persist = new HashSet<>();

    private Cguard module;
    private GuardManager manager;

    public CommandListener(Cguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    public void setCommandType(User user, CommandType guardtype, String pass, boolean createKeyBook)
    {
        map.put(user.getName(), new Triplet<>(guardtype, pass, createKeyBook));
        if (persist.contains(user.getName()))
        {
            user.sendTranslated("&ePersist mode is active. Your command will be repeated until reusing &6/cpersist");
        }
    }

    public void setCommandType(User user, CommandType modify, String players)
    {
        map.put(user.getName(), new Triplet<>(modify, players, false));
        if (persist.contains(user.getName()))
        {
            user.sendTranslated("&ePersist mode is active. Your command will be repeated until reusing &6/cpersist");
        }
    }

    /**
     * Toggles the persist mode for given user
     *
     * @param sender
     * @return true if persist mode is on for given user
     */
    public boolean persist(User sender)
    {
        if (persist.contains(sender.getName()))
        {
            persist.remove(sender.getName());
            this.map.remove(sender.getName());
            return false;
        }
        persist.add(sender.getName());
        return true;
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event)
    {
        if (!map.keySet().contains(event.getPlayer().getName())) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() != null)
        {
            // TODO check if block is allowed for protections
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            Guard guard = this.manager.getGuardAtLocation(event.getClickedBlock().getLocation());
            Triplet<CommandType, String, Boolean> triplet = map.get(user.getName());
            if (triplet.getFirst().isCreator())
            {
                if (guard != null)
                {
                    user.sendTranslated("&eThis block is already protected!");
                    event.setCancelled(true);
                    return;
                }
            }
            else if (guard == null)
            {
                user.sendTranslated("&6No protection detected here!");
                event.setCancelled(true);
                return;
            }
            switch (triplet.getFirst())
            {
            case C_PRIVATE:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_PRIVATE.guardType, triplet.getSecond());
                user.sendTranslated("&cPrivate Protection Created!");
                this.attemptCreatingKeyBook(guard, user, triplet.getThird());
                // TODO print short info
                break;
            case C_PUBLIC:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_PUBLIC.guardType, triplet.getSecond());
                user.sendTranslated("&cPublic Protection Created!");
                break;
            case C_DONATION:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_DONATION.guardType, triplet.getSecond());
                user.sendTranslated("&cDonation Protection Created!");
                this.attemptCreatingKeyBook(guard, user, triplet.getThird());
                break;
            case C_FREE:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_FREE.guardType, triplet.getSecond());
                user.sendTranslated("&cFree Protection Created!");
                this.attemptCreatingKeyBook(guard, user, triplet.getThird());
                break;
            case C_GUARDED:
                guard = this.manager.createGuard(event.getClickedBlock().getType(), event.getClickedBlock().getLocation(), user, C_GUARDED.guardType, triplet.getSecond());
                user.sendTranslated("&cGuard Protection Created!");
                this.attemptCreatingKeyBook(guard, user, triplet.getThird());
                break;
            case INFO:
                user.sendTranslated("PROTECTION FOUND: TODO INFO"); // TODO
                break;
            case MODIFY:
                if (!guard.isOwner(user))
                {
                    if (!guard.hasAdmin(user))
                    {
                        user.sendTranslated("&cYou are not allowed to modify the access-list of this protection!");
                        return;
                    }
                }
                String[] explode = StringUtils.explode(",", triplet.getSecond());
                for (String name : explode)
                {
                    boolean add = true;
                    if (name.startsWith("-"))
                    {
                        name = name.substring(1);
                        add = false;
                    }
                    User modifyUser = this.module.getCore().getUserManager().getUser(name, false);
                    if (modifyUser == null) throw new IllegalArgumentException(); // This is prevented by checking first in the cmd execution
                    short accessType = ACCESS_FULL;
                    if (add)
                    {
                        if (triplet.getThird())
                        {
                            accessType = ACCESS_ALL; // + AdminAccess
                        }
                    }
                    if (this.manager.setAccess(guard, modifyUser, add, accessType))
                    {
                        if (add)
                        {
                            if (triplet.getThird())
                            {
                                user.sendTranslated("&aGranted &2%s&a admin access to this protection!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aGranted &2%s&a access to this protection!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&aRemoved &2%s's&a access to this protection!", modifyUser.getName());
                        }
                    }
                    else
                    {
                        if (add)
                        {
                            if (triplet.getThird())
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to admin access!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to normal access!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&2%s&a had no access to this protection!", modifyUser.getName());
                        }
                    }
                }
                break;
            case REMOVE:
                if (!guard.isOwner(user))
                {
                    // TODO perm
                    user.sendTranslated("&cThis protection is not yours!");
                   return;
                }
                this.manager.removeGuard(guard);
                break;
            case UNLOCK:
                if (guard.hasPass())
                {
                    if (this.manager.checkPass(guard, triplet.getSecond()))
                    {
                        user.sendTranslated("&aUpon hearing the right pass-phrase the magic surrounding the container gets thinner and lets you pass!");
                        user.playSound(event.getClickedBlock().getLocation(), Sound.PISTON_EXTEND, 1, 2);
                        user.playSound(event.getClickedBlock().getLocation(), Sound.PISTON_EXTEND, 1, (float)1.5);
                        user.attachOrGet(GuardAttachment.class, this.module).addUnlock(guard);
                    }
                    else
                    {
                        user.sendTranslated("&eSudden pain makes you realize this was not the right pass-phrase!");
                        user.damage(0);
                    }
                }
                else
                {
                    user.sendTranslated("&eYou try to open the container with a pass-phrase but nothing changes!");
                }
                break;
            case INVALIDATE_KEYS:
                if (!guard.isOwner(user))
                {
                    user.sendTranslated("&cThis is not your protection!");
                }
                else if (guard.hasPass())
                {
                    user.sendTranslated("&eYou cannot invalidate BookKeys for password protected chests. &aChange the password to invalidate them!");
                }
                else
                {
                    this.manager.invalidateKeyBooks(guard);
                }
                break;
            case KEYS:
                if (!guard.isOwner(user))
                {
                    user.sendTranslated("&cThis is not your protection!");
                }
                else
                {
                    this.attemptCreatingKeyBook(guard, user, triplet.getThird());
                }
                break;
            }
            if (!this.persist.contains(user.getName()))
            {
                this.map.remove(user.getName());
            }
            event.setCancelled(true);
        }
    }

    private void attemptCreatingKeyBook(Guard guard, User user, Boolean third)
    {
        if (third)
            if (user.getItemInHand().getType().equals(Material.BOOK))
            {
                int amount = user.getItemInHand().getAmount() -1;
                ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(guard.getColorPass() + ChatFormat.parseFormats("&r&6KeyBook &8#" + guard.getId()));
                itemMeta.setLore(Arrays.asList(user.translate(ChatFormat.parseFormats("&eThis book can")), user
                                                   .translate(ChatFormat.parseFormats("&eunlock a magically")), user
                                                   .translate(ChatFormat.parseFormats("&elocked container"))));
                itemStack.setItemMeta(itemMeta);
                user.setItemInHand(itemStack);
                HashMap<Integer, ItemStack> full = user.getInventory().addItem(new ItemStack(Material.BOOK, amount));
                for (ItemStack stack : full.values())
                {
                    Location location = user.getLocation();
                    location.getWorld().dropItem(location, stack);
                }
                user.updateInventory();
            }
            else
            {
                user.sendTranslated("&cCould not create KeyBook! You need to hold a book in your hand in order to do this!");
            }
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event)
    {
        if (!map.keySet().contains(event.getPlayer().getName())) return;

        // TODO check if entity is allowed
    }

    public enum CommandType
    {
        C_PRIVATE(GUARDTYPE_PRIVATE),
        C_PUBLIC(GUARDTYPE_PUBLIC),
        C_DONATION(GUARDTYPE_DONATION),
        C_FREE(GUARDTYPE_FREE),
        C_GUARDED(GUARDTYPE_GUARDED),
        // TODO
        INFO,
        MODIFY,
        REMOVE,
        UNLOCK,
        INVALIDATE_KEYS,
        KEYS
        ;

        private CommandType(byte guardType)
        {
            this.guardType = guardType;
        }

        private CommandType()
        {
            guardType = null;
        }

        public final Byte guardType;

        public boolean isCreator()
        {
            return guardType != null;
        }
    }

}
