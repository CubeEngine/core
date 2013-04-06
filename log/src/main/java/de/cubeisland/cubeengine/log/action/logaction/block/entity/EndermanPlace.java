package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * Enderman placing blocks.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class EndermanPlace extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENTITY);
    }

    @Override
    public String getName()
    {
        return "enderman-place";
    }


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot placed by an enderman%s&a!",
                            time,logEntry.getNewBlock(),loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENDERMAN_PLACE_enable;
    }
}
