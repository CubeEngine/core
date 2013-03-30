package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface InsertBuilder extends ComponentBuilder<InsertBuilder>
{
    /**
     * Adds where to insert.
     *
     * @param table the table to insert into
     * @return fluent interface
     */
    public InsertBuilder into(String table);

    /**
     * Adds which cols to insert into and values to be inserted later.
     *
     * @param cols the columns
     * @return fluent interface
     */
    public InsertBuilder cols(String... cols);

    /**
     * Signals to insert into all col.
     * Don't forget to call values(...) with the correct amount of cols
     *
     * @return fluent interface
     */
    public InsertBuilder allCols();

    /**
     * Adds the VALUES statement with the amount of values
     *
     * @param amount the amount of values
     * @return fluent interface
     */
    public InsertBuilder values(int amount);
}
