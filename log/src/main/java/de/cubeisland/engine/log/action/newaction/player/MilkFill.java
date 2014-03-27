package de.cubeisland.engine.log.action.newaction.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBucketListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player filling a bucket with milk
 */
public class MilkFill extends PlayerActionType<PlayerBucketListener>
{
    // return "milk-fill";
    // return this.lm.getConfig(world).BUCKET_FILL_milk;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof MilkFill
            && ((MilkFill)action).playerUUID.equals(this.playerUUID);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} milked a cow",
                                    "{user} milked {amount} cows",
                                    this.playerName, count);
    }
}
