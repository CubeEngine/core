package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Using bonemeal
 * <p>Events: {@link RightClickActionType}</p>
 */
public class BonemealUse extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "bonemeal-use";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        Material mat = Material.getMaterial(logEntry.getAdditional().iterator().next().asText());
        user.sendTranslated("&2%s &aused bonemeal on &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            new de.cubeisland.cubeengine.log.storage.BlockData(mat));
    }
    //TODO override issimilar


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BONEMEAL_USE_enable;
    }
}
