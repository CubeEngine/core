/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.logaction.block.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * Changing a signs text.
 * <p>Events: {@link SignChangeEvent}</p>
 */
public class SignChange extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, PLAYER));
    }

    @Override
    public String getName()
    {
        return "sign-change";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            String[] oldLines = ((Sign)event.getBlock().getState()).getLines();
            ObjectNode json = this.om.createObjectNode();
            ArrayNode sign = json.putArray("sign");
            ArrayNode oldSign = json.putArray("oldSign");
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
        ArrayList<String> oldLines = new ArrayList<>();
        ArrayList<String> newLines = new ArrayList<>();
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
        String delim = ChatFormat.GREY + " | " + ChatFormat.WHITE;
        if (oldEmpty)
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} wrote {input#signtext} on a sign{}", time, logEntry.getCauserUser().getDisplayName(), StringUtils.implode(delim, newLines), loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} wrote {input#signtext}",time, logEntry.getCauserUser().getDisplayName(), StringUtils.implode(delim,newLines));
            user.sendTranslated(MessageType.POSITIVE, "    The old signtext was {input#signtext}{}", StringUtils.implode(delim,oldLines), loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.SIGN_CHANGE_enable;
    }
}
