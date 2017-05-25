/*
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
package org.cubeengine.libcube.service.database.mysql;

import org.jooq.QueryPart;
import org.jooq.Table;
import org.jooq.VisitContext;
import org.jooq.impl.DefaultVisitListener;

import static org.jooq.impl.DSL.table;

public class TablePrefixer extends DefaultVisitListener {

    private final String prefix;

    public TablePrefixer(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void visitStart(VisitContext context) {
        if (context.renderContext() != null)
        {
            QueryPart part = context.queryPart();
            if (part instanceof Table<?>)
            {
                String prefixedName = this.prefix + ((Table) part).getName();
                context.queryPart(table(prefixedName));
            }
        }
    }
}
