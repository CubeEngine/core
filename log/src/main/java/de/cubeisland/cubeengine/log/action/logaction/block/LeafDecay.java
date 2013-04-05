package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static org.bukkit.Material.AIR;

public class LeafDecay extends BlockActionType
{

    public LeafDecay(Log module)
    {
        super(module, 0x03, "leaf-decay");
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getBlock().getLocation(),null,
                                BlockData.of(event.getBlock().getState()),
                                AIR,null);
        }
    }
    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &adecayed%s!",
                            time,logEntry.getOldBlock(),loc);
    }

}
