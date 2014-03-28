package de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy;

import org.bukkit.Art;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 *  Represents a player breaking an image
 */
public class PlayerPaintingBreak extends PlayerHangingBreak
{
    public Art art; // TODO converter

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerPaintingBreak
            && this.player.equals(((PlayerPaintingBreak)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        // TODO indirect
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{text:One painting} got removed by {user}",
                                    "{1:amount} {text:painting} got removed by {user}",
                                    this.player.name, count);
    }
}
