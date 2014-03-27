package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a block fading away
 */
public class BlockFade extends BlockActionType<BlockListener>
{
    // return "block-fade";
    // return this.lm.getConfig(world).block.fade.enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof BlockFade
            && ((BlockFade)action).oldBlock == this.oldBlock;
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
                                    "{name#block} faded away",
                                    "{1:amount}x {name#block} faded away",
                                    this.oldBlock.name(), amount);
    }
}
