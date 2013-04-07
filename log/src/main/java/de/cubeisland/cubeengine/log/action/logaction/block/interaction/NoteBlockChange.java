package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Changing NoteblockNotes
 * <p>Events: {@link RightClickActionType}</p>
 */
public class NoteBlockChange extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "noteblock-change";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int clicks = logEntry.getNewBlock().data;
        user.sendTranslated("%s&2%s &aset the noteblock to &6%d&a clicks%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(), clicks,loc);
        // TODO attach (show the actual change no change -> fiddled around but did not change anything)
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).NOTEBLOCK_CHANGE_enable;
    }
}
