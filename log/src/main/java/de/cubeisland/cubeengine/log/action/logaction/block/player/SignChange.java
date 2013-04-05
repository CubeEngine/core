package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SignChange extends BlockActionType
{
    public SignChange(Log module)
    {
        super(module, 0x42, "sign-change");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            String[] oldLines = ((Sign)event.getBlock().getState()).getLines();
            ObjectNode json = this.om.createObjectNode();
            ArrayNode sign = json.putArray("sign");
            ArrayNode oldSign = json.putArray("oldSign"); //TODO debug this is not saved correctly
            boolean isEmpty = true;
            boolean wasEmpty = true;
            for (String line : event.getLines())
            {
                if (!line.isEmpty())
                {
                    isEmpty = false;
                }
                sign.add(line);
            }
            for (String line : oldLines)
            {
                if (!line.isEmpty())
                {
                    wasEmpty = false;
                }
                oldSign.add(line);
            }
            if (wasEmpty && isEmpty) return;
            this.logBlockChange(event.getPlayer(),event.getBlock().getState(),event.getBlock().getState(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        Iterator<JsonNode> oldSignIterator = logEntry.getAdditional().get("oldSign").iterator();
        Iterator<JsonNode> newSignIterator = logEntry.getAdditional().get("sign").iterator();
        boolean oldEmpty = true;
        ArrayList<String> oldLines = new ArrayList<String>();
        ArrayList<String> newLines = new ArrayList<String>();
        while (oldSignIterator.hasNext())
        {
            String line = oldSignIterator.next().asText();
            if (!line.isEmpty())
            {
                oldEmpty = false;
            }
            oldLines.add(line);
        }
        while (newSignIterator.hasNext())
        {
            String line = newSignIterator.next().asText();
            newLines.add(line);
        }
        String delim = ChatFormat.parseFormats("&7 | &f");
        if (oldEmpty)
        {
            user.sendTranslated("%s&2%s &awrote &7[&f%s&7]&a on a sign%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                StringUtils.implode(delim, newLines),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &awrote &7[&f%s&7]&a on a sign%s&a! \nThe old signtext was &7[&f%s&7]&a!",
                                time, logEntry.getCauserUser().getDisplayName(),
                                StringUtils.implode(delim,newLines), loc,
                                StringUtils.implode(delim,oldLines));
        }
    }
}
