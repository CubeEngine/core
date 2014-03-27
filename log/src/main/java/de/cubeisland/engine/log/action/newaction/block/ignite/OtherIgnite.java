package de.cubeisland.engine.log.action.newaction.block.ignite;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a fire set by an explosion or any other means
 */
public class OtherIgnite extends BlockActionType<BlockIgniteListener>
{
    // return "other-ignite";
    // return this.lm.getConfig(world).block.ignite.OTHER_IGNITE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof OtherIgnite;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "A fire got set by an explosion or something else",
                                    "{amount} fires got set by explosions or something else",
                                    count);
    }
}
