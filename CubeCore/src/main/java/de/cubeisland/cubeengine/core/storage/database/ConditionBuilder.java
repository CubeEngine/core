package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface ConditionBuilder
{
    public ConditionBuilder beginSub();
    public ConditionBuilder endSub();
    
    public ConditionBuilder and();
    public ConditionBuilder or();
}
