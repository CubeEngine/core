package de.cubeisland.engine.log.action.newaction.player.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.material.Dye;

import de.cubeisland.engine.log.action.newaction.player.PlayerLogListener;

import static org.bukkit.Material.*;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link PlayerInteractEntityEvent}
 * {@link PlayerShearEntityEvent}
 * <p>Actions:
 * {@link FuelFurnaceMinecart}
 * {@link PlayerEntityDye}
 * {@link PlayerSoupFill}
 * {@link PlayerEntityShear}
 */
public class PlayerEntityListener extends PlayerLogListener
{

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        PlayerEntityActionType action;
        if (player.getItemInHand().getType() == COAL && entity instanceof PoweredMinecart)
        {
            action = this.newAction(FuelFurnaceMinecart.class, entity.getWorld());
        }
        else if(player.getItemInHand().getType() == INK_SACK && entity instanceof Sheep || entity instanceof Wolf)
        {
            action = this.newAction(PlayerEntityDye.class, entity.getWorld());
            if (action != null)
            {
                ((PlayerEntityDye)action).setColor(((Dye)player.getItemInHand().getData()).getColor());
            }
        }
        else if (player.getItemInHand().getType().equals(BOWL) && entity instanceof MushroomCow)
        {
            action = this.newAction(PlayerSoupFill.class, entity.getWorld());
        }
        else return;
        if (action != null)
        {
            action.setEntity(entity);
            action.setPlayer(player);
            action.setLocation(entity.getLocation());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        PlayerEntityShear action = this.newAction(PlayerEntityShear.class, event.getEntity().getWorld());
        if (action != null)
        {
            action.setEntity(event.getEntity());
            action.setPlayer(event.getPlayer());
            action.setLocation(event.getEntity().getLocation());
            this.logAction(action);
        }
    }
}
