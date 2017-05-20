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
