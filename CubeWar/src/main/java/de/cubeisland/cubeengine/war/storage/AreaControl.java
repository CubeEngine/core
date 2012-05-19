package de.cubeisland.cubeengine.war.storage;

/**
 *
 * @author Faithcaio
 */
public class AreaControl
{

    AreaStorage areaDB;
    
    public AreaControl()
    {
        areaDB = AreaStorage.get();
        
    }
}
