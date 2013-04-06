package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * experience gain
 * <p>Events: {@link PlayerExpChangeEvent}</p>
 */
public class XpPickup extends SimpleLogActionType
{
    public XpPickup(Log module)
    {
        super(module, "xp-pickup",true, PLAYER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getAmount());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        //TODO attached count total amount
        int amount = logEntry.getAdditional().iterator().next().asInt();
        user.sendTranslated("%s&2%s&a earned &6%d experience%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(), amount,loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.causer == other.causer;
    }
}
