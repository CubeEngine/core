package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * launching firework-rockets
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.interaction.RightClickActionType RightClickActionType}</p>
 */
public class FireworkUse extends SimpleLogActionType
{
    public FireworkUse(Log module)
    {
        super(module, "firework-use", true, PLAYER, ENTITY, ITEM);//TODO item
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &aused a firework rocket%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        //TODO
        return false;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).FIREWORK_USE_enable;
    }
}
