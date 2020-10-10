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

import static org.spongepowered.api.command.Command.builder;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.logging.log4j.util.Strings;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.spongepowered.api.command.Command.Builder;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class AnnotationCommandBuilder
{

    private I18n i18n;

    @Inject
    public AnnotationCommandBuilder(I18n i18n)
    {
        this.i18n = i18n;
    }

    public void registerModuleCommands(RegisterCommandEvent<Parameterized> event, PluginContainer plugin, Object module, List<Field> commands)
    {
        for (Field command : commands)
        {
            try
            {
                this.registerCommands(event, plugin, command.get(module));
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public void registerCommands(RegisterCommandEvent<Parameterized> event, PluginContainer plugin, Object holder)
    {
        final Command holderAnnotation = holder.getClass().getAnnotation(Command.class);
        if (holderAnnotation != null)
        {
            final Builder builder = builder();
            final String name = this.getCommandName(null, holder, holderAnnotation);

            builder.setShortDescription(TextComponent.of(holderAnnotation.desc()));
            final HelpExecutor helpExecutor = new HelpExecutor(i18n);
            builder.setExecutor(helpExecutor);
            builder.child(builder().setExecutor(helpExecutor).build(), "?");
            //        builder.setExecutionRequirements()?
            //        builder.setExtendedDescription()!
            this.createChildCommands(holder, builder, plugin.getMetadata().getId(), "command", name);
            final Parameterized build = builder.build();
            helpExecutor.init(build, null, String.join(".", Arrays.asList(plugin.getMetadata().getId(), "command", name)));
            event.register(plugin, build, name, holderAnnotation.alias());
        }
        else
        {
            if (holder instanceof DispatcherCommand)
            {
                throw new IllegalStateException("Base command needs a Command annotation! " + holder.getClass().getSimpleName());
            }
            final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
            for (Method method : methods)
            {
                final Command methodAnnotation = method.getAnnotation(Command.class);
                String name = this.getCommandName(method, holder, methodAnnotation);
                final Parameterized build = this.buildCommand(holder, method, methodAnnotation, plugin.getMetadata().getId(), "command", name);
                event.register(plugin, build, name, methodAnnotation.alias());
            }
        }

    }

    private void createChildCommands(Object holder, Builder dispatcher, String... permNodes)
    {
        final String basePermNode = String.join(".", permNodes);
        dispatcher.setPermission(basePermNode + ".use");

        final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
        for (Method method : methods)
        {
            this.createChildCommand(holder, dispatcher, method, permNodes);
        }

        if (holder instanceof DispatcherCommand)
        {
            for (Object subHolder : ((DispatcherCommand) holder).getSubCommands())
            {
                final Command subHolderAnnotation = subHolder.getClass().getAnnotation(Command.class);
                if (subHolderAnnotation != null) {
                    final Builder builder = builder();
                    final String name = this.getCommandName(null, subHolder, subHolderAnnotation);
                    builder.setShortDescription(TextComponent.of(subHolderAnnotation.desc()));
                    //        builder.setExecutionRequirements()?
                    //        builder.setExtendedDescription()!
                    final String[] newPermNodes = Arrays.copyOf(permNodes, permNodes.length + 1);
                    newPermNodes[permNodes.length] = name;
                    this.createChildCommands(subHolder, builder, newPermNodes);
                    final HelpExecutor helpExecutor = new HelpExecutor(i18n);
                    builder.setExecutor(helpExecutor);
                    final List<String> alias = new ArrayList<>();
                    alias.add(name);
                    alias.addAll(Arrays.asList(subHolderAnnotation.alias()));

                    final Parameterized helpChild = builder().setExecutor(helpExecutor).build();
                    builder.child(helpChild, "?");

                    final Parameterized build = builder.build();
                    helpExecutor.init(build, null, String.join(".", permNodes));
                    dispatcher.child(build, alias);
                } else {
                    this.createChildCommands(subHolder, dispatcher, permNodes);
                }
            }
        }
    }

    private void createChildCommand(Object holder, Builder dispatcher, Method method, String... permNodes)
    {
        final Command methodAnnotation = method.getAnnotation(Command.class);
        String name = this.getCommandName(method, holder, methodAnnotation);
        final String[] newPermNodes = Arrays.copyOf(permNodes, permNodes.length + 1);
        newPermNodes[permNodes.length] = name;
        final Parameterized build = buildCommand(holder, method, methodAnnotation, newPermNodes);
        final List<String> alias = new ArrayList<>();
        alias.add(name);
        alias.addAll(Arrays.asList(methodAnnotation.alias()));
        dispatcher.child(build, alias);
    }

    private String getCommandName(Method method, Object holder, Command cmd)
    {
        String name = cmd.name();
        if (!name.isEmpty()) {
            return name;
        }
        if (method == null) {
            throw new IllegalStateException("Command needs a name: " + holder.getClass().getSimpleName());
        }
        return method.getName();
    }

    public Set<Method> getMethods(Class<?> holder)
    {
        HashSet<Method> methods = new LinkedHashSet<>(Arrays.asList(holder.getMethods()));
        for (Method method : methods)
        {
            method.setAccessible(true);
        }
        methods.addAll(Arrays.asList(holder.getDeclaredMethods()));
        return methods;
    }

    private Parameterized buildCommand(Object holder, Method method, Command annotation, String... permNodes)
    {
        final Builder builder = builder();

        final Annotation[][] annotationsList = method.getParameterAnnotations();
        final Type[] types = method.getParameterTypes();
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final List<ContextExtractor<?>> extractors = new ArrayList<>();
        for (int i = 0; i < types.length; i++)
        {
            final Type type = types[i];
            final Annotation[] annotations = annotationsList[i];
            final java.lang.reflect.Parameter parameter = parameters[i];
            extractors.add(this.buildParameter(i, builder, parameter, type, annotations, types.length - 1));
        }
        builder.setPermission(String.join("." , permNodes) + ".use");
        builder.setShortDescription(Component.text(annotation.desc()));
//        builder.setExecutionRequirements()
//        builder.setExtendedDescription()
//        builder.flag()
        final CubeEngineCommand executor = new CubeEngineCommand(holder, method, extractors);
        builder.setExecutor(executor);
        final HelpExecutor helpExecutor = new HelpExecutor(i18n);
        builder.child(builder().setExecutor(helpExecutor).build(), "?");
        final Parameterized build = builder.build();
        helpExecutor.init(build, executor, String.join("." , permNodes));
        return build;
    }

    private ContextExtractor<?> buildParameter(int index, Builder builder, java.lang.reflect.Parameter parameter, Type type, Annotation[] annotations, int last)
    {
        final String name = parameter.getName();
        // TODO search param annotation for name

        return buildParameter(index, builder, type, annotations, last, name, false);
    }

    private ContextExtractor<?> buildParameter(int index, Builder builder, Type type, Annotation[] annotations,
            int last, String name, boolean forceOptional) {

        if (type == CommandCause.class) {
            return COMMAND_CAUSE;
        }
        if (type == CommandContext.class) {
            return COMMAND_CAUSE;
        }
        Class<?> rawType =  (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
        final Parameter.Value.Builder<?> parameterBuilder;
        if (rawType == String.class) {
            if (index == last) {
                parameterBuilder = Parameter.remainingJoinedStrings();
            } else {
                parameterBuilder = Parameter.string();
            }
        } else if (rawType == Optional.class) {
            return this.buildParameter(index, builder, ((ParameterizedType) type).getActualTypeArguments()[0], annotations, last, name, true);
        } else if (rawType == List.class) {
            throw new IllegalStateException("Not implemented yet");
        } else if (rawType == Set.class) {
            throw new IllegalStateException("Not implemented yet");
        } else {
            throw new IllegalArgumentException("Could not build Parameter for type: " + TypeToken.of(type));
        }

        parameterBuilder.setKey(name);
        final Parameter.Value<?> param = parameterBuilder.build();
        builder.parameter(param);

        boolean optional = isOptional(annotations);

        if (forceOptional)
        {
            return c -> c.getOne(param);
        }
        else if (optional) {
            return c -> c.getOne(param).orElse(null);
        }
        else {
            return c -> c.requireOne(param);
        }
    }

    private boolean isOptional(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            // TODO optional annotation
        }
        return false;
    }

    public void injectCommands(Injector injector, Object module, List<Field> fields)
    {
        for (Field field : fields)
        {
            Object command = injector.getInstance(field.getType());
            try
            {
                field.setAccessible(true);
                field.set(module, command);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public static class HelpExecutor implements CommandExecutor {

        private I18n i18n;
        private Parameterized target;
        private CubeEngineCommand executor;
        private String perm;

        public HelpExecutor(I18n i18n) {
            this.i18n = i18n;
        }

        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            final Optional<String> actual = context.getCause().getContext().get(EventContextKeys.COMMAND);

            final Audience audience = context.getCause().getAudience();
            final Style grayStyle = Style.style(NamedTextColor.GRAY);

            Component descLabel = i18n.translate(audience, grayStyle, "Description:");
            final Component permText = i18n.translate(audience, grayStyle, "Permission: (click to copy) {input}", perm)
                    .append(Component.text(".use").color(NamedTextColor.WHITE));
            descLabel = descLabel.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, permText))
                    .clickEvent(ClickEvent.copyToClipboard(perm + ".use"));
            final Component descValue = target.getShortDescription(context.getCause()).get().color(NamedTextColor.GOLD);
            context.sendMessage(Component.empty().append(descLabel).append(Component.text(" ")).append(descValue));

            List<String> usages = new ArrayList<>();
            for (Parameter param : target.parameters()) {
                if (param instanceof Parameter.Value) {
                    String usage = ((Parameter.Value<?>) param).getUsage(context.getCause());
                    if (!param.isOptional()) {
                        usage = "<" + usage + ">";
                    }
                    usages.add(usage);
                } else {
                    usages.add("param?");
                }
            }

            final String cmdPrefix = context.getCause().getAudience() instanceof ServerPlayer ? "/" : "";
            final String usage = usages.isEmpty() && executor == null ? "<command>" : Strings.join(usages, ' ');
            final String joinedUsage = cmdPrefix + actual.orElse("missing command context") + " " + usage;

            i18n.send(audience, grayStyle, "Usage: {input}", joinedUsage);


//            context.sendMessage(target.getUsage(context.getCause()).style(grayStyle));
//            context.sendMessage(Component.text(actual.orElse("no cmd?")));

            final List<Parameter.Subcommand> subcommands = target.subcommands().stream().filter(sc -> !sc.getAliases().iterator().next().equals("?")).collect(Collectors.toList());
            if (!subcommands.isEmpty()) {
                context.sendMessage(Component.empty());
                i18n.send(audience, MessageType.NEUTRAL, "The following sub-commands are available:");
                context.sendMessage(Component.empty());
                for (Parameter.Subcommand subcommand : subcommands) {
                    final String firstAlias = subcommand.getAliases().iterator().next();
                    final Parameterized subCmd = subcommand.getCommand();
                    TextComponent textPart1 = Component.text(firstAlias, NamedTextColor.YELLOW);
                    final Component subPermText = i18n.translate(audience, grayStyle, "Permission: (click to copy) {input}", perm + "." + firstAlias)
                            .append(Component.text(".use").color(NamedTextColor.WHITE));
                    textPart1 = textPart1.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, subPermText))
                            .clickEvent(ClickEvent.copyToClipboard(perm + "." + firstAlias + ".use"));
                    final TextComponent text = Component.empty().append(textPart1)
                            .append(Component.text(": "))
                            .append(subCmd.getShortDescription(context.getCause()).get().style(grayStyle)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("click to show usage")))
                                    .clickEvent(ClickEvent.runCommand(cmdPrefix + actual.orElse("null") + " " + firstAlias + " ?")) // TODO missing command context
                            );

                    context.sendMessage(text);
                }
            } else {
                if (this.executor == null) {
                    i18n.send(audience, MessageType.NEGATIVE, "No actions are available");
                }
            }
            context.sendMessage(Component.empty());
            return CommandResult.empty();
        }

        public void init(Parameterized target, CubeEngineCommand executor, String perm) {
            this.target = target;
            this.executor = executor;
            this.perm = perm;
        }
    }

    public static class CubeEngineCommand implements CommandExecutor {

        private final Object holder;
        private final Method method;
        private final List<ContextExtractor<?>> extractors;

        public CubeEngineCommand(Object holder, Method method, List<ContextExtractor<?>> extractors)
        {
            this.holder = holder;
            this.method = method;
            this.extractors = extractors;
        }

        @Override
        public CommandResult execute(CommandContext context) throws CommandException {

            final List<Object> args = new ArrayList<>();
            for (ContextExtractor<?> extractor : this.extractors)
            {
                args.add(extractor.apply(context));
            }
            try
            {
                method.invoke(holder, args.toArray());
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                // TODO
                return CommandResult.error(TextComponent.of(e.getCause().getMessage()));
            }

            return CommandResult.success();
        }
    }

    public interface ContextExtractor<T> extends Function<CommandContext, T> {
    }

    private static final ContextExtractor<CommandCause> COMMAND_CAUSE = CommandContext::getCause;
    private static final ContextExtractor<CommandContext> COMMAND_CONTEXT = c -> c;
}
