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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Buildable;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.annotation.Alias;
import org.cubeengine.libcube.service.command.annotation.Command;
import org.cubeengine.libcube.service.command.annotation.Default;
import org.cubeengine.libcube.service.command.annotation.Delegate;
import org.cubeengine.libcube.service.command.annotation.Flag;
import org.cubeengine.libcube.service.command.annotation.Greedy;
import org.cubeengine.libcube.service.command.annotation.Label;
import org.cubeengine.libcube.service.command.annotation.Named;
import org.cubeengine.libcube.service.command.annotation.Option;
import org.cubeengine.libcube.service.command.annotation.ParameterPermission;
import org.cubeengine.libcube.service.command.annotation.Parser;
import org.cubeengine.libcube.service.command.annotation.ParserFor;
import org.cubeengine.libcube.service.command.annotation.Restricted;
import org.cubeengine.libcube.service.command.annotation.Using;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command.Builder;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.Parameter.FirstOfBuilder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.Parameter.SequenceBuilder;
import org.spongepowered.api.command.parameter.Parameter.Subcommand;
import org.spongepowered.api.command.parameter.Parameter.Value;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import static org.spongepowered.api.command.Command.builder;


/*
TODO handle @Alias with spaces
TODO @Restricted msg translation
 */
@Singleton
public class AnnotationCommandBuilder
{

    private ModuleManager momu;
    private I18n i18n;

    @Inject
    public AnnotationCommandBuilder(ModuleManager momu, I18n i18n)
    {
        this.momu = momu;
        this.i18n = i18n;
    }

