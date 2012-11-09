package de.cubeisland.cubeengine.core.storage.database.querybuilder;

import de.cubeisland.cubeengine.core.storage.database.AttrType;

public interface TableBuilder extends ComponentBuilder<TableBuilder>
{
    /**
     * Starts accepting fields.
     * 
     * @return fluent interface
     */
    public TableBuilder beginFields();

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type);

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @param length
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length);

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @param length
     * @param notnull
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length, boolean notnull);

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @param notnull
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean notnull);

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @param length
     * @param notnull
     * @param unsigned
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned);

    /**
     * Adds a field.
     * 
     * @param name
     * @param type
     * @param length
     * @param notnull
     * @param unsigned
     * @param ai
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length, boolean notnull, boolean unsigned, boolean ai);

    /**
     * Sets the primary Key.
     * 
     * @param key
     * @return fluent interface 
     */
    public TableBuilder primaryKey(String key);

    /**
     * Sets a foreign key.
     * 
     * @param key
     * @return fluent interface 
     */
    public TableBuilder foreignKey(String key);
    
    public TableBuilder unique(String field);

    /**
     * Sets the reference for the foreign key
     * 
     * @param table
     * @param key
     * @return fluent interface 
     */
    public TableBuilder references(String table, String key);

    /**
     * Finish accepting the fields
     * 
     * @return fluent interface 
     */
    public TableBuilder endFields();

    /**
     * Sets the engine
     * 
     * @param engine
     * @return fluent interface 
     */
    public TableBuilder engine(String engine);

    /**
     * Sets the default Charset
     * 
     * @param charset
     * @return fluent interface 
     */
    public TableBuilder defaultcharset(String charset);

    /**
     * Sets the autoincrement.
     * 
     * @param n
     * @return fluent interface 
     */
    public TableBuilder autoIncrement(int n);
}