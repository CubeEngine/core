package de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.PlayerHangingActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking an hanging entity
 * <p>SubActions:
 * {@link PlayerPaintingBreak}
 * {@link PlayerItemFrameBreak}
 */
public class PlayerHangingBreak extends PlayerHangingActionType
{


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerPaintingBreak
            && ((PlayerPaintingBreak)action).playerUUID.equals(this.playerUUID);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{text:One} {name#hanging} got removed by {user}",
                                    "{3:amount} {name#hanging} got removed by {user}",
                                    this.hangingType.name(), this.playerName, count);
    }

    public void setCause(ActionTypeBase action)
    {
        // TODO reference
    }
}
