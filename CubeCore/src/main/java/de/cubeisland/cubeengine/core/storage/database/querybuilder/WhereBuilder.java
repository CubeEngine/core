package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface WhereBuilder<T>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;
    
    public WhereBuilder<T> field(String col);
    public WhereBuilder<T> value();
    public WhereBuilder<T> is(int operation);
    
    public WhereBuilder<T> not();
    public WhereBuilder<T> and();
    public WhereBuilder<T> or();
    
    public WhereBuilder<T> beginSub();
    public WhereBuilder<T> endSub();
    
    public T end();
}
