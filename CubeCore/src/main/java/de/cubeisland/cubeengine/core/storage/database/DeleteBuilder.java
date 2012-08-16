package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface DeleteBuilder extends OrderedBuilder<DeleteBuilder>
{
    public DeleteBuilder from(String table);
}
