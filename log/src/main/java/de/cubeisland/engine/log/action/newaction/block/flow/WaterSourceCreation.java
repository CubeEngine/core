package de.cubeisland.engine.log.action.newaction.block.flow;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents water creating a new water source block
 */
public class WaterSourceCreation extends BlockActionType<FlowListener>
{
    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof WaterSourceCreation;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "A water source formed",
                                    "{amount} water sources formed", count);
    }
}
