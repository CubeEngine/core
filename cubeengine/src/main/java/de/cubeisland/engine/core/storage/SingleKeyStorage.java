/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.storage;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.SingleKeyEntity;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.engine.core.storage.database.mysql.MySQLQueryBuilder;

import static de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase.prepareColumnName;

/**
 * Storage-Implementation for single Integer-Key-Models
 */
public class SingleKeyStorage<Key_f, M extends Model<Key_f>> extends AbstractStorage<Key_f, M, SingleKeyEntity>
{
    protected String dbKey = null;
    protected boolean keyIsAi = false;
    private boolean storeAsync = false;

    public SingleKeyStorage(Database database, Class<M> model, int revision)
    {
        super(database, model, SingleKeyEntity.class, revision);
        this.tableName = this.storageType.tableName();
        this.dbKey = this.storageType.primaryKey();
        this.keyIsAi = this.storageType.autoIncrement();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        //Fields:
        StringBuilder builder = new StringBuilder(MySQLQueryBuilder.createTable(this.tableName, false, true));
        builder.append("\n( ");
        boolean first = true;
        for (Field field : this.reverseFieldNames.values())
        {
            if (!first)
            {
                builder.append(",\n");
            }
            first = false;
            Attribute attribute = this.attributeAnnotations.get(field);
            String dbName = this.fieldNames.get(field);
            String defaultValue = null;
            if (attribute.defaultIsValue())
            {
                try
                {
                    M model = this.modelClass.newInstance();
                    defaultValue = field.get(model).toString();
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException("Default value is not set OR Default-Constructor is not accessible.");
                }
            }
            if (this.dbKey.equals(dbName))
            {
                builder.append(MySQLQueryBuilder.field(dbName, attribute.type(), attribute
                    .notnull(), this.keyIsAi, true, attribute.comment(), defaultValue, attribute.unsigned(), attribute
                                                           .length(), attribute.decimals()));
            }
            else
            {
                if (attribute.type().equals(AttrType.ENUM))
                {
                    if (!field.getType().isEnum())
                    {
                        throw new IllegalArgumentException("The field " + field.getName() + " is not an enum!");
                    }
                    Field[] enumConst = field.getClass().getEnumConstants();
                    List<String> list = new ArrayList<String>();
                    for (Field f : enumConst)
                    {
                        list.add(field.getName());
                    }
                    builder.append(MySQLQueryBuilder.field(dbName, attribute.type(), attribute
                        .notnull(), false, false, attribute.comment(), defaultValue, false, 0, 0, list
                                                               .toArray(new String[list.size()])));
                }
                else
                {
                    builder.append(MySQLQueryBuilder.field(dbName, attribute.type(), attribute.notnull(), false, false, attribute.comment(), defaultValue,
                                           attribute.unsigned(), attribute.length(), attribute.decimals()));
                }
            }
            if (attribute.defaultIsValue())
            {

            }
        }
        for (Index index : this.storageType.indices())
        {
            for (String indexField : index.fields())
            {
                if (!this.fieldNames.containsValue(indexField))
                {
                    throw new IllegalStateException("Cannot create Index! Field " + indexField + " not found!");
                }
            }
            switch (index.value())
            {
                case FOREIGN_KEY:
                    String[] fields = index.fields();
                    builder.append(",\n FOREIGN KEY (").append(prepareColumnName(fields[0]));
                    for (int i = 1; i < fields.length; ++i)
                    {
                        builder.append(", ").append(prepareColumnName(fields[i]));
                    }
                    builder.append(')');
                    break;
                case UNIQUE:
                    fields = index.fields();
                    builder.append(",\n UNIQUE(").append(prepareColumnName(fields[0]));
                    for (int i = 1; i < fields.length; ++i)
                    {
                        builder.append(", ").append(prepareColumnName(fields[i]));
                    }
                    builder.append(")");
                    break;
                case INDEX:
                    fields = index.fields();
                    builder.append(", INDEX (").append(prepareColumnName(fields[0]));
                    for (int i = 1; i < fields.length; ++i)
                    {
                        builder.append(", ").append(prepareColumnName(fields[i]));
                    }
                    builder.append(')');
            }
        }
        builder.append(",\nPRIMARY KEY(").append(this.dbKey).append("))");
        builder.append(", ENGINE=").append(this.storageType.engine()).append(" DEFAULT CHARSET=").append(this.storageType
                                                                                                             .charset());
        if (this.keyIsAi)
        {
            builder.append("\nAUTO_INCREMENT=").append(1);
        }
        try
        {
            this.database.execute(builder.toString());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while creating Table", ex);
        }
        try
        {
            this.prepareStatements();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for the table "+ this.tableName, ex);
        }
        tableManager.registerTable(this.tableName, this.revision);
    }

