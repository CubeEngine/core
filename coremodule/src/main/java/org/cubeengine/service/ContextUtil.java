package org.cubeengine.service;

import java.util.Collections;
import java.util.Set;
import org.spongepowered.api.service.context.Context;

import static org.spongepowered.api.service.permission.SubjectData.GLOBAL_CONTEXT;

public class ContextUtil
{
    public static final Context GLOBAL = new Context("global", "");

    public static Set<Context> toSet(Context context)
    {
        return GLOBAL.getType().equals(context.getType()) ? GLOBAL_CONTEXT : Collections.singleton(context);
    }
}
