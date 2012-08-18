package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface ConditionalBuilder<T>
{   
    public FunctionBuilder<T> beginFunction();
    
    public T orderBy(String... cols);
    public T limit(int n);
    public T offset(int n);
    
    public QueryBuilder end();
}
