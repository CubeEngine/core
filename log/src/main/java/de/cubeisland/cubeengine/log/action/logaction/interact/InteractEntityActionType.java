package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.DyeColor;
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

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.cubeengine.log.action.logaction.container.ItemInsert;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static org.bukkit.Material.*;

/**
 * Container-ActionType for interaction
 * <p>Events: {@link PlayerInteractEntityEvent}</p>
 * <p>External Actions:
 * {@link ItemInsert},
 * {@link EntityDye},
 * {@link SoupFill}
 */
public class InteractEntityActionType extends ActionTypeContainer
{
    public InteractEntityActionType(Log module)
    {
        super(module, "INTERACT_ENTITY");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (player.getItemInHand().getType().equals(COAL) && entity instanceof PoweredMinecart)
        {
            ItemInsert itemInsert = this.manager.getActionType(ItemInsert.class);
            if (itemInsert.isActive(player.getWorld()))
            {
                ItemData itemData = new ItemData(player.getItemInHand());
                itemData.amount = 1;
                itemInsert.logSimple(entity.getLocation(),player,entity,itemData.serialize(this.om));
            }
        }
        else if(player.getItemInHand().getType().equals(INK_SACK) && entity instanceof Sheep || entity instanceof Wolf)
        {
            EntityDye entityDye = this.manager.getActionType(EntityDye.class);
            if (entityDye.isActive(entity.getWorld()))
            {
                String additional = entityDye.serializeData(null, entity,
                            DyeColor.getByDyeData(player.getItemInHand().getData().getData()));
                entityDye.logSimple(entity.getLocation(),player,entity,additional);
            }
        }
        else if (player.getItemInHand().getType().equals(BOWL) && entity instanceof MushroomCow)
        {
            SoupFill soupFill = this.manager.getActionType(SoupFill.class);
            if (soupFill.isActive(player.getWorld()))
            {
                soupFill.logSimple(entity.getLocation(),player,entity,null);
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        throw new UnsupportedOperationException();
    }
}
