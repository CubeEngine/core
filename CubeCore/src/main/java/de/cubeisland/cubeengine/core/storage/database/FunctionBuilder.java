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
    
    public FunctionBuilder<T> where();
    
    public FunctionBuilder<T> now();
    
    public FunctionBuilder<T> avg(String col);
    public FunctionBuilder<T> count(String col, boolean distinct);
    public FunctionBuilder<T> count(String col);//distinct false
    public FunctionBuilder<T> countall();
    public FunctionBuilder<T> min(String col);
    public FunctionBuilder<T> max(String col);
    public FunctionBuilder<T> sum(String col);
    public FunctionBuilder<T> first(String col);
    public FunctionBuilder<T> last(String col);
    
    public FunctionBuilder<T> ucase(String col);
    public FunctionBuilder<T> lcase(String col);
    public FunctionBuilder<T> mid(String col,int start, int length);
    public FunctionBuilder<T> mid(String col,int start);
    public FunctionBuilder<T> len(String col);
    
    public FunctionBuilder<T> round(String col,int decimals);
    
    public FunctionBuilder<T> format(String col, String format);//z.B.: FORMAT(Now(),'YYYY-MM-DD')
    
    public FunctionBuilder<T> as(String name);
    public FunctionBuilder<T> groupBy(String col);//TODO group by multiple cols
    public FunctionBuilder<T> having();//needs ops like wherebuilder
    
    public FunctionBuilder<T> field(String col);
    public FunctionBuilder<T> value();
    public FunctionBuilder<T> is(int operation);
    
    public FunctionBuilder<T> not();
    public FunctionBuilder<T> and();
    public FunctionBuilder<T> or();
    
    public FunctionBuilder<T> beginSub();
    public FunctionBuilder<T> endSub();
    
    public T endFunction();
}
