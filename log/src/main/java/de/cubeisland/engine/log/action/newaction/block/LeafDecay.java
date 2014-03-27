package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents leaves decaying
 */
public class LeafDecay extends BlockActionType<BlockListener>
{
    // return "leaf-decay";
    // return this.lm.getConfig(world).block.LEAF_DECAY_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LeafDecay
            && ((LeafDecay)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{name#block} decayed",
                                    "{1:amount}x {name#block} decayed",
                                    this.oldBlock.name(), count);
    }
}
