package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface MergeBuilder<This extends MergeBuilder, Parent extends QueryBuilder> extends ComponentBuilder<This, Parent>
{
    public This into(String table);
    public This cols(String... cols);
    public This updateCols(String... cols);
}
