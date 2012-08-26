package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface UpdateBuilder<This extends UpdateBuilder, Parent extends QueryBuilder> extends ConditionalBuilder<This, Parent>
{
    public This tables(String... tables);
    
    public This cols(String... cols);
}
