package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface UpdateBuilder<This extends UpdateBuilder, QueryBuilder> extends ConditionalBuilder<This, QueryBuilder>
{
    public This tables(String... tables);
    
    public This cols(String... cols);
}
