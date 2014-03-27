package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player eating a cake
 */
public class CakeEat extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "cake-eat";
    // return this.lm.getConfig(world).block.CAKE_EAT_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        int piecesLeft = 6 - logEntry.getNewBlock().data; // TODO
        if (piecesLeft == 0)
        {
            return user.getTranslation(POSITIVE, "The cake is a lie! Ask {user} he knows it!", this.playerName);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} ate a piece of cake", this.playerName);
        }
    }
}
