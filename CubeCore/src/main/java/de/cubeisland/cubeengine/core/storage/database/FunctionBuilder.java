package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface FunctionBuilder<T>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;
    
    
    public FunctionBuilder<T> beginFunction(String function);
    public FunctionBuilder<T> function(String function);
   
    public FunctionBuilder<T> distinct();
    public FunctionBuilder<T> wildcard();
    
    public FunctionBuilder<T> where();
    
    public FunctionBuilder<T> as(String name);
    public FunctionBuilder<T> groupBy(String... cols);//TODO group by multiple cols
    public FunctionBuilder<T> having();
    
    public FunctionBuilder<T> field(String col);
    public FunctionBuilder<T> value();
    public FunctionBuilder<T> value(String name);
    public FunctionBuilder<T> is(int operation);
    
    public FunctionBuilder<T> not();
    public FunctionBuilder<T> and();
    public FunctionBuilder<T> or();
    
    public FunctionBuilder<T> beginSub();
    public FunctionBuilder<T> endSub();
    
    public FunctionBuilder<T> comma();
    
    public FunctionBuilder<T> endFunction();
    
    public T endFunctions();
}
