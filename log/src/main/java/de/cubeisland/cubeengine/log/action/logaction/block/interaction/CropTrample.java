package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Trampling Crops
 * <p>Events: {@link RightClickActionType}</p>
 */
public class CropTrample extends BlockActionType

{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "crop-trample";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        // TODO attached log only show the crop trampled down then
        user.sendTranslated("&2%s &atrampeled down &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            logEntry.getOldBlock());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).CROP_TRAMPLE_enable;
    }
}
