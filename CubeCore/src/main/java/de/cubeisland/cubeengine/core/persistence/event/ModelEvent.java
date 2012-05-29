package de.cubeisland.cubeengine.core.persistence.event;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.event.CubeEvent;
import de.cubeisland.cubeengine.core.persistence.Model;
import org.bukkit.event.HandlerList;

/**
 *
 * @author CubeIsland-Dev
 */
public class ModelEvent extends CubeEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final Model model;
    
    public ModelEvent(CubeCore core, Model model) 
    {
        super(core);
        this.model = model;
    }

    public Model getModel()
    {
        return model;
    }
}
