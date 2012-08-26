package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface InsertBuilder<This extends InsertBuilder, Parent extends QueryBuilder> extends ComponentBuilder<This, Parent>
{
    public This into(String table);
    public This cols(String... cols);
}
