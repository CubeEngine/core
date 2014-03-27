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
            && ((PlayerPaintingBreak)action).playerUUID.equals(this.playerUUID);
    }

    @Override
    public String translateAction(User user)
    {
        // TODO indirect
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{text:One painting} got removed by {user}",
                                    "{1:amount} {text:painting} got removed by {user}",
                                    this.playerName, amount);
    }
}
