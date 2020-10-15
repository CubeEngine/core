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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.logging.log4j.util.Strings;
import org.cubeengine.libcube.service.command.annotation.Command;
import org.cubeengine.libcube.service.command.annotation.Default;
import org.cubeengine.libcube.service.command.annotation.Flag;
import org.cubeengine.libcube.service.command.annotation.Named;
import org.cubeengine.libcube.service.command.annotation.Option;
import org.cubeengine.libcube.service.command.annotation.Parser;
import org.cubeengine.libcube.service.command.annotation.ParserFor;
import org.cubeengine.libcube.service.command.annotation.Using;
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
import org.spongepowered.api.command.parameter.Parameter.Value;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import static org.spongepowered.api.command.Command.builder;

@Singleton
public class AnnotationCommandBuilder
{

    private I18n i18n;

    @Inject
    public AnnotationCommandBuilder(I18n i18n)
    {
        this.i18n = i18n;
    }

    public void registerModuleCommands(Injector injector, RegisterCommandEvent<Parameterized> event,
                                       PluginContainer plugin, Object module, List<Field> commands)
    {
        for (Field command : commands)
        {
            try
            {
                command.setAccessible(true);
                this.registerCommands(injector, event, plugin, command.get(module));
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public void registerCommands(Injector injector, RegisterCommandEvent<Parameterized> event, PluginContainer plugin,
                                 Object holder)
    {
        final Command holderAnnotation = holder.getClass().getAnnotation(Command.class);
        if (holderAnnotation != null)
        {
            this.createParsers(injector, holder);

            final Builder builder = builder();
            final String name = this.getCommandName(null, holder, holderAnnotation);

            builder.setShortDescription(Component.text(holderAnnotation.desc()));
            final HelpExecutor helpExecutor = new HelpExecutor(i18n);
            builder.setExecutor(helpExecutor);
// TODO reenable help subcmd           builder.child(builder().setExecutor(helpExecutor).build(), "?");
            //        builder.setExecutionRequirements()?
            //        builder.setExtendedDescription()!
            this.createChildCommands(holder, builder, plugin.getMetadata().getId(), "command", name);
            final Parameterized build = builder.build();
            helpExecutor.init(build, null,
                              String.join(".", Arrays.asList(plugin.getMetadata().getId(), "command", name)));
            event.register(plugin, build, name, holderAnnotation.alias());
        }
        else
        {
            if (holder instanceof DispatcherCommand)
            {
                throw new IllegalStateException(
                    "Base command needs a Command annotation! " + holder.getClass().getSimpleName());
            }
            final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(
                m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
            for (Method method : methods)
            {
                final Command methodAnnotation = method.getAnnotation(Command.class);
                String name = this.getCommandName(method, holder, methodAnnotation);
                final Parameterized build = this.buildCommand(holder, method, methodAnnotation,
                                                              plugin.getMetadata().getId(), "command", name);
                event.register(plugin, build, name, methodAnnotation.alias());
            }
        }
    }

    public void createParsers(Injector injector, Object holder)
    {
        final Using using = holder.getClass().getAnnotation(Using.class);
        if (using != null)
        {
            for (Class<?> parser : using.value())
            {
                final Object parserInstance = injector.getInstance(parser);
                final Class<?> parsedType = parser.getAnnotation(ParserFor.class).value();
                ParameterRegistry.register(parsedType, parserInstance);
            }
        }
    }

    private void createChildCommands(Object holder, Builder dispatcher, String... permNodes)
    {
        final String basePermNode = String.join(".", permNodes);
        dispatcher.setPermission(basePermNode + ".use");

        final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(
            m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
        for (Method method : methods)
        {
            this.createChildCommand(holder, dispatcher, method, permNodes);
        }

        if (holder instanceof DispatcherCommand)
        {
            for (Object subHolder : ((DispatcherCommand)holder).getSubCommands())
            {
                final Command subHolderAnnotation = subHolder.getClass().getAnnotation(Command.class);
                if (subHolderAnnotation != null)
                {
                    final Builder builder = builder();
                    final String name = this.getCommandName(null, subHolder, subHolderAnnotation);
                    builder.setShortDescription(Component.text(subHolderAnnotation.desc()));
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
                }
                else
                {
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
        if (!name.isEmpty())
        {
            return name;
        }
        if (method == null)
        {
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

    public static class Requirements implements Predicate<CommandCause>
    {
        private final List<Predicate<CommandCause>> requirements = new ArrayList<>();
        private String permission;

        @Override
        public boolean test(CommandCause commandCause)
        {
            for (Predicate<CommandCause> requirement : this.requirements)
            {
                if (!requirement.test(commandCause))
                {
                    return false;
                }
            }
            return true;
        }

        public void add(Predicate<CommandCause> predicate)
        {
            this.requirements.add(predicate);
        }

        public void addPermission(String permission)
        {
            this.permission = permission;
            this.requirements.add(c -> c.hasPermission(permission));
        }

        public String getPermission()
        {
            return permission;
        }
    }

    private Parameterized buildCommand(Object holder, Method method, Command annotation, String... permNodes)
    {
        final Builder builder = builder();

        final Annotation[][] annotationsList = method.getParameterAnnotations();
        final Type[] types = method.getParameterTypes();
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final List<ContextExtractor<?>> extractors = new ArrayList<>();
        final Requirements requirements = new Requirements();
        List<Parameter> params = new ArrayList<>();
        Map<Named, Parameter> namedParameter = new LinkedHashMap<>();
        List<org.spongepowered.api.command.parameter.managed.Flag> flags = new ArrayList<>();
        for (int i = 0; i < types.length; i++)
        {
            final Type type = types[i];
            final Annotation[] annotations = annotationsList[i];
            final java.lang.reflect.Parameter parameter = parameters[i];
            extractors.add(
                this.buildParameter(i, params, namedParameter, flags, parameter, type, annotations, types.length - 1, requirements));
        }
        buildParams(builder, params, namedParameter, flags);
        requirements.addPermission(String.join(".", permNodes) + ".use");
        builder.setExecutionRequirements(requirements);
        builder.setShortDescription(Component.text(annotation.desc()));
//        builder.setExtendedDescription()
        final CubeEngineCommand executor = new CubeEngineCommand(holder, method, extractors);
        builder.setExecutor(executor);
        final HelpExecutor helpExecutor = new HelpExecutor(i18n);
        builder.child(builder().setExecutor(helpExecutor).build(), "?");
        final Parameterized build = builder.build();
        helpExecutor.init(build, executor, String.join(".", permNodes));
        return build;
    }

    private void buildParams(Builder builder, List<Parameter> params, Map<Named, Parameter> namedParams, List<org.spongepowered.api.command.parameter.managed.Flag> flags)
    {
        for (Parameter param : params)
        {
            builder.parameter(param);
        }
        for (org.spongepowered.api.command.parameter.managed.Flag flag : flags)
        {
            builder.flag(flag);
        }
        for (Entry<Named, Parameter> namedParam : namedParams.entrySet())
        {
            {
                // Flag experiment
//                builder.flag(org.spongepowered.api.command.parameter.managed.Flag.of(namedParam.getValue(), namedParam.getKey().value()));
            }
            {
                // Sequence
                final Value<Boolean> literal = Parameter.literal(Boolean.class, true, namedParam.getKey().value()).setKey(namedParam.getKey().value()[0]).build();
                final Parameter named = Parameter.seqBuilder(literal).then(namedParam.getValue()).optional().terminal().build();
                builder.parameter(named);
            }



        }
    }

    private ContextExtractor<?> buildParameter(int index, List<Parameter> params, Map<Named, Parameter> namedParameter,
                                               List<org.spongepowered.api.command.parameter.managed.Flag> flags,
                                               java.lang.reflect.Parameter parameter, Type type,
                                               Annotation[] annotations, int last, Requirements requirements)
    {
        final String name = parameter.getName();
        // TODO search param annotation for name

        return buildParameter(index, params, namedParameter, flags, type, annotations, last, name, false, requirements);
    }

    private ContextExtractor<?> buildParameter(int index, List<Parameter> params, Map<Named, Parameter> namedParameter,
                                               List<org.spongepowered.api.command.parameter.managed.Flag> flags,
                                               Type type, Annotation[] annotations, int last, String name,
                                               boolean forceOptional, Requirements requirements)
    {

        if (type == CommandCause.class)
        {
            return COMMAND_CAUSE;
        }
        if (type == CommandContext.class)
        {
            return COMMAND_CONTEXT;
        }
        if (index == 0)
        {
            if (type == ServerPlayer.class)
            {
                // First Parameter and ServerPlayer == restricted ServerPlayer command
                requirements.add(AnnotationCommandBuilder::playerRestricted);
                return COMMAND_PLAYER;
            }
            if (type == Audience.class)
            {
                return COMMAND_AUDIENCE;
            }
        }

        Flag flagAnnotation = getAnnotated(annotations, Flag.class);
        Parser parserAnnotation = getAnnotated(annotations, Parser.class);

        Class<?> rawType = (Class<?>)(type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
        if (flagAnnotation != null)
        {
            return buildFlagParameter(flags, name, flagAnnotation, rawType);
        }
        final Parameter.Value.Builder<?> parameterBuilder;
        if (rawType == Optional.class)
        {
            return this.buildParameter(index, params, namedParameter, flags, ((ParameterizedType)type).getActualTypeArguments()[0],
                                       annotations, last, name, true, requirements);
        }
        else if (rawType == List.class)
        {
            throw new IllegalStateException("Not implemented yet");
        }
        else if (rawType == Set.class)
        {
            throw new IllegalStateException("Not implemented yet");
        }
        else
        {
            final Class<?> parserType = parserAnnotation != null && parserAnnotation.parser()
                != ValueParser.class ? parserAnnotation.parser() : rawType;
            final ValueParser parser = ParameterRegistry.getParser(parserType, index == last);
            if (parser != null)
            {
                parameterBuilder = Parameter.builder(rawType).parser(parser);
            }
            else
            {
                throw new IllegalArgumentException("Could not build Parameter for type: " + TypeToken.of(type));
            }

            final Class<?> completerType = parserAnnotation != null && parserAnnotation.completer()
                != ValueCompleter.class ? parserAnnotation.completer() : rawType;
            final ValueCompleter completer = ParameterRegistry.getCompleter(completerType);
            if (completer != null)
            {
                parameterBuilder.setSuggestions(completer);
            }
            else if (rawType == String.class)
            {
                parameterBuilder.setSuggestions((context, currentInput) -> Collections.emptyList());
            }
        }

        parameterBuilder.setKey(name);
        Default defaultAnnotation = getAnnotated(annotations, Default.class);
        final DefaultParameterProvider defaultParameterProvider;
        Named namedAnnotation = getAnnotated(annotations, Named.class);

        boolean optional = defaultAnnotation != null || namedAnnotation != null || isOptional(annotations);

        if (defaultAnnotation != null)
        {
            Class<?> clazz = defaultAnnotation.value();
            if (clazz == DefaultParameterProvider.class)
            {
                clazz = rawType;
            }
            defaultParameterProvider = ParameterRegistry.getDefaultProvider(clazz);
            if (namedAnnotation == null)
            {
                parameterBuilder.optional().orDefault(defaultParameterProvider);
            }
        }
        else
        {
            defaultParameterProvider = null;
            if (optional && namedAnnotation == null)
            {
                parameterBuilder.optional();
            }
        }

        Parameter.Value<?> param = parameterBuilder.build();

        if (namedAnnotation != null)
        {
            if (namedAnnotation.value().length == 0)
            {
                throw new IllegalArgumentException("Named parameter must have at least one name");
            }
            namedParameter.put(namedAnnotation, param);
        }
        else
        {
            params.add(param);
        }

        if (forceOptional)
        {
            return c -> c.getOne(param);
        }
        else if (optional)
        {
            if (namedAnnotation != null && defaultParameterProvider != null)
            {
                return c -> ((Optional)c.getOne(param)).orElse(defaultParameterProvider.apply(c.getCause()));
            }
            return c -> c.getOne(param).orElse(null);
        }
        else
        {
            return c -> c.requireOne(param);
        }
    }

    private ContextExtractor<Object> buildFlagParameter( List<org.spongepowered.api.command.parameter.managed.Flag> flags, String name, Flag flagAnnotation,
                                                        Class<?> rawType)
    {
        if (rawType == Boolean.class || rawType == boolean.class)
        {
            String longName = flagAnnotation.longName();
            if (longName.isEmpty())
            {
                longName = name;
            }
            String shortName = flagAnnotation.value();
            if (shortName.isEmpty())
            {
                shortName = longName.substring(0, 1);
            }

            // TODO permissions
            final org.spongepowered.api.command.parameter.managed.Flag flag = org.spongepowered.api.command.parameter.managed.Flag.builder().aliases(shortName, longName).build();
            flags.add(flag);
            return (c -> c.hasFlag(flag));
        }
        throw new IllegalArgumentException("@Flag parameter must be a boolean");
    }

    private static boolean playerRestricted(CommandCause cause)
    {
        final boolean isPlayer = cause.getAudience() instanceof ServerPlayer;
        if (!isPlayer)
        {
            cause.sendMessage(Identity.nil(), Component.text("This command is restricted to players in game")); // TODO translate
        }
        return isPlayer;
    }

    private <T extends Annotation> T getAnnotated(Annotation[] annotations, Class<T> clazz)
    {
        for (Annotation annotation : annotations)
        {
            if (clazz.isAssignableFrom(annotation.getClass()))
            {
                return clazz.cast(annotation);
            }
        }
        return null;
    }

    private boolean isOptional(Annotation[] annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof Option)
            {
                return true;
            }
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

    public static class HelpExecutor implements CommandExecutor
    {

        private I18n i18n;
        private Parameterized target;
        private CubeEngineCommand executor;
        private String perm;

        public HelpExecutor(I18n i18n)
        {
            this.i18n = i18n;
        }

        @Override
        public CommandResult execute(CommandContext context) throws CommandException
        {
            final Optional<String> actual = context.getCause().getContext().get(EventContextKeys.COMMAND);
            final Audience audience = context.getCause().getAudience();
            final Style grayStyle = Style.style(NamedTextColor.GRAY);
            Component descLabel = i18n.translate(audience, grayStyle, "Description:");
            final Component permText = i18n.translate(audience, grayStyle, "Permission: (click to copy) {input}",
                                                      perm).append(Component.text(".use").color(NamedTextColor.WHITE));
            descLabel = descLabel.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, permText)).clickEvent(
                ClickEvent.copyToClipboard(perm + ".use"));
            final Component descValue = target.getShortDescription(context.getCause()).get().color(NamedTextColor.GOLD);
            context.sendMessage(Identity.nil(), Component.empty().append(descLabel).append(Component.text(" ")).append(descValue));

            List<String> usages = new ArrayList<>();
            for (Parameter param : target.parameters())
            {
                if (param instanceof Parameter.Value)
                {
                    String usage = ((Parameter.Value<?>)param).getUsage(context.getCause());
                    if (!param.isOptional())
                    {
                        usage = "<" + usage + ">";
                    }
                    usages.add(usage);
                }
                else
                {
                    usages.add("param(" + param.getClass().getSimpleName() + ")");
                }
            }

            final String cmdPrefix = context.getCause().getAudience() instanceof ServerPlayer ? "/" : "";
            final String usage = usages.isEmpty() && executor == null ? "<command>" : Strings.join(usages, ' ');
            final String joinedUsage = cmdPrefix + actual.orElse("missing command context") + " " + usage;

            i18n.send(audience, grayStyle, "Usage: {input}", joinedUsage);

//            context.sendMessage(target.getUsage(context.getCause()).style(grayStyle));
//            context.sendMessage(Component.text(actual.orElse("no cmd?")));

            final List<Parameter.Subcommand> subcommands = target.subcommands().stream().filter(
                sc -> !sc.getAliases().iterator().next().equals("?")).collect(Collectors.toList());
            if (!subcommands.isEmpty())
            {
                context.sendMessage(Identity.nil(), Component.empty());
                i18n.send(audience, MessageType.NEUTRAL, "The following sub-commands are available:");
                context.sendMessage(Identity.nil(), Component.empty());
                for (Parameter.Subcommand subcommand : subcommands)
                {
                    final String firstAlias = subcommand.getAliases().iterator().next();
                    final Parameterized subCmd = subcommand.getCommand();
                    TextComponent textPart1 = Component.text(firstAlias, NamedTextColor.YELLOW);
                    final Component subPermText = i18n.translate(audience, grayStyle,
                                                                 "Permission: (click to copy) {input}",
                                                                 perm + "." + firstAlias).append(
                        Component.text(".use").color(NamedTextColor.WHITE));
                    textPart1 = textPart1.hoverEvent(
                        HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, subPermText)).clickEvent(
                        ClickEvent.copyToClipboard(perm + "." + firstAlias + ".use"));
                    final TextComponent text = Component.empty().append(textPart1).append(Component.text(": ")).append(
                        subCmd.getShortDescription(context.getCause()).get().style(grayStyle).hoverEvent(
                            HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                  Component.text("click to show usage"))).clickEvent(
                            ClickEvent.runCommand(cmdPrefix + actual.orElse("null") + " " + firstAlias + " ?"))
                        // TODO missing command context
                                                                                                                      );

                    context.sendMessage(Identity.nil(), text);
                }
            }
            else
            {
                if (this.executor == null)
                {
                    i18n.send(audience, MessageType.NEGATIVE, "No actions are available");
                }
            }
            context.sendMessage(Identity.nil(), Component.empty());
            return CommandResult.empty();
        }

        public void init(Parameterized target, CubeEngineCommand executor, String perm)
        {
            this.target = target;
            this.executor = executor;
            this.perm = perm;
        }
    }

    public static class CubeEngineCommand implements CommandExecutor
    {

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
        public CommandResult execute(CommandContext context) throws CommandException
        {

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
                e.printStackTrace();
                // TODO
                return CommandResult.error(Component.text(e.getCause().getMessage()));
            }

            return CommandResult.success();
        }
    }

    public interface ContextExtractor<T> extends Function<CommandContext, T>
    {
    }

    private static final ContextExtractor<CommandCause> COMMAND_CAUSE = CommandContext::getCause;
    private static final ContextExtractor<CommandContext> COMMAND_CONTEXT = c -> c;
    private static final ContextExtractor<ServerPlayer> COMMAND_PLAYER = c -> (ServerPlayer)c.getCause().getAudience();
    private static final ContextExtractor<Audience> COMMAND_AUDIENCE = c -> c.getCause().getAudience();
}
