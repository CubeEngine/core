package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.ITEM_FRAME;
import static org.bukkit.Material.PAINTING;

public class HangingBreak extends BlockActionType
{
    public HangingBreak(Log module)
    {
        super(module, 0x63, "hanging-break");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS))
        {
            if (!this.isActive(event.getEntity().getWorld())) return;
            Location location = event.getEntity().getLocation();
            Entity causer = this.plannedHangingBreak.get(location);
            if (causer != null)
            {
                if (event.getEntity() instanceof ItemFrame)
                {
                    ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                    String itemInFrame = itemStack == null ? null : new ItemData(itemStack).serialize(this.om);
                    this.logBlockChange(location,causer,ITEM_FRAME,AIR,itemInFrame);
                }
                else if (event.getEntity() instanceof Painting)
                {
                    this.queueLog(location,causer,PAINTING.name(),1L*((Painting)event.getEntity()).getArt().getId(),AIR.name(),(byte)0,null);
                }
            }
            else
            {
                System.out.print("Unexpected HangingBreakEvent");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        if (!this.isActive(event.getEntity().getWorld())) return;
        Location location = event.getEntity().getLocation();
        if (event.getRemover() instanceof Player || event.getRemover() instanceof Projectile)
        {
            Entity causer = null;
            if (event.getRemover() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getRemover();
                if (projectile.getShooter() != null)
                {
                    causer = projectile.getShooter();
                }
            }
            else if (event.getRemover() instanceof Player)
            {
               causer = event.getRemover();
            }
            if (event.getEntity() instanceof ItemFrame)
            {
                ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                String itemInFrame = itemStack == null ? null : new ItemData(itemStack).serialize(this.om);
                this.logBlockChange(location,causer,ITEM_FRAME,AIR,itemInFrame);
            }
            else if (event.getEntity() instanceof Painting)
            {
                this.queueLog(location,causer,PAINTING.name(),1L*((Painting)event.getEntity()).getArt().getId(),AIR.name(),(byte)0,null);
            }
        }
        else
            System.out.print("Not a player breaking Hanging?");
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Entity> plannedHangingBreak = new ConcurrentHashMap<Location,Entity>();
    public void preplanHangingBreak(Location location, Entity player)
    {
        plannedHangingBreak.put(location, player);
        if (!clearPlanned)
        {
            clearPlanned = true;
            HangingBreak.this.logModule.getCore().getTaskManager().scheduleSyncDelayedTask(logModule, new Runnable() {
                @Override
                public void run() {
                    clearPlanned = false;
                    HangingBreak.this.plannedHangingBreak.clear();
                }
            });
        }
    }
}
