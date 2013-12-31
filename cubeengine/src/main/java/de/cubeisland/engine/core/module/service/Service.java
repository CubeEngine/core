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
package de.cubeisland.engine.core.module.service;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import de.cubeisland.engine.core.module.Module;

public class Service<T>
{
    private final Module module;
    private final Class<T> interfaceClass;
    private final PriorityQueue<Implementation> implementations;
    private final T proxy;
    private final ServiceInvocationHandler invocationHandler;

    @SuppressWarnings("unchecked")
    public Service(Module module, Class<T> interfaceClass)
    {
        this.interfaceClass = interfaceClass;
        this.module = module;
        this.implementations = new PriorityQueue<>();
        this.invocationHandler = new ServiceInvocationHandler(this, this.implementations);
        this.proxy = (T)Proxy.newProxyInstance(module.getClassLoader(), new Class[] {interfaceClass}, this.invocationHandler);
    }

    public Module getModule()
    {
        return module;
    }

    public Class<T> getInterface()
    {
        return interfaceClass;
    }

    public T getImplementation()
    {
        return this.proxy;
    }

    public List<Implementation> getImplementations()
    {
        return new ArrayList<>(implementations);
    }

    public Service<T> addImplementation(Module module, T implementation, Priority priority)
    {
        synchronized (this.implementations)
        {
            this.implementations.add(new Implementation(module, implementation, priority));
        }
        return this;
    }

    public Service<T> removeImplementation(T implementation)
    {
        synchronized (this.implementations)
        {
            Iterator<Implementation> it = this.implementations.iterator();
            while (it.hasNext())
            {
                if (it.next().getTarget() == implementation)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    public Service<T> removeImplementations(Module module)
    {
        synchronized (this.implementations)
        {
            Iterator<Implementation> it = this.implementations.iterator();
            while (it.hasNext())
            {
                if (it.next().getModule() == module)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    static class Implementation implements Comparable<Implementation>
    {
        private final Module module;
        private final Object target;
        private final Priority priority;

        public Implementation(Module module, Object target, Priority priority)
        {
            this.module = module;
            this.target = target;
            this.priority = priority;
        }

        public Module getModule()
        {
            return module;
        }

        public Object getTarget()
        {
            return target;
        }

        public Priority getPriority()
        {
            return priority;
        }

        @Override
        public int compareTo(Implementation o)
        {
            if (priority.ordinal() == o.priority.ordinal())
            {
                return 0;
            }
            else
            {
                return priority.ordinal() < o.priority.ordinal() ? 1 : -1;
            }
        }
    }

    public static enum Priority
    {
        LOWEST,
        LOWER,
        NORMAL,
        HIGHER,
        HIGHEST
    }
}
