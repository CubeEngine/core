package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a block breaking
 */
public class BlockBreak extends BlockActionType<BlockListener>
{
    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        return user.getTranslation(POSITIVE, "{name#block} got destroyed or moved", this.oldBlock.name());
    }
}
