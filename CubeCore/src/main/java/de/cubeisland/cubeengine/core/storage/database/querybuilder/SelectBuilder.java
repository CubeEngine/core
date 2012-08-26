package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface SelectBuilder<This extends SelectBuilder, Parent extends QueryBuilder> extends ConditionalBuilder<This, Parent>
{
    public This cols(String... cols);
    public This from(String... tables);
}
