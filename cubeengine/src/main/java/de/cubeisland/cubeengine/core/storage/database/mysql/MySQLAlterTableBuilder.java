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

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.AlterTableBuilder;

/**
 * MYSQLQueryBuilder for altering tables.
 */
public class MySQLAlterTableBuilder extends
    MySQLComponentBuilder<AlterTableBuilder> implements AlterTableBuilder
{
    public MySQLAlterTableBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public AlterTableBuilder alterTable(String table)
    {
        this.query = new StringBuilder("ALTER TABLE ").append(this.database.prepareTableName(table)).append(' ');
        return this;
    }

    @Override
    public AlterTableBuilder add(String field, AttrType type)
    {
        this.query.append("ADD ").append(this.database.prepareFieldName(field)).append(" ").append(parent.getAttrTypeString(type));
        return this;
    }

    @Override
    public AlterTableBuilder drop(String field)
    {
        this.query.append("DROP COLUMN ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder change(String field, String newName, AttrType type)
    {
        this.query.append("CHANGE COLUMN ").
            append(this.database.prepareFieldName(field)).append(" ").
            append(this.database.prepareFieldName(newName));

        if (type != null)
        {
            this.query.append(" ").append(parent.getAttrTypeString(type));
        }
        return this;
    }

    @Override
    public AlterTableBuilder modify(String field, AttrType type)
    {
        this.query.append("MODIFY COLUMN ").append(this.database.prepareFieldName(field)).append(" ").append(parent.getAttrTypeString(type));
        return this;
    }

    @Override
    public AlterTableBuilder addUniques(String... fields)
    {
        this.query.append("ADD UNIQUE (").append(this.database.prepareFieldName(fields[0]));
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
        }
        this.query.append(")");
        return this;
    }

    @Override
    public AlterTableBuilder defaultValue(String value)
    {
        this.defaultValue();
        this.query.append(value);
        return this;
    }

    @Override
    public AlterTableBuilder defaultValue()
    {
        this.query.append(" DEFAULT ");
        return this;
    }

    @Override
    public AlterTableBuilder addCheck()
    {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Override
    public AlterTableBuilder setDefault(String field)
    {
        this.query.append(" MODIFY ").append(this.database.prepareFieldName(field)).append(" DEFAULT ? ");
        return this;
    }

    @Override
    public AlterTableBuilder addForeignKey(String field, String foreignTable, String foreignField)
    {
        this.query.append(" ADD FOREIGN KEY (").append(this.database.prepareFieldName(field))
            .append(") REFERENCES ").append(this.database.prepareTableName(foreignTable)).append(".")
            .append(this.database.prepareFieldName(foreignField));
        return this;
    }

    @Override
    public AlterTableBuilder setPrimary(String field)
    {
        this.query.append(" ADD PRIMARY (").append(this.database.prepareFieldName(field)).append(")");
        return this;
    }

    @Override
    public AlterTableBuilder dropUnique(String field)
    {
        this.query.append("DROP INDEX ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropPrimary()
    {
        this.query.append("DROP PRIMARY KEY");
        return this;
    }

    @Override
    public AlterTableBuilder dropCheck(String field)
    {
        this.query.append("DROP CHECK ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropDefault(String field)
    {
        this.query.append("MODIFY ").append(this.database.prepareFieldName(field)).append(" DROP DEFAULT");
        return this;
    }

    @Override
    public AlterTableBuilder dropIndex(String field)
    {
        this.query.append("DROP INDEX ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropForeignKey(String field)
    {
        this.query.append("DROP FOREIGN KEY ").append(this.database.prepareFieldName(field));
        return this;
    }
}
