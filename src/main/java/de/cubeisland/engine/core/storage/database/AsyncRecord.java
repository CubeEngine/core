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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jooq.impl.UpdatableRecordImpl;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public abstract class AsyncRecord<T extends AsyncRecord<T>> extends UpdatableRecordImpl<T>
{
    protected AsyncRecord(Table<T> table)
    {
        super(table);
    }

    public CompletableFuture<Integer> updateAsync()
    {
        return supplyAsync(this::update);
    }

    public CompletableFuture<Integer> updateAsync(Executor executor)
    {
        return supplyAsync(this::update, executor);
    }

    public CompletableFuture<Integer> insertAsync()
    {
        return supplyAsync(this::insert);
    }

    public CompletableFuture<Integer> insertAsync(Executor executor)
    {
        return supplyAsync(this::insert, executor);
    }

    public CompletableFuture<Integer> deleteAsync()
    {
        return supplyAsync(this::delete);
    }

    public CompletableFuture<Integer> deleteAsync(Executor executor)
    {
        return supplyAsync(this::delete, executor);
    }
}
