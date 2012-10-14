package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface ConditionalBuilder<This extends ConditionalBuilder> extends ComponentBuilder<This>
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
     * Sets the offset.
     * 
     * @param n
     * @return fluent interface
     */
    public This offset(int n);

    /**
     * Adds WHERE condition.
     * 
     * @return fluent interface
     */
    public This where();
}