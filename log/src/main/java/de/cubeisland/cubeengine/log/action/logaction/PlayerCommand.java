package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player commands
 * <p>Events: {@link PlayerCommandPreprocessEvent}</p>
 */
public class PlayerCommand extends SimpleLogActionType
{
    public PlayerCommand(Log module)
    {
        super(module, "player-command",true, PLAYER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        //TODO attach same cmd
        if (event.getMessage().trim().isEmpty()) return;
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getMessage());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("&2%s&a used the command &f\"&6%s&f\"&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            logEntry.getAdditional().iterator().next().asText());
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.additional.iterator().next().asText().equals(other.additional.iterator().next().asText());
    }
}
