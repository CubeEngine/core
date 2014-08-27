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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.cubeisland.engine.core.CubeEngine;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AsyncRecord<T extends AsyncRecord<T>> extends UpdatableRecordImpl<T>
{
    private Callable<Integer> update;
    private Callable<Integer> insert;
    private Callable<Integer> delete;

    protected AsyncRecord(Table<T> table)
    {
        super(table);
    }

    private ExecutorService getExecutor()
    {
        return CubeEngine.getCore().getDB().getExecutor();
    }

    public Future<Integer> asyncUpdate()
    {
        if (this.update == null)
        {
            this.update = new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    return update();
                }
            };
        }
        return this.getExecutor().submit(this.update);
    }

    public Future<Integer> asyncInsert()
    {
        if (this.insert == null)
        {
            this.insert = new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    return insert();
                }
            };
        }
        return this.getExecutor().submit(this.insert);
    }

    public Future<Integer> asyncDelete()
    {
        if (this.delete == null)
        {
            this.delete = new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    return delete();
                }
            };
        }
        return this.getExecutor().submit(this.delete);
    }



}
