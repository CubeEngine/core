package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface InsertBuilder extends ComponentBuilder<InsertBuilder>
{
    /**
     * Adds where to insert.
     * 
     * @param table
     * @return fluent interface
     */
    public InsertBuilder into(String table);

    /**
     * Adds which cols to insert into.
     * 
     * @param cols
     * @return fluent interface
     */
    public InsertBuilder cols(String... cols);
}