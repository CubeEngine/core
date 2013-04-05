package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class BonemealUse extends BlockActionType
{
    public BonemealUse(Log module)
    {
        super(module, 0x44, "bonemeal-use");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        Material mat = Material.getMaterial(logEntry.getAdditional().iterator().next().asText());
        user.sendTranslated("&2%s &aused bonemeal on &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            new de.cubeisland.cubeengine.log.storage.BlockData(mat));
    }
}
