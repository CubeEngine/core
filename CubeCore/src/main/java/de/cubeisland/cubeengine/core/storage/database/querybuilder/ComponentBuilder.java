package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Anselm Brehme
 */
public interface ComponentBuilder<This extends ComponentBuilder,Parent>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;
    
    public This rawSQL(String sql);
    public This function(String function);
    public This beginFunction(String function);
    public This endFunction();
    
    public This field(String field);
    public This value(Object value);
    public This value();
    public This is(Integer operation);
    
    public This wildcard();
    
    public This not();
    public This and();
    public This or();
    
    public This beginSub();
    public This endSub();
    
    public Parent end();
    
    
}
