package de.cubeisland.engine.log.action.newaction.block.player.place;

import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class PlayerLavaBucketPlace extends PlayerBlockPlace
{
    // return "lava-bucket";
    // return this.lm.getConfig(world).block.bucket.LAVA_BUCKET_enable;

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} emptied {amount} lava-buckets", this.playerName, this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} emptied a lava-bucket", this.playerName);
    }
}
