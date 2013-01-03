package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface ConditionalBuilder<This extends ConditionalBuilder> extends
    ComponentBuilder<This>
{
    /**
     * Adds ordering by column
     *
     * @param cols
     * @return fluent interface
     */
    public This orderBy(String... cols);

    /**
     * Limits the output to n
     *
     * @param n
     * @return fluent interface
     */
    public This limit(int n);
    
    /**
     * Adds LIMIT ?
     *
     * @return fluent interface
     */
    public This limit();

    /**
     * Sets the offset.
     *
     * @param n
     * @return fluent interface
     */
    public This offset(int n);
    
    /**
     * Adds OFFSET ?
     *
     * @return fluent interface
     */
    public This offset();

    /**
     * Starts a WHERE condition.
     *
     * @return fluent interface
     */
    public This where();

    /**
     * Adds a "BETWEEN ? AND ?" statement.
     *
     * @return fluent interface
     */
    public This between();

    /**
     * Adds ASCending keyword
     *
     * @return fluent interface
     */
    public This asc();

    /**
     * Adds DESCending keyword
     *
     * @return fluent interface
     */
    public This desc();
}
