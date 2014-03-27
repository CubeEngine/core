package de.cubeisland.engine.log.action.newaction.player.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player filling a bowl with mushroom-soup using a mooshroom
 */
public class PlayerSoupFill extends PlayerEntityActionType
{
    // return "soup-fill";
    // return this.lm.getConfig(world).BOWL_FILL_SOUP;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerSoupFill
            && ((PlayerSoupFill)action).playerUUID.equals(this.playerUUID)
            && ((PlayerSoupFill)action).entityType == this.entityType;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} made a soup using mooshrooms",
                                    "{user} made {amount} soups using mooshrooms",
                                    this.playerName, count);
    }
}
