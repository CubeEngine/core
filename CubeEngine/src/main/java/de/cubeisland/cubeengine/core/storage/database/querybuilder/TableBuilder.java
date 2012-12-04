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

    // TODO default
    /**
     * Sets the primary Key.
     *
     * @param key
     * @return fluent interface
     */
    public TableBuilder primaryKey(String... key);

    /**
     * Sets given field to be unique
     *
     * @param field
     * @return fluent interface
     */
    public TableBuilder unique(String field);

    /**
     * Starts a CHECK statement.
     * Dont forget to use beginSub and endSub.
     *
     * @param field
     * @return fluent interface
     */
    public TableBuilder check();

    /**
     * Sets a foreign key.
     *
     * @param key
     * @return fluent interface
     */
    public TableBuilder foreignKey(String key);

    /**
     * Sets the reference for the foreign key
     *
     * @param table
     * @param field
     * @return fluent interface
     */
    public TableBuilder references(String table, String field);

    /**
     * Sets what should be done when trying to remove a key.
     *
     * @param table
     * @param field
     * @return fluent interface
     */
    public TableBuilder onDelete(String doThis);

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
