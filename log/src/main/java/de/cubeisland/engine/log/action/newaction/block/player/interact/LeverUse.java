package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a lever
 */
public class LeverUse extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "lever-use";
    // return this.lm.getConfig(world).block.LEVER_USE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LeverUse
            && this.coord.compareTo(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        // TODO plural
        if ((logEntry.getNewBlock().data & 0x8) == 0x8)
        {
            return user.getTranslation(POSITIVE, "{user} activated the lever",
                                       this.playerName);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} deactivated the lever",
                                       this.playerName);
        }
    }
}
