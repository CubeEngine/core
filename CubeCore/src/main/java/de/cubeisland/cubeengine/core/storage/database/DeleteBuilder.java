package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface DeleteBuilder extends ConditionalBuilder
{
    public DeleteBuilder from(String table);
}
