package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a block spreading
 */
public class BlockSpread extends BlockActionType<BlockListener>
{
    // return "block-spread";
    // return this.lm.getConfig(world).block.spread.BLOCK_SPREAD_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof BlockForm
            && ((BlockForm)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{name#block} spreaded",
                                    "{1:amount}x {name#block} spreaded",
                                    this.newBlock.name(), amount);
    }
}
