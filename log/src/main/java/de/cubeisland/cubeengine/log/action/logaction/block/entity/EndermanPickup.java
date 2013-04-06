package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * Enderman picking up blocks.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class EndermanPickup  extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENTITY);
    }

    @Override
    public String getName()
    {
        return "enderman-pickup";
    }


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot picked up by an enderman%s!",
                            logEntry.getOldBlock());
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENDERMAN_PICKUP_enable;
    }
}
