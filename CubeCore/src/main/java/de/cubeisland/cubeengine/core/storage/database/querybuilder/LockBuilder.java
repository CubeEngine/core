package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Anselm Brehme
 */
public interface LockBuilder extends ComponentBuilder<LockBuilder>
{
    public LockBuilder read();
    public LockBuilder write();
    public LockBuilder table(String table);
    public LockBuilder lock();
}