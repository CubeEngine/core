package de.cubeisland.engine.log.action.newaction.block.player.destroy;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBucketListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.*;

/**
 * Represents a player filling a bucket
 */
public class PlayerBucketFill extends PlayerBlockActionType<PlayerBucketListener>
{
    // return "bucket-fill";
    // return this.lm.getConfig(world).block.bucket.BUCKET_FILL_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerBucketFill
            && ((PlayerBucketFill)action).playerUUID.equals(this.playerUUID)
            && ((PlayerBucketFill)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.oldBlock == LAVA || this.oldBlock == STATIONARY_LAVA)
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} filled a bucket with lava",
                                        "{user} filled {amount} buckets with lava",
                                       this.playerName, count);
        }
        if (this.oldBlock == WATER || this.oldBlock == STATIONARY_WATER)
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} filled a bucket with water",
                                        "{user} filled {amount} buckets with water",
                                       this.playerName, count);
        }
        return user.getTranslationN(POSITIVE, count,
                                    "{user} filled a bucket with some random fluids",
                                    "{user} filled {amount} buckets with some random fluids!",
                                    this.playerName, count);
    }
}
