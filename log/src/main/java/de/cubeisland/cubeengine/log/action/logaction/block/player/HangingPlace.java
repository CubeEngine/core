package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingPlaceEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

import static org.bukkit.Material.*;

public class HangingPlace extends BlockActionType
{
    public HangingPlace(Log module)
    {
        super(module, 0x61, "hanging-place");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            if (event.getEntity() instanceof ItemFrame)
            {
                this.logBlockChange(event.getEntity().getLocation(),event.getPlayer(),AIR,ITEM_FRAME,null);
            }
            else if (event.getEntity() instanceof Painting)
            {
                BlockData blockData = BlockData.of(PAINTING,(byte)((Painting)event.getEntity()).getArt().getId());
                this.logBlockChange(event.getEntity().getLocation(),event.getPlayer(),AIR,blockData,null);
            }
        }
    }
}
