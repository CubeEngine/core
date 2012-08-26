package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Anselm Brehme
 */
public interface QueryBuilder<This extends QueryBuilder,String> extends ComponentBuilder<This, String>
{
    public InsertBuilder insert();
    
    public MergeBuilder merge();

    public SelectBuilder select(String... tables);
    
    public UpdateBuilder update(String... tables);

    public DeleteBuilder delete();

    public TableBuilder createTable(String name, boolean ifNoExist);
    
    public This clearTable(String table);
    
    public This dropTable(String... tables);
    
    public String end();
}
