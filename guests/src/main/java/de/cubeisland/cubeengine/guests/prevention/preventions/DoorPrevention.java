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
package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredItemPrevention;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Prevents door usage.
 */
public class DoorPrevention extends FilteredItemPrevention
{
    private static final EnumSet<Material> DOORS = EnumSet.of(Material.WOODEN_DOOR, Material.IRON_DOOR, Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, Material.FENCE_GATE);

    public DoorPrevention(Guests guests)
    {
        super("door", guests);
        setEnableByDefault(true);
    }

    @Override
    public Set<Material> decodeList(List<String> list)
    {
        Set<Material> materials = super.decodeList(list);

        EnumSet<Material> doors = EnumSet.noneOf(Material.class);
        for (Material material : materials)
        {
            if (DOORS.contains(material))
            {
                doors.add(material);
            }
        }

        return doors;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event)
    {
        final Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
        {
            final Material material = event.getClickedBlock().getType();
            if (DOORS.contains(material))
            {
                prevent(event, event.getPlayer(), material);
            }
        }
    }
}
