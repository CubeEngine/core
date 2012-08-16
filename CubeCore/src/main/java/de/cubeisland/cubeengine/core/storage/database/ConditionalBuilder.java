package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public interface ConditionalBuilder<T>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;
    
    public T beginWhere();
    
    public T col(String col);
    public T value();
    public T op(int operation);
    
    public T not();
    public T and();
    public T or();
    
    public T beginSub();
    public T endSub();
    
    public T endWhere();
}
