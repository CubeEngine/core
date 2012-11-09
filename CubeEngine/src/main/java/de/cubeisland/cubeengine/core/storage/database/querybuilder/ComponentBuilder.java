package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface ComponentBuilder<This extends ComponentBuilder>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;

    /**
     * Inserts RawSQL-Code.
     * 
     * @param sql
     * @return fluent interface
     */
    public This rawSQL(String sql);

    /**
     * Adds a function.
     * 
     * @param function
     * @return fluent interface
     */
    public This function(String function);

    /**
     * Begins a function.
     * 
     * @param function
     * @return fluent interface
     */
    public This beginFunction(String function);

    /**
     * Ends the current function.
     * 
     * @return fluent interface
     */
    public This endFunction();

    /**
     * Adds a quoted field.
     * 
     * @param field
     * @return fluent interface
     */
    public This field(String field);

    /**
     * Adds a value.
     * 
     * @param value
     * @return fluent interface
     */
    public This value(Object value);

    /**
     * Adds a variable which can be later replaced by a value.
     * 
     * @return fluent interface
     */
    public This value();

    /**
     * Adds an operation
     * 
     * @param operation
     * @return fluent interface
     */
    public This is(Integer operation);

    /**
     * Adds a wildcard.
     * 
     * @return fluent interface
     */
    public This wildcard();

    /**
     * Adds NOT.
     * 
     * @return fluent interface
     */
    public This not();

    /**
     * Adds AND.
     * 
     * @return fluent interface
     */
    public This and();
    
    /**
     * Adds OR.
     * 
     * @return fluent interface
     */
    public This or();

    /**
     * Adds AS ...
     * 
     * @param field
     * @return fluent interface
     */
    public This as(String field);

    /**
     * Adds grouping by given fields
     * 
     * @param field
     * @return fluent interface
     */
    public This groupBy(String... field);

    /**
     * Adds HAVING.
     * 
     * @return fluent interface
     */
    public This having();

    /**
     * Adds (
     * 
     * @return fluent interface
     */
    public This beginSub();

    /**
     * Adds )
     * 
     * @return fluent interface
     */
    public This endSub();

    /**
     * Ends the querypart and returns the QueryBuilder
     * 
     * @return the QueryBuilder
     */
    public QueryBuilder end();
}