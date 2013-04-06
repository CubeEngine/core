package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;

/**
 * lightning-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class LightningIgnite extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }
    @Override
    public String getName()
    {
        return "lightning-ignite";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire got set by a lightning strike%s&a!",time,loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LIGHTNING_IGNITE_enable;
    }
}
