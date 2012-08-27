package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface InsertBuilder extends ComponentBuilder<InsertBuilder>
{
    public InsertBuilder into(String table);
    public InsertBuilder cols(String... cols);
}