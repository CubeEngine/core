package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface UpdateBuilder extends ConditionalBuilder<UpdateBuilder>
{
    /**
     * Sets the tables to update.
     *
     * @param tables
     * @return fluent interface
     */
    public UpdateBuilder tables(String... tables);

    /**
     * Sets the colsto update
     * 
     * @param cols
     * @return fluent interface 
     */
    public UpdateBuilder cols(String... cols);
}