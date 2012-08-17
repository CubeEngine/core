package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Anselm Brehme
 */
public interface FunctionBuilder<T>
{
    public FunctionBuilder<T> currentTime();
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
    
    public T end();
    //TODO DISTINCT in conditional
  
}
