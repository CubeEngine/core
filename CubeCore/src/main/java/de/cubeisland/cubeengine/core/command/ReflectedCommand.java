package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Named;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.map.hash.THashMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class ReflectedCommand extends CubeCommand
{
    private final Object commandContainer;
    private final Method commandMethod;
    private final Flag[] flags;
    private final int min;
    private final int max;
    private Map<String[], Class<?>> namedParameters;

    public ReflectedCommand(Module module, Object commandContainer, Method method, Command annotation, String name, String description, String usage, List<String> aliases)
    {
        super(module, name, description, usage, aliases);
        this.commandMethod = method;
        this.commandContainer = commandContainer;
        
        this.flags = annotation.flags();
        this.min = annotation.min();
        this.max = annotation.max();
        this.namedParameters = new THashMap<String[], Class<?>>();
        
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] paramTypes = method.getParameterTypes();
        
        Annotation[] paramAnnotations;
        Named namedAnnotation;
        for (int i = 1; i < paramTypes.length; ++i)
        {
            namedAnnotation = null;
            paramAnnotations = annotations[i];
            for (int j = 0; j < paramAnnotations.length; ++j)
            {
                if (paramAnnotations[i] instanceof Named)
                {
                    namedAnnotation = (Named)paramAnnotations[j];
                    break;
                }
            }
            if (namedAnnotation != null)
            {
                this.namedParameters.put(namedAnnotation.value(), paramTypes[i]);
            }
        }
    }

    @Override
    public void run(CommandContext context)
    {
        try 
        {
            // TODO permission check -> throw exception
            
            this.commandMethod.invoke(this.commandContainer, context);
        }
        catch (Exception e)
        {
            context.setResult(false);
        }
    }
}
