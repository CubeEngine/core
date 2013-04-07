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

import de.cubeisland.cubeengine.core.Core;
import gnu.trove.map.hash.THashMap;

/**
 * Manages the revision of tables in the database.
 */
public class TableManager extends SingleKeyStorage<Long, Table>
{
    private static final int REVISION = 1;

    private THashMap<String, Table> tables = new THashMap<String, Table>();

    public TableManager(final Core core)
    {
        super(core.getDB(), Table.class, REVISION);
        tableManager = this;
        initialize();
        for (Table t : this.getAll())
        {
            tables.put(t.table, t);
        }
    }

    /**
     * Registers a table
     *
     * @param table    the table
     * @param revision its revision
     */
    public void registerTable(String table, Integer revision)
    {
        if ("tables".equals(table))
        {
            return;
        }
        Table t = this.tables.get(table);
        if (t != null)
        {
            t.revision = revision;
            this.update(t);
            return; // Table got updated!
        }
        this.store(new Table(table, revision));
    }

    /**
     * Gets the Revision of a table in database
     *
     * @param table the table
     * @return The revision of given table OR -1 if table not registered yet
     */
    public int getRevision(String table)
    {
        Table t = this.tables.get(table);
        if (t != null)
        {
            return t.revision;
        }
        return -1;
    }
}
