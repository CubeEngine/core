package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import org.bukkit.Art;

import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player placing a painting
 */
public class PlayerPaintingPlace extends PlayerHangingPlace
{
    public Art art;

    @Override
    public String translateAction(User user)
    {
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{text:A painting} got hung up by {user}", // TODO art singular
                                    "{2:amount} {text:paintings} got hung up by {user}",
                                    this.hangingType.name(), this.playerName, amount);
    }
}
