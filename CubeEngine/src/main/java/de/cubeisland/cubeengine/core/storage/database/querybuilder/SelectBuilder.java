package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface SelectBuilder extends ConditionalBuilder<SelectBuilder>
{
    /**
     * Adds the cols to select.
     *
     * @param cols
     * @return fluent interface
     */
    public SelectBuilder cols(String... cols);

    /**
     * Adds the tables to select from.
     * 
     * @param tables
     * @return fluent interface
     */
    public SelectBuilder from(String... tables);
}