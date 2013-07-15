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

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredPrevention;

/**
 * Prevents users from trading.
 */
public class TradingPrevention extends FilteredPrevention<Profession>
{
    public TradingPrevention(Guests guests)
    {
        super("trading", guests);
        setFilterMode(FilterMode.NONE);
        setFilterItems(EnumSet.allOf(Profession.class));
    }

    @Override
    public Set<Profession> decodeList(List<String> list)
    {
        Set<Profession> professions = EnumSet.noneOf(Profession.class);

        for (String name : list)
        {
            professions.add(Profession.valueOf(name.trim().toUpperCase(Locale.ENGLISH)));
        }

        return professions;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked() instanceof Villager && !can(event.getPlayer()))
        {
            prevent(event, event.getPlayer(), ((Villager)event.getRightClicked()).getProfession());
        }
    }
}
