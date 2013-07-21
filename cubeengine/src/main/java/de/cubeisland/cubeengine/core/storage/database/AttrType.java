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
package de.cubeisland.cubeengine.core.storage.database;

/**
 * The possible AttributeTypes for the database.
 */
public enum AttrType
{
    // Numbers
    /**
     * -2147483648 to 2147483647 normal. 0 to 4294967295 UNSIGNED.
     */
    INT(false, true),
    /**
     * -128 to 127 normal. 0 to 255 UNSIGNED.
     */
    TINYINT(false, true),
    /**
     * -32768 to 32767 normal. 0 to 65535 UNSIGNED.
     */
    SMALLINT(false, true),
    /**
     * -8388608 to 8388607 normal. 0 to 16777215 UNSIGNED.
     */
    MEDIUMINT(false, true),
    /**
     * -9223372036854775808 to 9223372036854775807 normal. 0 to 18446744073709551615 UNSIGNED.
     */
    BIGINT(false, true),
    DECIMAL(false, true),
    FLOAT(false, true),
    DOUBLE(false, true),
    REAL(false, true),
    BIT( false, true),
    BOOLEAN(false, false),
    SERIAL(true, true),
    // Date/Time:
    DATE(false, false),
    TIME(false, false),
    DATETIME(false, false),
    TIMESTAMP(false, false),
    YEAR(false, false),
    // Strings
    CHAR(false, true),
    /**
     * Up to 255 characters.
     */
    VARCHAR(true, false),
    /**
     * Up to 65,535 characters.
     */
    TEXT(false, false),
    /**
     * Up to 255 characters.
     */
    TINYTEXT(true, false),
    /**
     * Up to 16,777,215 characters.
     */
    MEDIUMTEXT(true,false),
    /**
     * Up to 4,294,967,295 characters.
     */
    LONGTEXT(true,false),
    // Binary
    BINARY(false,false),
    VARBINARY(true,false),
    TINYBLOB(false,false),
    /**
     * Up 16,777,215 bytes of data.
     */
    MEDIUMBLOB(false,false),
    /**
     * Up 65,535 bytes of data.
     */
    BLOB(false,false),
    /**
     * Up 4,294,967,295 bytes of data.
     */
    LONGBLOB(false,false),
    ENUM(true,false),
    SET(true,false);
    private final boolean length;
    private final boolean signed;

    private AttrType(boolean length, boolean signed)
    {
        this.length = length;
        this.signed = signed;
    }

    public boolean hasLength()
    {
        return this.length;
    }

    public boolean canBeSigned()
    {
        return this.signed;
    }
}
