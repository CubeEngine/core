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
     * @param name the name of the field
     * @param type the type of the field
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param unsigned whether the field is unsigned
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param unsigned whether the field is unsigned
     * @param notnull whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned, boolean notnull);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @param notnull whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length, boolean notnull);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @param notnull whether the field is unsigned
     * @param unsigned whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned, int length, boolean notnull);

    /**
     * Adds a field.
     * 
     * @param name the name
     * @param enumValues the enum values
     * @param notnull whether the field may be null
     * @return fluent interface
     */
    public TableBuilder enumField(String name, String[] enumValues, boolean notnull);

    /**
     * Adds a DEFAULT value
     *
     * @return fluent interface
     */
    public TableBuilder defaultValue(String sql);

    /**
     * Adds AUTO_INCREMENT to the current field
     *
     * @return fluent interface
     */
    public TableBuilder autoIncrement();

    /**
     * Adds an INDEX to the current field
     *
     * @return fluent interface
     */
    public TableBuilder index();

    /**
     * Sets the primary Key.
     *
     * @param fields the fields to use as the primary key
     * @return fluent interface
     */
    public TableBuilder primaryKey(String... fields);

    /**
     * Sets given field to be unique
     *
     * @param field the field to make unique
     * @return fluent interface
     */
    public TableBuilder unique(String field);

    /**
     * Starts a CHECK statement. Don't forget to use beginSub and endSub.
     *
     * @return fluent interface
     */
    public TableBuilder check();

    /**
     * Sets a foreign key.
     *
     * @param key the name
     * @return fluent interface
     */
    public TableBuilder foreignKey(String key);

    /**
     * Sets the reference for the foreign key
     *
     * @param table the table to reference
     * @param field the field of the references table
     * @return fluent interface
     */
    public TableBuilder references(String table, String field);

    /**
     * Sets what should be done when trying to remove a key.
     *
     * @param doThis what to do
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
     * @param engine the name of the engine
     * @return fluent interface
     */
    public TableBuilder engine(String engine);

    /**
     * Sets the default Charset
     *
     * @param charset the charset
     * @return fluent interface
     */
    public TableBuilder defaultcharset(String charset);

    /**
     * Sets the autoincrement.
     *
     * @param n the auto-increment start value
     * @return fluent interface
     */
    public TableBuilder autoIncrement(int n);
}
