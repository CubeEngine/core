package de.cubeisland.cubeengine.core.storage.database;

public interface CompareBuilder<T>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;
    
    public T field(String col);
    public T value();
    public T is(int operation);
    
    public T not();
    public T and();
    public T or();
    
    public T beginSub();
    public T endSub();
}
