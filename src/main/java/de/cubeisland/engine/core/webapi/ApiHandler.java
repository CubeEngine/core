package de.cubeisland.engine.core.webapi;

import java.util.LinkedHashMap;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;

public abstract class ApiHandler
{
    private final Module module;
    private final String route; // and command (for ws)
    private final Permission permission;
    private final LinkedHashMap<String, Class> parameters;
    private final RequestMethod reqMethod;

    protected ApiHandler(Module module, String route, Permission perm, LinkedHashMap<String, Class> params, RequestMethod reqMethod)
    {
        this.module = module;
        this.route = route;
        this.permission = perm;
        this.parameters = params;
        this.reqMethod = reqMethod;
    }

    public abstract ApiResponse execute(ApiRequest request);

    public Module getModule()
    {
        return module;
    }

    public String getRoute()
    {
        return route;
    }

    public Permission getPermission()
    {
        return permission;
    }

    public LinkedHashMap<String, Class> getParameters()
    {
        return parameters;
    }

    public RequestMethod getReqMethod()
    {
        return reqMethod;
    }
}
