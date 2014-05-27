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
package de.cubeisland.engine.core.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

public class ListenableFuture<V> implements Future<V>
{
    private final Future<V> future;
    private ExecutorService executor;

    public ListenableFuture(Future<V> future, ExecutorService executor)
    {
        expectNotNull(future);
        expectNotNull(executor);
        this.future = future;
        this.executor = executor;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return this.future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled()
    {
        return this.future.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return this.future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException
    {
        return this.future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return this.future.get(timeout, unit);
    }

    /**
     * When the computation represented by this Future is done or throws an exception,
     * the corresponding method in the specified FutureCallback will be called
     *
     * @param callback
     */
    public ListenableFuture<V> addCallback(final FutureCallback<V> callback)
    {
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    callback.onSuccess(future.get());
                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
            }
        });
        return this;
    }
}
