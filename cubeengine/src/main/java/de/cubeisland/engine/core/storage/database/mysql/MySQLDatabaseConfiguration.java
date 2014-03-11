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

import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.core.storage.database.DatabaseConfiguration;

/**
 * MySQLDatabaseConfig containing all needed information to connect to a MySQLDatabase
 */
public class MySQLDatabaseConfiguration extends DatabaseConfiguration
{
    @Comment("The host to connect with. Default: localhost")
    public String host = "localhost";

    @Comment("The port to connect with. Default 3306")
    public short port = 3306;

    @Comment("The user using the database")
    public String user = "minecraft";

    @Comment("The password for the specified user")
    public String password = "";

    @Comment("The name of the database")
    public String database = "minecraft";

    @Comment("The table prefix to use for all CubeEngine tables")
    public String tablePrefix = "cube_";
}
