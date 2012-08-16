package de.cubeisland.cubeengine.core.persistence;

/**
 *
 * @author Anselm Brehme
 */
public interface QuerryBuilder
{
    public QuerryBuilder select(String... col);

    public QuerryBuilder insertinto(String table, String... col);

    public QuerryBuilder delete();

    public QuerryBuilder from(String table);

    public QuerryBuilder where(String... conditions);

    public QuerryBuilder values(int n);

    public QuerryBuilder limit(int n);

    public QuerryBuilder orderBy(String col);

    public QuerryBuilder offset(int n);

    public QuerryBuilder createTableINE(String table);

    public QuerryBuilder attribute(String name, AttrType type);

    public QuerryBuilder attribute(String name, AttrType type, int n);
    
    public QuerryBuilder unsigned();

    public QuerryBuilder next();
        
    public QuerryBuilder notNull();

    public QuerryBuilder nulL();

    public QuerryBuilder autoincrement();

    public QuerryBuilder engine(String engine);

    public QuerryBuilder defaultcharset(String charset);

    public QuerryBuilder autoincrement(int n);

    public QuerryBuilder primaryKey(String key);
    
    public QuerryBuilder foreignKey(String key);

    public QuerryBuilder references(String table, String key);
}
