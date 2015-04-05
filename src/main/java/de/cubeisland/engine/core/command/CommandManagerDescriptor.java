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
package de.cubeisland.engine.core.command;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.SimpleCommandDescriptor;
import de.cubeisland.engine.butler.parameter.ParameterUsageGenerator;
import de.cubeisland.engine.butler.parametric.context.BasicCommandContext;
import de.cubeisland.engine.butler.parametric.context.CommandContextBuilder;
import de.cubeisland.engine.butler.parametric.context.ContextBuilder;
import de.cubeisland.engine.butler.parametric.context.SourceContextBuilder;

public class CommandManagerDescriptor extends SimpleCommandDescriptor implements ContextBuilder
{
    private ContextBuilder srcCtxBuilder = new SourceContextBuilder();
    private ContextBuilder cmdCtxBuilder = new CommandContextBuilder();

    public CommandManagerDescriptor()
    {
        this.setName("Base CommandDispatcher for CubeEngine");
        this.setUsageGenerator(new ParameterUsageGenerator());
    }

    @Override
    public Object buildContext(CommandInvocation invocation, Class<?> parameterType)
    {
        if (CommandSource.class.isAssignableFrom(parameterType))
        {
            return srcCtxBuilder.buildContext(invocation, parameterType);
        }
        else if (BasicCommandContext.class.isAssignableFrom(parameterType))
        {
            return cmdCtxBuilder.buildContext(invocation, parameterType);
        }
        throw new IllegalArgumentException("Unknown Context Type " + parameterType.getName());
    }
}
