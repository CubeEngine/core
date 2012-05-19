package de.cubeisland.cubeengine.war.storage;

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
