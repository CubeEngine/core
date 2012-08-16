package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface InsertBuilder
{
    public InsertBuilder into(String... tables);
    public InsertBuilder cols(String... cols);
    public InsertBuilder values(int n);
}
