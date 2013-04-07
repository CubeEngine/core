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
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * This class represents a filterable Prevention related to Materials.
 */
public abstract class FilteredItemPrevention extends
    FilteredPrevention<Material>
{
    private boolean ignoreBlocks = false;

    public FilteredItemPrevention(final String name, final Guests guests)
    {
        this(name, guests, true);
    }

    public FilteredItemPrevention(String name, Guests guests, boolean allowPunishing)
    {
        super(name, guests, allowPunishing);
        setFilterItems(EnumSet.of(Material.DIRT));
        setFilterMode(FilterMode.NONE);
    }

    /**
     * Sets whether this prevention should ignore blocks
     *
     * @param ignore true to ignore blocks
     */
    public final void setIgnoreBlocks(boolean ignore)
    {
        this.ignoreBlocks = ignore;
    }

    /**
     * Returns whether this prevention ignores blocks
     *
     * @return true if it ignores blocks
     */
    public final boolean getIgnoreBlocks()
    {
        return this.ignoreBlocks;
    }

    @Override
    public List<String> encodeSet(Set<Material> set)
    {
        List<String> materials = super.encodeSet(set);

        for (int i = 0; i < materials.size(); ++i)
        {
            materials.set(i, materials.get(i).toLowerCase().replace('_', ' '));
        }

        return materials;
    }

    @Override
    public Set<Material> decodeList(List<String> list)
    {
        Set<Material> materials = EnumSet.noneOf(Material.class);

        for (String entry : list)
        {
            Material material = Material.matchMaterial(entry);
            if (material != null)
            {
                materials.add(material);
            }
        }

        return materials;
    }

    /**
     * This method checks whether the player can do the subaction
     *
     * @param player   the player
     * @param material the material
     * @return true if he can
     */
    @Override
    public boolean can(final Player player, final Material material)
    {
        if (this.ignoreBlocks && material.getId() <= 256)
        {
            return true;
        }
        else
        {
            return super.can(player, material);
        }
    }
}
