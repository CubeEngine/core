package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface MergeBuilder<This extends MergeBuilder, QueryBuilder> extends ComponentBuilder<This, QueryBuilder>
{
    public This into(String table);
    public This cols(String... cols);
    public This updateCols(String... cols);
}
