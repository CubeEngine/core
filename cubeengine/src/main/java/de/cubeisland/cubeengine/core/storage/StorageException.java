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
package de.cubeisland.cubeengine.core.storage;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * This exception is thrown when an error occurs during the storage-process.
 */
public class StorageException extends RuntimeException
{
    private static final long serialVersionUID = -3367478236326791833L;

    public StorageException(String message)
    {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Also prints the SQL-Statement that caused the exception
     *
     * @param message
     * @param cause
     * @param statement
     */
    public StorageException(String message, SQLException cause, Statement statement)
    {
        //this allows seeing the complete sql that did produce the error
        super(message + "\n\n" + statement.toString(), cause);
    }
}
