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
package de.cubeisland.engine.core.storage.database.mysql;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.storage.database.AttrType.DataTypeInfo;
import static de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase.prepareColumnName;

/**
 * QueryBuilder implementation for MYSQL.
 */
public class MySQLQueryBuilder
{
    public static String createTable(String tableName, boolean temporary, boolean ifNotExists)
    {
        StringBuilder builder = new StringBuilder("CREATE");
        if (temporary)
        {
            builder.append(" TEMPORARY");
        }
        builder.append(" TABLE");
        if (ifNotExists)
        {
            builder.append(" IF NOT EXISTS");
        }
        builder.append(MySQLDatabase.prepareTableName(tableName));
        return builder.toString();
    }

    public static String field(String col_name, AttrType data_type, boolean notNull, boolean autoIncrement, boolean primaryKey, String comment, String defaultValue, boolean data_type_unsigned, int data_type_length, int data_type_decimals, String... date_type_values)
    {
        StringBuilder builder = new StringBuilder(MySQLDatabase.prepareColumnName(col_name)).append(" ");
        builder.append(data_type.name());
        if (data_type.can(DataTypeInfo.LENGTH))
        {
            if (data_type_length != 0)
            {
                if (data_type.can(DataTypeInfo.DECIMALS) && data_type_decimals != 0)
                {
                    builder.append("(").append(data_type_length).append(",").append(data_type_decimals).append(")");
                }
                else
                {
                    builder.append("(").append(data_type_length).append(")");
                }
            }
        }
        if (data_type.can(DataTypeInfo.UNSIGNED) && data_type_unsigned)
        {
            builder.append(" UNSIGNED");
        }
        if (false && data_type.can(DataTypeInfo.ZEROFILL))
        {
            builder.append(" ZEROFILL");
        }
        if (data_type.can(DataTypeInfo.VALUES) && date_type_values.length > 0)
        {
            builder.append("(").append(StringUtils.implode(", ", date_type_values)).append(")");
        }
        if (data_type.can(DataTypeInfo.CHARSET))
        {
            //builder.append(" CHARACTER SET ");
        }
        if (data_type.can(DataTypeInfo.COLLATE)) // default: utf8_unicode_ci
        {
            builder.append(" COLLATE ").append("utf8_unicode_ci");
        }
        if (notNull)
        {
            builder.append(" NOT");
        }
        builder.append(" NULL");
        if (defaultValue != null && !defaultValue.isEmpty())
        {
            builder.append(" DEFAULT ").append(defaultValue);
        }
        if (autoIncrement)
        {
            builder.append(" AUTO_INCREMENT");
        }
        if (primaryKey)
        {
            builder.append(" PRIMARY KEY");
        }
        if (comment != null && !comment.isEmpty())
        {
            builder.append(" COMMENT ").append(MySQLDatabase.prepareString(comment));
        }
        return builder.toString();
    }

    public static String fieldsInBrackets(String... fields)
    {
        return new StringBuilder("(").append(fields(fields)).append(")").toString();
    }

    public static String fields(String... fields)
    {
        StringBuilder builder = new StringBuilder(prepareColumnName(fields[0]));
        for (int i = 1; i < fields.length; i++)
        {
            builder.append(", ").append(prepareColumnName(fields[i]));
        }
        return builder.toString();
    }
}
