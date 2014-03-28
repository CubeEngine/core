package de.cubeisland.engine.log.action.newaction.block.player.place;

import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class PlayerWaterBucketPlace extends PlayerBlockPlace
{
    // return "water-bucket";
    // return this.lm.getConfig(world).block.bucket.WATER_BUCKET_enable;

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} emptied {amount} water-buckets", this.player.name, this
                .getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} emptied a water-bucket", this.player.name);
    }
}
