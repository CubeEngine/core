package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredPrevention;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

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
    public Set<Profession> decodeList(List<String> list) {
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
