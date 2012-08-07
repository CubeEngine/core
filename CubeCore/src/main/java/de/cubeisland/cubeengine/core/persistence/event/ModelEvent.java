package de.cubeisland.cubeengine.core.persistence.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEvent;
import de.cubeisland.cubeengine.core.persistence.Model;

/**
 *
 * @author Anselm Brehme
 */
public abstract class ModelEvent extends CubeEvent
{
    private final Model<?> model;

    public ModelEvent(Core core, Model<?> model)
    {
        super(core);
        this.model = model;
    }

    public Model<?> getModel()
    {
        return model;
    }
}