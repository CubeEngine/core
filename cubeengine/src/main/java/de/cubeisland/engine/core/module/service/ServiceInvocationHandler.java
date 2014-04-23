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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.PriorityQueue;

import de.cubeisland.engine.core.module.service.Service.Implementation;

class ServiceInvocationHandler implements InvocationHandler
{
    private final Service<?> service;
    private final PriorityQueue<Implementation> implementations;

    public ServiceInvocationHandler(Service<?> service, PriorityQueue<Implementation> implementations)
    {
        this.service = service;
        this.implementations = implementations;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        synchronized (this.implementations)
        {
            final Implementation impl = this.implementations.peek();
            if (impl == null)
            {
                this.service.getModule().getLog().warn("The service <{}> was invoked, but has no implementations!", this.service.getInterface().getName());
                return null;
            }

            try
            {
                return method.invoke(impl.getTarget(), args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getCause();
            }
        }
    }
}
