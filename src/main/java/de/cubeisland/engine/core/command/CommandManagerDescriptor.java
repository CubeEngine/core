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

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.SimpleCommandDescriptor;
import de.cubeisland.engine.command.parametric.context.BasicCommandContext;
import de.cubeisland.engine.command.parametric.context.CommandContextBuilder;
import de.cubeisland.engine.command.parametric.context.ContextBuilder;
import de.cubeisland.engine.command.parametric.context.SourceContextBuilder;
import de.cubeisland.engine.command.parameter.ParameterUsageGenerator;
import de.cubeisland.engine.core.Core;

public class CommandManagerDescriptor extends SimpleCommandDescriptor implements de.cubeisland.engine.command.ExceptionHandler, ContextBuilder
{
    private ExceptionHandler exceptionHandler;

    private ContextBuilder srcCtxBuilder = new SourceContextBuilder();
    private ContextBuilder cmdCtxBuilder = new CommandContextBuilder();

    public CommandManagerDescriptor(Core core)
    {
        this.exceptionHandler = new ExceptionHandler(core);
        this.setName("Base CommandDispatcher for CubeEngine");
        this.setUsageGenerator(new ParameterUsageGenerator());
    }

    @Override
    public void handleException(Throwable e, CommandBase command, CommandInvocation invocation)
    {
        this.exceptionHandler.handleException(e, command, invocation);
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