    public Map<CommandMapping, Parameterized> registerModuleCommands(Injector injector, RegisterCommandEvent<Parameterized> event,
                                                                     PluginContainer plugin, Object module, List<Field> commands)
    {
        Map<CommandMapping, org.spongepowered.api.command.Command.Parameterized> moduleCommands = new HashMap<>();
        for (Field command : commands)
        {
            try
            {
                command.setAccessible(true);
                this.registerCommands(injector, module.getClass(), event, plugin, command.get(module), moduleCommands);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
        return moduleCommands;
    }

    public void registerCommands(Injector injector, Class<?> moduleClass, RegisterCommandEvent<Parameterized> event, PluginContainer plugin, Object holder, Map<CommandMapping, Parameterized> moduleCommands)
    {
        final Command holderAnnotation = holder.getClass().getAnnotation(Command.class);
        this.createParsers(injector, holder);
        if (holderAnnotation != null)
        {
            final Builder builder = builder();
            final String name = this.getCommandName(null, holder, holderAnnotation);

            builder.shortDescription(Component.text(holderAnnotation.desc()));

            //        builder.setExecutionRequirements()?
            //        builder.setExtendedDescription()!
            final Optional<CommandExecutor> dispatcherExecutor = this.createChildCommands(event, injector, moduleClass, plugin, holder, builder, getBasePerm(plugin), "command", name);

            final HelpExecutor helpExecutor = new HelpExecutor(i18n);
            builder.executor(new DispatcherExecutor(helpExecutor, dispatcherExecutor.orElse(null)));
            builder.addChild(builder().executor(helpExecutor).build(), "?");

            final Parameterized build = builder.build();
            helpExecutor.init(build, null,
                              String.join(".", Arrays.asList(getBasePerm(plugin), "command", name)));

            final CommandMapping mapping = event.register(plugin, build, name, holderAnnotation.alias()).mapping();
            moduleCommands.put(mapping, build);
        }
        else
        {
            if (holder instanceof DispatcherCommand)
            {
                throw new IllegalStateException("Base command needs a Command annotation! " + holder.getClass().getSimpleName());
            }
            final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(
                m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
            for (Method method : methods)
            {
                final Command methodAnnotation = method.getAnnotation(Command.class);
                String name = this.getCommandName(method, holder, methodAnnotation);
                try
                {
                    final Parameterized build = this.buildCommand(injector, holder, method, methodAnnotation, getBasePerm(plugin), "command", name);
                    final CommandMapping mapping = event.register(plugin, build, name, methodAnnotation.alias()).mapping();
                    moduleCommands.put(mapping, build);
                }
                catch (Exception e)
                {
                    momu.getLoggerFor(moduleClass).error(e, "Failed to register command {} in {}", name, holder.getClass().getSimpleName());
                }
            }
        }
    }

    public String getBasePerm(PluginContainer plugin)
    {
        final String id = plugin.getMetadata().getId();
        if (id.startsWith("cubeengine-"))
        {
            return "cubeengine." + id.substring(11);
        }
        return id;
    }

    public void createParsers(Injector injector, Object holder)
    {
        final Using using = holder.getClass().getAnnotation(Using.class);
        if (using != null)
        {
            for (Class<?> parser : using.value())
            {
                final Object parserInstance = injector.getInstance(parser);
                final ParserFor parserAnnot = parser.getAnnotation(ParserFor.class);
                if (parserAnnot == null)
                {
                    throw new IllegalStateException("Parser " + parser.getName() + " is missing @ParserFor annotation");
                }
                final Class<?> parsedType = parserAnnot.value();
                ParameterRegistry.register(parsedType, parserInstance);
            }
        }
    }

    private Optional<CommandExecutor> createChildCommands(RegisterCommandEvent<Parameterized> event, Injector injector, Class<?> moduleClass, PluginContainer plugin, Object holder, Builder dispatcher, String... permNodes)
    {
        final String basePermNode = String.join(".", permNodes);
        dispatcher.permission(basePermNode + ".use");
        Optional<CommandExecutor> dispatcherExecutor = Optional.empty();
        final Set<Method> methods = this.getMethods(holder.getClass()).stream().filter(
            m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toSet());
        for (Method method : methods)
        {
            final Command methodAnnotation = method.getAnnotation(Command.class);
            String name = this.getCommandName(method, holder, methodAnnotation);
            try
            {
                if (methodAnnotation.dispatcher())
                {
                    final String[] newPermNodes = Arrays.copyOf(permNodes, permNodes.length + 1);
                    newPermNodes[permNodes.length] = name;
                    final Parameterized build = buildCommand(injector, holder, method, methodAnnotation, newPermNodes);

                    dispatcher.addParameters(build.parameters());

                    // TODO execution requirements - but they do not apply for child commands does it work?
                    final Predicate<CommandCause> requirements = build.executionRequirements();
                    // TODO would get overwritten by other requirements... maybe wrap executor instead?
                    dispatcher.executionRequirements(requirements);

                    dispatcherExecutor = build.executor();
                }
                else
                {
                    this.createChildCommand(event, injector, plugin, holder, dispatcher, method, methodAnnotation, permNodes);
                }
            }
            catch (Exception e)
            {
                momu.getLoggerFor(moduleClass).error(e, "Failed to register command {} in {}", name, holder.getClass().getSimpleName());
            }
        }

        if (holder instanceof DispatcherCommand)
        {
            for (Object subHolder : ((DispatcherCommand)holder).getSubCommands())
            {
                this.createParsers(injector, subHolder);
                final Command subHolderAnnotation = subHolder.getClass().getAnnotation(Command.class);
                if (subHolderAnnotation != null)
                {
                    final Builder builder = builder();
                    final String name = this.getCommandName(null, subHolder, subHolderAnnotation);
                    builder.shortDescription(Component.text(subHolderAnnotation.desc()));
                    //        builder.setExecutionRequirements()?
                    //        builder.setExtendedDescription()!
                    final String[] newPermNodes = Arrays.copyOf(permNodes, permNodes.length + 1);
                    newPermNodes[permNodes.length] = name;
                    final Optional<CommandExecutor> subDispatcherExecutor = this.createChildCommands(event, injector, moduleClass, plugin, subHolder, builder, newPermNodes);
                    final HelpExecutor helpExecutor = new HelpExecutor(i18n);
                    builder.executor(new DispatcherExecutor(helpExecutor, subDispatcherExecutor.orElse(null)));
                    final List<String> alias = new ArrayList<>();
                    alias.add(name);
                    alias.addAll(Arrays.asList(subHolderAnnotation.alias()));

                    final Parameterized helpChild = builder().executor(helpExecutor).build();
                    builder.addChild(helpChild, "?");

                    final Parameterized build = builder.build();
                    helpExecutor.init(build, null, String.join(".", permNodes));
                    dispatcher.addChild(build, alias);

                    final Alias aliasAnnotation = subHolder.getClass().getAnnotation(Alias.class);
                    if (aliasAnnotation != null)
                    {
                        event.register(plugin, build, aliasAnnotation.value(), aliasAnnotation.alias());
                    }
                }
                else
                {
                    this.createChildCommands(event, injector, moduleClass, plugin, subHolder, dispatcher, permNodes);
                }
            }
        }
        return dispatcherExecutor;
    }

    private void createChildCommand(RegisterCommandEvent<Parameterized> event, Injector injector, PluginContainer plugin, Object holder, Builder dispatcher, Method method, Command methodAnnotation, String... permNodes)
    {
        String name = this.getCommandName(method, holder, methodAnnotation);
        final String[] newPermNodes = Arrays.copyOf(permNodes, permNodes.length + 1);
        newPermNodes[permNodes.length] = name;
        final Parameterized build = buildCommand(injector, holder, method, methodAnnotation, newPermNodes);
        final List<String> alias = new ArrayList<>();
        alias.add(name);
        alias.addAll(Arrays.asList(methodAnnotation.alias()));
        dispatcher.addChild(build, alias);

        if (this.isDelegateMethod(holder, name))
        {
            final List<Parameter> parameters = build.parameters();
            dispatcher.addParameters(parameters);
            dispatcher.executor(build.executor().get());
        }

        final Alias aliasAnnotation = method.getAnnotation(Alias.class);
        if (aliasAnnotation != null)
        {
            event.register(plugin, build, aliasAnnotation.value(), aliasAnnotation.alias());
        }

    }

    private boolean isDelegateMethod(Object holder, String name)
    {
        final Delegate annotation = holder.getClass().getAnnotation(Delegate.class);
        return annotation != null && annotation.value().equals(name);
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

        public void add(Restricted restricted)
        {
            if (restricted != null)
            {
                final Class<?> restrictedTo = restricted.value();
                final String msg = restricted.msg().isEmpty() ? "Command is restricted to " + restrictedTo.getSimpleName() : restricted.msg();

                this.add(commandCause -> {
                    if (!restrictedTo.isAssignableFrom(commandCause.subject().getClass()))
                    {
// TODO custom error message                       commandCause.getAudience().sendMessage(Identity.nil(), Component.text(msg));
                        return false;
                    }
                    return true;
                });
            }
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

    private Parameterized buildCommand(Injector injector, Object holder, Method method, Command annotation, String... permNodes)
    {
        final Builder builder = builder();

        final Annotation[][] annotationsList = method.getParameterAnnotations();
        final Type[] types = method.getGenericParameterTypes();
        final java.lang.reflect.Parameter[] parameters = method.getParameters();
        final List<ContextExtractor<?>> extractors = new ArrayList<>();
        final Requirements requirements = new Requirements();
        List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params = new ArrayList<>();
        Map<Named, org.spongepowered.api.util.Builder<? extends Parameter, ?>> namedParameter = new LinkedHashMap<>();
        for (int i = 0; i < types.length; i++)
        {
            final Type type = types[i];
            final Annotation[] annotations = annotationsList[i];
            final java.lang.reflect.Parameter parameter = parameters[i];
            extractors.add(
                this.buildParameter(i, params, namedParameter, parameter, type, annotations, types.length - 1, requirements, injector, permNodes));
        }
        final CubeEngineCommand executor = new CubeEngineCommand(holder, method, extractors, injector);
        buildParams(builder, params, namedParameter, executor);
        requirements.addPermission(String.join(".", permNodes) + ".use");
        requirements.add(method.getAnnotation(Restricted.class));
        builder.executionRequirements(requirements);
        builder.shortDescription(Component.text(annotation.desc()));
//        builder.setExtendedDescription()
        builder.executor(executor);
        final HelpExecutor helpExecutor = new HelpExecutor(i18n);
        builder.addChild(builder().executor(helpExecutor).build(), "?");
        final Parameterized build = builder.build();
        helpExecutor.init(build, executor, String.join(".", permNodes));
        return build;
    }

    private <T> void buildParams(Builder builder, List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params, Map<Named, org.spongepowered.api.util.Builder<? extends Parameter, ?>> namedParams,
                                 CubeEngineCommand executor)
    {
        for (int i = 0; i < params.size(); i++)
        {
            final org.spongepowered.api.util.Builder<? extends Parameter, ?> param = params.get(i);
            if (i == params.size()-1)
            {
                if (param instanceof Value.Builder)
                {
                    ((Value.Builder)param).terminal();
                }
                else if (param instanceof FirstOfBuilder)
                {
                    ((FirstOfBuilder)param).terminal();
                }
                else if (param instanceof SequenceBuilder)
                {
                    ((SequenceBuilder)param).terminal();
                }
            }
            builder.addParameter(param.build());
        }

        Map<Named, org.spongepowered.api.command.Command.Builder> namedSubCommands = new LinkedHashMap<>();
        for (Entry<Named, org.spongepowered.api.util.Builder<? extends Parameter, ?>> namedParam : namedParams.entrySet())
        {
            {
                // Flag experiment
//                builder.flag(org.spongepowered.api.command.parameter.managed.Flag.of(namedParam.getValue(), namedParam.getKey().value()));
            }
            {
                final Builder namedSubCmd = builder().addParameter(namedParam.getValue().build()).executor(executor);
                namedSubCommands.put(namedParam.getKey(), namedSubCmd);

//                // Sequence
//                final Value<Boolean> literal = Parameter.literal(Boolean.class, true, namedParam.getKey().value())
//                                                        .setSuggestions((f,g) -> Arrays.asList(namedParam.getKey().value()))
//                                                        .setKey(namedParam.getKey().value()[0]).build();
//                final Parameter named = Parameter.seqBuilder(literal).then(namedParam.getValue().build()).optional().terminal().build();
//                builder.parameter(named);
            }
        }

        if (!namedSubCommands.isEmpty())
        {
            final ArrayList<Entry<Named, Builder>> reverse = new ArrayList<>(namedSubCommands.entrySet());
            Collections.reverse(reverse);
            List<Subcommand> subcommands = new ArrayList<>();
            for (Entry<Named, Builder> namedParam : reverse)
            {
                if (!subcommands.isEmpty())
                {
                    final FirstOfBuilder firstofBuilder = Parameter.firstOfBuilder(subcommands.get(0)).optional();
                    for (int i = 1, size = subcommands.size(); i < size; i++)
                    {
                        firstofBuilder.or(subcommands.get(i));
                    }
                    namedParam.getValue().addParameter(firstofBuilder.build());
                }

                subcommands.add(Parameter.subcommand(namedParam.getValue().build(), namedParam.getKey().value()[0],
                                                                         Arrays.copyOfRange(namedParam.getKey().value(), 1, namedParam.getKey().value().length)));
            }
            Collections.reverse(subcommands);

            final FirstOfBuilder namedBuilder = Parameter.firstOfBuilder(subcommands.get(0)).optional();
            for (int i = 1, subcommandsSize = subcommands.size(); i < subcommandsSize; i++)
            {
                namedBuilder.or(subcommands.get(i));
            }
            builder.addParameter(namedBuilder.build());
        }
    }

    private ContextExtractor<?> buildParameter(int index, List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params,
                                               Map<Named, org.spongepowered.api.util.Builder<? extends Parameter, ?>> namedParameter, java.lang.reflect.Parameter parameter, Type type,
                                               Annotation[] annotations, int last, Requirements requirements, Injector injector, String[] permNodes)
    {
        final String name = parameter.getName();
        // TODO search param annotation for name

        return buildParameter(index, params, namedParameter, type, annotations, last, name, false, requirements, injector, permNodes);
    }

    private <T> ContextExtractor<?> buildParameter(int index, List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params,
                                                   Map<Named, org.spongepowered.api.util.Builder<? extends Parameter, ?>> namedParameter, Type type, Annotation[] annotations, int last, String name,
                                                   boolean forceOptional, Requirements requirements, Injector injector, String[] permNodes)
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

        final Flag flagAnnotation = getAnnotated(annotations, Flag.class);
        final ParameterPermission permAnnotation = getAnnotated(annotations, ParameterPermission.class);

        Class<?> rawType = (Class<?>)(type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
        if (flagAnnotation != null)
        {
            return this.buildFlagParameter(params, name, flagAnnotation, permAnnotation, rawType, permNodes);
        }
        if (rawType == Optional.class)
        {
            return this.buildParameter(index, params, namedParameter, ((ParameterizedType)type).getActualTypeArguments()[0],
                                       annotations, last, name, true, requirements, injector, permNodes);
        }
        if (FirstValueParameter.class.isAssignableFrom(rawType)) {
            List<ContextExtractor<?>> extractors = new ArrayList<>();
            for (Field field : rawType.getFields())
            {
                final Type fieldType = field.getGenericType();
                extractors.add(this.buildParameter(index, params, namedParameter, fieldType,
                                                   annotations, last, field.getName(), true, requirements, injector, permNodes));

            }
            return this.buildFirstValueParameter(injector, extractors, params, rawType);
        }


        final Parameter.Value.Builder parameterBuilder = Parameter.builder((TypeToken<T>)TypeToken.get(type));
        final Parser parserAnnotation = getAnnotated(annotations, Parser.class);
        final Greedy greedyAnnotation = getAnnotated(annotations, Greedy.class);
        final Class<? extends ValueParameter<T>> customParserType = parserAnnotation != null && parserAnnotation.parser() != ValueParser.class ? (Class<ValueParameter<T>>) parserAnnotation.parser() : null;
        final Class<? extends ValueCompleter> customCompleterType = parserAnnotation != null && parserAnnotation.completer() != ValueCompleter.class ? parserAnnotation.completer() : null;

        parameterBuilder.addParser(ParameterRegistry.getParser(injector, type, customParserType,index == last, greedyAnnotation != null));

        final ValueCompleter completer = ParameterRegistry.getCompleter(injector, type, customCompleterType);
        if (completer != null)
        {
            parameterBuilder.suggestions(completer);
        }

        parameterBuilder.key(name);
        final Key<T> key = (Key<T>) Parameter.key(name, TypeToken.get(type));

        Default defaultAnnotation = getAnnotated(annotations, Default.class);
        Named namedAnnotation = getAnnotated(annotations, Named.class);

        boolean optional = defaultAnnotation != null || namedAnnotation != null || isOptional(annotations);

        final DefaultParameterProvider<T> defaultParameterProvider;
        if (defaultAnnotation != null)
        {
            defaultParameterProvider = ParameterRegistry.getDefaultProvider(injector, type, defaultAnnotation.value());
            if (namedAnnotation == null)
            {
                parameterBuilder.optional();
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

        Label labelAnnotation = getAnnotated(annotations, Label.class);
        if (labelAnnotation != null)
        {
            parameterBuilder.usage(k -> labelAnnotation.value());
        }

        if (permAnnotation != null)
        {
            parameterBuilder.requiredPermission(String.join(".", permNodes) + "." + name);
        }

        if (namedAnnotation != null)
        {
            if (namedAnnotation.value().length == 0)
            {
                throw new IllegalArgumentException("Named parameter must have at least one name");
            }
            namedParameter.put(namedAnnotation, parameterBuilder);
        }
        else
        {
            params.add(parameterBuilder);
        }

        if (forceOptional)
        {
            return new OptionalContextExtractor<>(key);
        }
        else
        {
            return new SimpleContextExtractor<>(key, optional, defaultParameterProvider);
        }
    }

    private static class SimpleContextExtractor<T> implements ContextExtractor<T>
    {
        private Parameter.Key<T> key;
        boolean optional;
        private DefaultParameterProvider<T> defaultParameterProvider;

        public SimpleContextExtractor(Key<T> key, boolean optional, DefaultParameterProvider<T> defaultParameterProvider)
        {
            this.key = key;
            this.optional = optional;
            this.defaultParameterProvider = defaultParameterProvider;
        }

        @Override
        public T apply(CommandContext commandContext)
        {
            if (this.optional)
            {
                if (defaultParameterProvider != null)
                {
                    return commandContext.one(key).orElse(defaultParameterProvider.apply(commandContext.cause()));
                }
                return commandContext.one(key).orElse(null);
            }
            return commandContext.requireOne(key);
        }
    }

    private static class OptionalContextExtractor<T> implements ContextExtractor<Optional<T>>
    {
        private Parameter.Key<T> key;

        public OptionalContextExtractor(Key<T> key)
        {
            this.key = key;
        }

        @Override
        public Optional<T> apply(CommandContext commandContext)
        {
            return commandContext.one(key);
        }
    }

    private static class FirstOfContextExtractor<T> implements ContextExtractor<T>
    {
        private final Field[] fields;
        private Class<T> clazz;
        private Injector injector;
        private final List<ContextExtractor<?>> extractors;

        public FirstOfContextExtractor(Class<T> clazz, Injector injector, List<ContextExtractor<?>> extractors)
        {
            this.clazz = clazz;
            this.injector = injector;
            this.extractors = extractors;
            this.fields = clazz.getFields();
        }

        @Override
        public T apply(CommandContext commandContext)
        {
            final T instance = injector.getInstance(clazz);
            try
            {
                for (int i = 0; i < fields.length; i++)
                {
                    final Field field = fields[i];
                    Object extractedValue = extractors.get(i).apply(commandContext);
                    if (extractedValue instanceof Optional && field.getType() != Optional.class) {
                        extractedValue = ((Optional<?>)extractedValue).orElse(null);
                    }
                    field.set(instance, extractedValue);
                }
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException("Failed to extract context for " + clazz.getName(), e);
            }
            return instance;
        }
    }

    private ContextExtractor<?> buildFirstValueParameter(Injector injector, List<ContextExtractor<?>> extractors, List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params, Class<?> rawType)
    {
        final List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> firstValueParams = new ArrayList<>(params.subList(params.size() - extractors.size(), params.size()));
        params.removeAll(firstValueParams);
        final FirstOfBuilder firstOfBuilder = Sponge.game().builderProvider().provide(FirstOfBuilder.class);
        firstOfBuilder.orFirstOf(firstValueParams.stream().map(Buildable.Builder::build).collect(Collectors.toList()));
        params.add(firstOfBuilder);

        return new FirstOfContextExtractor<>(rawType, injector, extractors);
    }

    private ContextExtractor<Boolean> buildFlagParameter(List<org.spongepowered.api.util.Builder<? extends Parameter, ?>> params, String name, Flag flagAnnotation,
                                                        ParameterPermission permAnnotation, Class<?> rawType, String[] permNodes)
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

            final String finalShortName = shortName;
            final String finalLongName = longName;
            final Value.Builder<Boolean> builderShort = Parameter.literal(Boolean.class, true, "-" + shortName).optional().key(name).usage(key -> "-" + finalShortName);
            final Value.Builder<Boolean> builderLong = Parameter.literal(Boolean.class, true, "-" + longName).optional().key(name).usage(key -> "-" + finalLongName);
            if (permAnnotation != null)
            {
                builderShort.requiredPermission(String.join(".", permNodes) + "." + name);
                builderLong.requiredPermission(String.join(".", permNodes) + "." + name);
            }

            final FirstOfBuilder flagParamBuilder = Parameter.firstOfBuilder(builderShort.build()).or(builderLong.build()).optional();
            final Key<Boolean> key = Parameter.key(name, Boolean.class);
            params.add(flagParamBuilder);
            return new SimpleContextExtractor<>(key, true, c -> false);
        }
        throw new IllegalArgumentException("@Flag parameter must be a boolean");
    }

    private static boolean playerRestricted(CommandCause cause)
    {
        final boolean isPlayer = cause.subject() instanceof ServerPlayer;
        if (!isPlayer)
        {
            // TODO show error message?
            // TODO translate
//            throw new IllegalStateException("This command is restricted to players in game");
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

    public interface ContextExtractor<T> extends Function<CommandContext, T>
    {
    }

    private static final ContextExtractor<CommandCause> COMMAND_CAUSE = CommandContext::cause;
    private static final ContextExtractor<CommandContext> COMMAND_CONTEXT = c -> c;
    private static final ContextExtractor<ServerPlayer> COMMAND_PLAYER = c -> (ServerPlayer)c.cause().audience();
    private static final ContextExtractor<Audience> COMMAND_AUDIENCE = c -> c.cause().audience();
}
