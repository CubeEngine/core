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
package org.cubeengine.libcube.service.command;

import com.google.inject.Injector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import org.cubeengine.libcube.service.command.annotation.ExceptionHandler;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CubeEngineCommand implements CommandExecutor
{

    private final Object holder;
    private final Method method;
    private final List<AnnotationCommandBuilder.ContextExtractor<?>> extractors;
    private final List<CommandExceptionHandler> exceptionHandlers;

    public CubeEngineCommand(Object holder, Method method, List<AnnotationCommandBuilder.ContextExtractor<?>> extractors, Injector injector)
    {
        this.holder = holder;
        this.method = method;
        this.extractors = extractors;

        final ExceptionHandler holderExceptions = holder.getClass().getAnnotation(ExceptionHandler.class);
        final ExceptionHandler methodExceptions = method.getAnnotation(ExceptionHandler.class);
        this.exceptionHandlers = new ArrayList<>();
        this.gatherExceptionHandlers(injector, holderExceptions);
        this.gatherExceptionHandlers(injector, methodExceptions);
        this.exceptionHandlers.sort(Comparator.comparing(CommandExceptionHandler::priority));
    }

    public void gatherExceptionHandlers(Injector injector, ExceptionHandler annotation)
    {
        if (annotation != null)
        {
            for (Class<? extends CommandExceptionHandler> clazz : annotation.value())
            {
                this.exceptionHandlers.add(injector.getInstance(clazz));
            }
        }
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {

        final List<Object> args = new ArrayList<>();
        for (AnnotationCommandBuilder.ContextExtractor<?> extractor : this.extractors)
        {
            args.add(extractor.apply(context));
        }
        try
        {
            method.invoke(holder, args.toArray());
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {

            for (CommandExceptionHandler handler : this.exceptionHandlers)
            {
                final Builder text = Component.text();
                if (handler.handleException(e, context, text))
                {
                    return CommandResult.error(text.build());
                }
            }

            e.printStackTrace();
            if (e.getCause().getMessage() == null)
            {
                return CommandResult.error(Component.text(e.getCause().getClass().getSimpleName()));
            }
            return CommandResult.error(Component.text(e.getCause().getMessage()));
        }

        return CommandResult.success();
    }
}
