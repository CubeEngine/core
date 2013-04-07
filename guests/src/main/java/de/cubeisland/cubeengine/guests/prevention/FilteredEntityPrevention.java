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
package de.cubeisland.cubeengine.guests.prevention;

import de.cubeisland.cubeengine.guests.Guests;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.EntityType;

/**
 * This class represents a filterable Prevention related to Materials.
 */
public abstract class FilteredEntityPrevention extends
    FilteredPrevention<EntityType>
{
    public FilteredEntityPrevention(final String name, final Guests guests)
    {
        this(name, guests, true);
    }

    public FilteredEntityPrevention(String name, Guests guests, boolean allowPunishing)
    {
        super(name, guests, allowPunishing);
        setFilterItems(EnumSet.of(EntityType.CREEPER));
        setFilterMode(FilterMode.NONE);
    }

    @Override
    public List<String> encodeSet(Set<EntityType> set)
    {
        List<String> types = super.encodeSet(set);

        for (int i = 0; i < types.size(); ++i)
        {
            types.set(i, types.get(i).toLowerCase().replace('_', ' '));
        }

        return types;
    }

    @Override
    public Set<EntityType> decodeList(List<String> list)
    {
        Set<EntityType> types = EnumSet.noneOf(EntityType.class);

        for (String entry : list)
        {
            EntityType type = EntityType.fromName(entry.replace(' ', '_'));
            if (type != null)
            {
                types.add(type);
            }
        }

        return types;
    }
}
