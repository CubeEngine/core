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
package de.cubeisland.engine.core.storage.database;

import java.util.EnumSet;

import static de.cubeisland.engine.core.storage.database.AttrType.DataTypeInfo.*;

/**
 * The possible AttributeTypes for the database.
 */
public enum AttrType
{
    // Numbers
    /**
     * -2147483648 to 2147483647 normal. 0 to 4294967295 UNSIGNED.
     */
    INT(LENGTH, UNSIGNED, ZEROFILL),
    /**
     * -128 to 127 normal. 0 to 255 UNSIGNED.
     */
    TINYINT(LENGTH, UNSIGNED, ZEROFILL),
    /**
     * -32768 to 32767 normal. 0 to 65535 UNSIGNED.
     */
    SMALLINT(LENGTH, UNSIGNED, ZEROFILL),
    /**
     * -8388608 to 8388607 normal. 0 to 16777215 UNSIGNED.
     */
    MEDIUMINT(LENGTH, UNSIGNED, ZEROFILL),
    /**
     * -9223372036854775808 to 9223372036854775807 normal. 0 to 18446744073709551615 UNSIGNED.
     */
    BIGINT(LENGTH, UNSIGNED, ZEROFILL),
    DECIMAL(LENGTH, UNSIGNED, DECIMALS, ZEROFILL),
    FLOAT(LENGTH, UNSIGNED, DECIMALS, ZEROFILL),
    DOUBLE(LENGTH, UNSIGNED, DECIMALS, ZEROFILL),
    REAL(LENGTH, UNSIGNED, DECIMALS, ZEROFILL),
    BIT(LENGTH),
    BOOLEAN,
    // Date/Time:
    DATE,
    TIME,
    DATETIME,
    TIMESTAMP,
    YEAR,
    // Strings
    CHAR(LENGTH, CHARSET, COLLATE),
    /**
     * Up to 255 characters.
     */
    VARCHAR(LENGTH, CHARSET, COLLATE),
    /**
     * Up to 65,535 characters.
     */
    TEXT(DataTypeInfo.BINARY, CHARSET, COLLATE),
    /**
     * Up to 255 characters.
     */
    TINYTEXT(DataTypeInfo.BINARY, CHARSET, COLLATE),
    /**
     * Up to 16,777,215 characters.
     */
    MEDIUMTEXT(DataTypeInfo.BINARY, CHARSET, COLLATE),
    /**
     * Up to 4,294,967,295 characters.
     */
    LONGTEXT(DataTypeInfo.BINARY, CHARSET, COLLATE),
    // Binary
    BINARY(LENGTH),
    VARBINARY(LENGTH),
    TINYBLOB,
    /**
     * Up 16,777,215 bytes of data.
     */
    MEDIUMBLOB,
    /**
     * Up 65,535 bytes of data.
     */
    BLOB,
    /**
     * Up 4,294,967,295 bytes of data.
     */
    LONGBLOB,
    ENUM(VALUES, CHARSET, COLLATE),
    SET(VALUES, CHARSET, COLLATE);

    private EnumSet<DataTypeInfo>  info;

    private AttrType(DataTypeInfo... info)
    {
        if (info.length == 0)
        {
            this.info = EnumSet.noneOf(DataTypeInfo.class);
        }
        else
        {
            this.info = EnumSet.of(info[0], info);
        }
    }

    public boolean can(DataTypeInfo info)
    {
        return this.info.contains(info);
    }

    public enum DataTypeInfo
    {
        LENGTH,
        UNSIGNED,
        DECIMALS,
        CHARSET,
        COLLATE,
        VALUES,
        ZEROFILL,
        BINARY
    }
}
