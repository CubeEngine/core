package de.cubeisland.cubeengine.guests.prevention;

import de.cubeisland.cubeengine.guests.Guests;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.EntityType;

/**
 * This class represents a filterable Prevention related to Materials.
 */
public abstract class FilteredEntityPrevention extends FilteredPrevention<EntityType>
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
