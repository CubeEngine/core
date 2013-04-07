package de.cubeisland.cubeengine.log.action.logaction.worldedit;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

public class WorldEditActionType extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER,BLOCK);
    }

    @Override
    public String getName()
    {
        return "worldedit";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getNewBlock().material.equals(Material.AIR))
        {
            user.sendTranslated("&2%s &aused worldedit to remove &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock());
        }
        else if (logEntry.getOldBlock().material.equals(Material.AIR))
        {
            user.sendTranslated("&2%s &aused worldedit to place &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getNewBlock());
        }
        else
        {
            user.sendTranslated("&2%s &aused worldedit to replace &6%s&a with &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),
                                logEntry.getNewBlock());
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).WORLDEDIT_enable;
    }
}
