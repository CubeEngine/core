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
package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.MergeBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for merging into tables.
 */
public class MySQLMergeBuilder extends MySQLComponentBuilder<MergeBuilder>
    implements MergeBuilder
{
    private boolean updateColsSpecified;
    private String[] insertCols;

    protected MySQLMergeBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLMergeBuilder into(String table)
    {
        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareTableName(table)).append(" ");
        this.updateColsSpecified = false;
        this.insertCols = null;
        return this;
    }

    @Override
    public MySQLMergeBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");

        this.query.append('(').append(this.database.prepareFieldName(cols[0]));
        int i;
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        this.query.append(") VALUES (?");
        for (i = 1; i < cols.length; ++i)
        {
            this.query.append(",?");
        }
        this.query.append(')');

        this.insertCols = cols;
        return this;
    }

    @Override
    public MySQLMergeBuilder updateCols(String... updateCols)
    {
        if (this.insertCols == null)
        {
            throw new IllegalStateException("No insert cols specified!");
        }
        Validate.notEmpty(updateCols, "You have to specify at least one col to update!");
        Validate.isTrue(this.insertCols.length >= updateCols.length, "More update cols than insert cols specified!");

        String col = this.database.prepareFieldName(updateCols[0]);
        this.query.append(" ON DUPLICATE KEY UPDATE ").append(col).append("=VALUES(").append(col).append(')');
        for (int i = 1; i < updateCols.length; ++i)
        {
            col = this.database.prepareFieldName(updateCols[i]);
            this.query.append(',').append(col).append("=VALUES(").append(col).append(')');
        }

        this.updateColsSpecified = true;
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        Validate.isTrue(this.updateColsSpecified, "You have to specify which cols to update!");
        this.insertCols = null;
        return super.end();
    }
}
