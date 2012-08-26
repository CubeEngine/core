package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface InsertBuilder<This extends InsertBuilder, QueryBuilder> extends ComponentBuilder<This, QueryBuilder>
{
    public This into(String table);
    public This cols(String... cols);
}
