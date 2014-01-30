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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.cubeisland.engine.core.CubeEngine;

public class ListenableExecutorService implements ExecutorService
{
    private final ExecutorService executor;
    private final ExecutorService callbackExecutor;

    public ListenableExecutorService()
    {
        this.executor = Executors.newSingleThreadExecutor(CubeEngine.getCore().getTaskManager().getThreadFactory());
        this.callbackExecutor = Executors.newFixedThreadPool(5, CubeEngine.getCore().getTaskManager().getThreadFactory());
    }

    @Override
    public void shutdown()
    {
        this.executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return this.executor.shutdownNow();
    }

    @Override
    public boolean isShutdown()
    {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return this.executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        return this.executor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task)
    {
        return new ListenableFuture<>(this.executor.submit(task), this.callbackExecutor);
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result)
    {
        return new ListenableFuture<>(this.executor.submit(task, result), this.callbackExecutor);
    }

    @Override
    public ListenableFuture<Void> submit(Runnable task)
    {
        return new ListenableFuture<>((Future<Void>)this.executor.submit(task), this.callbackExecutor);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        return this.executor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
    {
        return this.executor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        return this.executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return this.executor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command)
    {
        this.executor.execute(command);
    }
}
