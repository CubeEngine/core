package de.cubeisland.engine.log.action.newaction.block.player.interact;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player changing the delay of a repeater
 */
public class RepeaterChange extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "repeater-change";
    // return this.lm.getConfig(world).block.REPEATER_CHANGE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof RepeaterChange
            && this.playerUUID.equals(((RepeaterChange)action).playerUUID)
            && TimeUnit.MINUTES.toMillis(2) > Math.abs(this.date.getTime() - action.date.getTime());
    }

    @Override
    public String translateAction(User user)
    {
        // TODO
        Long oldTicks = (logEntry.getData() >> 2) +1;
        Integer newTicks = (logEntry.getNewdata() >> 2) +1;
        if (this.hasAttached())
        {
            LogEntry last = this.getAttached().get(this.getAttached().size() - 1);
            newTicks = (last.getNewdata() >> 2) +1;
        }
        if (this.hasAttached() && oldTicks.intValue() == newTicks)
        {
            return user.getTranslation(POSITIVE, "{user} fiddled around with the repeater but did not change anything", this.playerName);
        }
        return user.getTranslation(POSITIVE, "{user} set the repeater to {amount} ticks delay", this.playerName, newTicks);
    }
}
