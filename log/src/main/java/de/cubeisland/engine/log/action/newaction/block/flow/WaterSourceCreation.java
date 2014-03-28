package de.cubeisland.engine.log.action.newaction.block.flow;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

/**
 * Represents water creating a new water source block
 */
public class WaterSourceCreation extends BlockActionType<FlowListener>
{
    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        return null;
    }
}
