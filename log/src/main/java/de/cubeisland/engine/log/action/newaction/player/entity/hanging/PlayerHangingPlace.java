package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player placing a hanging entity
 */
public class PlayerHangingPlace extends PlayerHangingActionType
{
    // return "hanging-place";
    // return this.lm.getConfig(world).HANGING_PLACE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerHangingPlace
            && this.player.equals(((PlayerHangingPlace)action).player)
            && ((PlayerHangingPlace)action).hangingType == this.hangingType;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{name#hanging} got hung up by {user}",
                                    "{name#hanging} got hung up by {user} {amount} times",
                                    this.hangingType.name(), this.player.name, count);
    }
}
