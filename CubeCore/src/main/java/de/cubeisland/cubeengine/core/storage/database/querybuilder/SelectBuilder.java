package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface SelectBuilder<This extends SelectBuilder, QueryBuilder> extends ConditionalBuilder<This, QueryBuilder>
{
    public This cols(String... cols);
    public This from(String... tables);
}
