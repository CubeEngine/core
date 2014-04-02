package de.cubeisland.engine.log.action.newaction;

import de.cubeisland.engine.log.action.ActionTypeCategory;

public interface NamedAction
{
    // Categories ?
    ActionTypeCategory getCategory();
    String getName();
}
