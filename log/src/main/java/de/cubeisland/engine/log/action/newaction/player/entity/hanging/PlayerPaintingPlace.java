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
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{text:A painting} got hung up by {user}", // TODO art singular
                                    "{2:amount} {text:paintings} got hung up by {user}",
                                    this.hangingType.name(), this.player.name, count);
    }
}
