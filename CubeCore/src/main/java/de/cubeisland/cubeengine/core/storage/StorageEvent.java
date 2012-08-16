package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEvent;

/**
 *
 * @author Anselm Brehme
 */
public abstract class StorageEvent extends CubeEvent
{
    private final Object model;

    public StorageEvent(Core core, Object model)
    {
        super(core);
        this.model = model;
    }

    public Object getModel()
    {
        return model;
    }
}