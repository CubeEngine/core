package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a block burning away
 */
public class BlockBurn extends BlockActionType<BlockListener>
{
    // return "block-burn";
    // return this.lm.getConfig(world).block.BLOCK_BURN_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof BlockBurn
            && ((BlockBurn)action).oldBlock == this.oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "A {name#block} went up into flames",
                                    "{1:amount}x {name#block} went up into flames",
                                    this.oldBlock.name(), count);
    }
}
