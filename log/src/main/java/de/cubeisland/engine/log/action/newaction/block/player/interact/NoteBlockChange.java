package de.cubeisland.engine.log.action.newaction.block.player.interact;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

/**
 * Represents a player changing the tune of a noteblock
 */
public class NoteBlockChange extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "noteblock-change";
    // return this.lm.getConfig(world).block.NOTEBLOCK_CHANGE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof NoteBlockChange
            && this.playerUUID.equals(((NoteBlockChange)action).playerUUID)
            && TimeUnit.MINUTES.toMillis(2) > Math.abs(this.date.getTime() - action.date.getTime());
    }

    @Override
    public String translateAction(User user)
    {
        // TODO
        Long oldClicks = logEntry.getData();
        Integer newClicks = logEntry.getNewdata().intValue();
        if (this.hasAttached())
        {
            LogEntry last = logEntry.getAttached().last();
            newClicks = last.getNewdata().intValue();
        }

        if (oldClicks.intValue() == newClicks)
        {
            return user.getTranslation(MessageType.POSITIVE, "{user} fiddled around with the noteblock but did not change anything", this.playerName);
        }
        return user.getTranslation(MessageType.POSITIVE, "{user} set the noteblock to {amount} clicks", this.playerName, newClicks);
    }
}