    /**
     * Prepares the Default-Statements
     */
    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        String[] fields = new String[this.fieldNames.size() - 1];
        int i = 0;
        for (String fieldName : this.fieldNames.values())
        {
            if (!fieldName.equals(this.dbKey))
            {
                fields[i++] = fieldName;
            }
        }
        String preparedTableName = MySQLDatabase.prepareTableName(this.tableName);
        StringBuilder builder = new StringBuilder();
        if (this.keyIsAi)
        {
            builder.append("INSERT INTO ").append(preparedTableName).append(" ").append(MySQLQueryBuilder
                                                                                            .fieldsInBrackets(fields));
        }
        else
        {
            builder.append("INSERT INTO ").append(preparedTableName).append(" ").append(MySQLQueryBuilder
                                                                                            .fieldsInBrackets(this.allFields));
        }
        this.database.storeStatement(this.modelClass, "store", builder.toString());
        builder = new StringBuilder("INSERT INTO ").append(preparedTableName).append(" ").append(MySQLQueryBuilder.fieldsInBrackets(this.allFields)).append("\nVALUES (?");
        for (i = 1; i < this.allFields.length; ++i)
        {
            builder.append(", ?");
        }
        String col = MySQLDatabase.prepareColumnName(fields[0]);
        builder.append(')').append("\nON DUPLICATE KEY UPDATE ").append(col).append("=VALUES(").append(col).append(")");
        for (int j = 1; j < fields.length; ++j)
        {
            col = MySQLDatabase.prepareColumnName(fields[j]);
            builder.append(", ").append(col).append("=VALUES(").append(col).append(')');
        }
        this.database.storeStatement(this.modelClass, "merge", builder.toString());
        this.database.storeStatement(this.modelClass, "get", new StringBuilder("SELECT ").append(MySQLQueryBuilder.fields(this.allFields))
                             .append("\nFROM ").append(preparedTableName)
                             .append("\nWHERE ").append(MySQLDatabase.prepareColumnName(this.dbKey)).append(" = ?").toString());
        builder = new StringBuilder("UPDATE ").append(preparedTableName).append("\nSET ");
        builder.append(MySQLDatabase.prepareColumnName(fields[0])).append("=? ");
        for (int j = 1; j < fields.length; ++j)
        {
            builder.append(',').append(MySQLDatabase.prepareColumnName(fields[j])).append("=? ");
        }
        builder.append("\nWHERE ").append(MySQLDatabase.prepareColumnName(this.dbKey)).append(" = ?");
        this.database.storeStatement(this.modelClass, "update", builder.toString());

        this.database.storeStatement(this.modelClass, "delete", new StringBuilder("DELETE FROM ").append(preparedTableName)
                                             .append("\nWHERE ").append(MySQLDatabase.prepareColumnName(this.dbKey)).append(" = ? LIMIT 1").toString());
    }

    @Override
    public M get(Key_f key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "get", key);
            if (resultSet.next())
            {
                loadedModel = this.createModel(resultSet);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
        return loadedModel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(final M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.dbKey) || !this.keyIsAi)
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            if (this.keyIsAi && !storeAsync)
            {
                // This is never async
                model.setId((Key_f)this.database.getLastInsertedId(this.modelClass, "store", values.toArray()));
            }
            else
            {
                if (async)
                {
                    this.database.asyncPreparedExecute(this.modelClass, "store", values.toArray());
                }
                else
                {
                    this.database.preparedExecute(this.modelClass, "store", values.toArray());
                }
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while storing Model into Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading Model to store", ex);
        }
    }

    @Override
    public void update(M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.dbKey))
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            values.add(this.reverseFieldNames.get(this.dbKey).get(model));
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "update", values.toArray());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "update", values.toArray());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while updating the Model", ex,this.database.getStoredStatement(modelClass, "update"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while updating the Model", ex);
        }
    }

    @Override
    public void merge(M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.allFields)
            {
                values.add(this.reverseFieldNames.get(name).get(model));
            }
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "merge", values.toArray());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "merge", values.toArray());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while merging the model", ex,this.database.getStoredStatement(modelClass, "merge"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while merging a model", ex);
        }
    }

    @Override
    public void deleteByKey(Key_f key, boolean async)
    {
        try
        {
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "delete", key);
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "delete", key);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting from Database", ex);
        }
    }

    public void doStoreAsync()
    {
        this.storeAsync = true;
    }
}
