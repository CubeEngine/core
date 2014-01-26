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
package de.cubeisland.engine.basics.command.moderation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.user.User;

public class PaintingListener implements Listener
{
    private final Basics module;
    private Map<String, Painting> paintingChange;

    public PaintingListener(Basics module)
    {
        this.module = module;
        this.paintingChange = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked().getType() == EntityType.PAINTING)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());

            if (!module.perms().CHANGEPAINTING.isAuthorized(user))
            {
                user.sendTranslated("&cYou are not allowed to change the painting.");
                return;
            }
            Painting painting = (Painting)event.getRightClicked();

            Painting playerPainting = this.paintingChange.get(user.getName());
            if (playerPainting == null)
            {
                this.paintingChange.put(user.getName(), painting);
                user.sendTranslated("&aYou can now cycle through the paintings using your mousewheel.");
            }
            else
            {
                this.paintingChange.remove(user.getName());
                user.sendTranslated("&aPainting is locked now");
            }
        }
    }

    private int compareSlots(int previousSlot, int newSlot)
    {
        if(previousSlot == 8 && newSlot == 0)
        {
            newSlot = 9;
        }
        if(previousSlot == 0 && newSlot == 8)
        {
            newSlot = -1;
        }
        return Integer.compare(previousSlot, newSlot);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent event)
    {
        if (!this.paintingChange.isEmpty())
        {
            Painting painting = this.paintingChange.get(event.getPlayer().getName());

            if (painting != null)
            {
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                final int maxDistanceSquared = this.module.getConfiguration().maxChangePaintingDistance * this.module
                    .getConfiguration().maxChangePaintingDistance;

                if (painting.getLocation().toVector()
                            .distanceSquared(user.getLocation().toVector()) > maxDistanceSquared)
                {
                    this.paintingChange.remove(user.getName());
                    user.sendTranslated("&aPainting is locked now");
                    return;
                }

                Art[] arts = Art.values();
                int artNumber = painting.getArt().ordinal();
                int change = this.compareSlots(event.getPreviousSlot(), event.getNewSlot());
                artNumber += change;
                if (artNumber >= arts.length)
                {
                    artNumber = 0;
                }
                else if(artNumber < 0)
                {
                    artNumber = arts.length - 1;
                }
                for (int i = 0; i < arts.length; i++)
                {
                    if (painting.setArt(arts[artNumber]))
                    {
                        return;
                    }
                    artNumber += change;
                    if (artNumber >= arts.length)
                    {
                        artNumber = 0;
                    }
                    if (artNumber == -1)
                    {
                        artNumber = arts.length - 1;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPaintingBreakEvent(HangingBreakEvent event)
    {
        if (!(event.getEntity() instanceof Painting))
        {
            return;
        }

        Painting painting = (Painting)event.getEntity();

        Iterator<Entry<String, Painting>> paintingIterator = this.paintingChange.entrySet().iterator();
        while(paintingIterator.hasNext())
        {
            Entry<String, Painting> entry = paintingIterator.next();
            if(entry.getValue().equals(painting))
            {
                this.module.getCore().getUserManager().getExactUser(entry.getKey()).sendTranslated("&cThe painting broke");
                paintingIterator.remove();
            }
        }
    }
}
