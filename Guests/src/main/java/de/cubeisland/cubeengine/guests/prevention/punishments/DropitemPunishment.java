package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Drops the player players held item.
 */
public class DropitemPunishment implements Punishment
{
    private final Location helper = new Location(null, 0, 0, 0);

    @Override
    public String getName()
    {
        return "dropitem";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().dropItemNaturally(player.getLocation(this.helper), player.getItemInHand()).setPickupDelay(config.getInt("pickupDelay", 4) * 20);
        player.getInventory().clear(player.getInventory().getHeldItemSlot());
    }
}
