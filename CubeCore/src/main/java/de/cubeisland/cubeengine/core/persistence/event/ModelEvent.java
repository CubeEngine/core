package de.cubeisland.cubeengine.core.persistence.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEvent;

/**
 *
 * @author Anselm Brehme
 */
public abstract class ModelEvent extends CubeEvent
{
    private final Object model;

    public ModelEvent(Core core, Object model)
    {
        super(core);
        this.model = model;
    }

    public Object getModel()
    {
        return model;
    }
}