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
package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Changing a signs text.
 * <p>Events: {@link SignChangeEvent}</p>
 */
public class SignChange extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
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

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).SIGN_CHANGE_enable;
    }

    @Override
    public boolean rollback(LogEntry logEntry, boolean force)
    {
        if (!force) // Signs have to be attached first!
        {
            return false;
        }
        de.cubeisland.cubeengine.log.storage.BlockData oldBlock = logEntry.getOldBlock();
        Block block = logEntry.getLocation().getBlock();
        if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST))
        {
            Sign sign = (Sign)block.getState();
            ArrayNode oldSign = (ArrayNode)logEntry.getAdditional().get("oldSign");
            if (oldSign == null)
            {
                oldSign = (ArrayNode)logEntry.getAdditional().get("sign"); // For old logs saving oldSign wrongly as sign
            }
            sign.setLine(0,oldSign.get(0).textValue());
            sign.setLine(1,oldSign.get(1).textValue());
            sign.setLine(2,oldSign.get(2).textValue());
            sign.setLine(3,oldSign.get(3).textValue());
            sign.update();
            return true;
        }
        return false; // No sign at Position!
    }
    // TODO redo overwrite
}
