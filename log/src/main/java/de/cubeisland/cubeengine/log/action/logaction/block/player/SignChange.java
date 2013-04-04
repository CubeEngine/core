package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

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
}
