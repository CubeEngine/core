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
package de.cubeisland.engine.locker.storage;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.locker.Locker;

import static de.cubeisland.engine.core.util.ChatFormat.DARK_RED;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.Material.PAPER;
import static org.bukkit.Sound.*;

public class KeyBook
{
    public static final String TITLE = ChatFormat.RESET.toString() + ChatFormat.GOLD + "KeyBook " + ChatFormat.DARK_GREY + "#";
    public final ItemStack item;
    public final User currentHolder;
    private final Locker module;
    public final long lockID;
    private final String keyBookName;

    private KeyBook(ItemStack item, User currentHolder, Locker module)
    {
        this.item = item;
        this.currentHolder = currentHolder;
        this.module = module;
        keyBookName = item.getItemMeta().getDisplayName();
        lockID = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
    }

    public static KeyBook getKeyBook(ItemStack item, User currentHolder, Locker module)
    {
        if (item.getType() == Material.ENCHANTED_BOOK &&
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().contains(KeyBook.TITLE))
        {
            try
            {
                return new KeyBook(item, currentHolder, module);
            }
            catch (NumberFormatException|IndexOutOfBoundsException ignore)
            {}
        }
        return null;
    }

    public boolean check(Lock lock, Location effectLocation)
    {
        if (lock.getId().equals(lockID)) // Id matches ?
        {
            // Validate book
            if (this.isValidFor(lock))
            {
                if (effectLocation != null) currentHolder.sendTranslated(POSITIVE, "As you approach with your KeyBook the magic lock disappears!");
                currentHolder.playSound(effectLocation, PISTON_EXTEND, 1, 2);
                currentHolder.playSound(effectLocation, PISTON_EXTEND, 1, (float)1.5);
                if (effectLocation != null) lock.notifyKeyUsage(currentHolder);
                return true;
            }
            else
            {
                currentHolder.sendTranslated(NEGATIVE, "You try to open the container with your KeyBook\n" +
                                        "but you get forcefully pushed away!");
                this.invalidate();
                currentHolder.playSound(effectLocation, GHAST_SCREAM, 1, 1);
                final Vector userDirection = currentHolder.getLocation().getDirection();
                currentHolder.damage(1);
                currentHolder.setVelocity(userDirection.multiply(-3));
                return false;
            }
        }
        else
        {
            currentHolder.sendTranslated(NEUTRAL, "You try to open the container with your KeyBook but nothing happens!");
            currentHolder.playSound(effectLocation, BLAZE_HIT, 1, 1);
            currentHolder.playSound(effectLocation, BLAZE_HIT, 1, (float)0.8);
            return false;
        }
    }

    public void invalidate()
    {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatFormat.parseFormats(DARK_RED + "Broken KeyBook"));
        itemMeta.setLore(Arrays.asList(ChatFormat // TODO translate as one object
               .parseFormats(currentHolder.getTranslation(NEUTRAL, "This KeyBook")), ChatFormat
               .parseFormats(currentHolder.getTranslation(NEUTRAL, "looks old and")), ChatFormat
               .parseFormats(currentHolder.getTranslation(NEUTRAL, "used up. It")), ChatFormat
               .parseFormats(currentHolder.getTranslation(NEUTRAL, "wont let you")), ChatFormat
               .parseFormats(currentHolder.getTranslation(NEUTRAL, "open any containers!"))));
        item.setItemMeta(itemMeta);
        item.setType(PAPER);
        currentHolder.updateInventory();
    }

    public boolean isValidFor(Lock lock)
    {
        boolean b = keyBookName.startsWith(lock.getColorPass());
        if (!b)
        {
            this.module.getLog().debug("Invalid KeyBook detected! {}|{}", lock.getColorPass(), keyBookName);
        }
        return b;
    }
}
