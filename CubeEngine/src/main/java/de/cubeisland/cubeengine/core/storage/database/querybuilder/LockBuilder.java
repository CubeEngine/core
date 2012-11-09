package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Anselm Brehme
 */
public interface LockBuilder extends ComponentBuilder<LockBuilder>
{
    /**
     * Locks the table for ...
     * 
     * @return fluent interface
     */
    public LockBuilder lock();
    
    /**
     * locks for READ-ACCESS
     * 
     * @return fluent interface
     */
    public LockBuilder read();

    /**
     * locks for READ-ACCESS
     * 
     * @return fluent interface
     */
    public LockBuilder write();

    /**
     * adds the table to lock
     * 
     * @param table
     * @return fluent interface
     */
    public LockBuilder table(String table);

    
}