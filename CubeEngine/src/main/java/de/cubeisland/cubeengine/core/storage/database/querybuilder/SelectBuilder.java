package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface SelectBuilder extends ConditionalBuilder<SelectBuilder>
{
    /**
     * Adds the SELECT statement
     * 
     * @return fluent interface
     */
    public SelectBuilder select();

    /**
     * Adds the cols to select.
     *
     * @param cols
     * @return fluent interface
     */
    public SelectBuilder cols(String... cols);

    /**
     * Adds the INTO statement.
     * This will create a new table containing the values selected in the query.
     * 
     * @param table the table-name
     * @return fluent interface
     */
    public SelectBuilder into(String table);

    /**
     * Adds the IN statement to specify in which database a table is.
     * Use after INTO
     * e.g. into("backup_table").in("Backup_database")
     * 
     * @param database
     * @return fluent interface
     */
    public SelectBuilder in(String database);

    /**
     * Adds the tables to select from.
     * 
     * @param tables
     * @return fluent interface
     */
    public SelectBuilder from(String... tables);

    /**
     * Adds the distinct statement
     * 
     * @return fluent interface
     */
    public SelectBuilder distinct();

    /**
     * Creates a UNION of to SELECT statements
     * 
     * @param all if false returns distinct values else all
     * @return fluent interface
     */
    public SelectBuilder union(boolean all);

    // TODO Inner / Left / Right / Full Join

}
