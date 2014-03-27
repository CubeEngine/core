package de.cubeisland.engine.log.action.newaction.block.ignite;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents lava setting a block on fire
 */
public class LavaIgnite extends BlockActionType<BlockIgniteListener>
{
    // return "lava-ignite";
    // return this.lm.getConfig(world).block.ignite.LAVA_IGNITE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LavaIgnite;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "A fire got set by lava",
                                    "{amount} fires got set by lava",
                                    count);
    }

    public void setSource(Location source)
    {
        // TODO
    }
}
