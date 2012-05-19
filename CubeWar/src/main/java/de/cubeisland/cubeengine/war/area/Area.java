package de.cubeisland.cubeengine.war.area;

import de.cubeisland.cubeengine.war.storage.AreaModel;
import de.cubeisland.cubeengine.war.groups.Group;

/**
 *
 * @author Faithcaio
 */
public class Area
{
    AreaModel model;
    
    public Area(AreaModel model)
    {
        this.model = model;
    }

    public Group getGroup()
    {
        return this.model.getGroup();
    }
}
