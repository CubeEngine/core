package de.cubeisland.engine.log.action.newaction;

import de.cubeisland.engine.log.action.ActionCategory;

public interface NamedAction
{
    // Categories ?
    ActionCategory getCategory();

    String getName();
}
