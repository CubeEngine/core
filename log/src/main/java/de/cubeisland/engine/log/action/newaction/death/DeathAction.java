package de.cubeisland.engine.log.action.newaction.death;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

public abstract class DeathAction extends ActionTypeBase<DeathListener>
{
    public Reference<KillAction> killer;
    // TODO reference to KillAction
}
