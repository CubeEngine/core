package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.webapi.server.*;
import org.bukkit.plugin.Plugin;

/**
 * This controller is a basic controller that
 */
@Controller(name = "apibukkit", authenticate = true, serializer = "json")
public class ApibukkitController extends ApiController
{
    private final ApiManager manager;
    //private final JsonParser parser;

    public ApibukkitController(Plugin plugin)
    {
        super(plugin);
        this.manager = ApiManager.getInstance();
        //this.parser = new JsonParser();
    }

    @Action(authenticate = true, parameters =
    {
        "routes"
    })
    public void combined(ApiRequest request, ApiResponse response)
    {
//        JsonObject routes;
//        try
//        {
//            routes = this.parser.parse(request.params.get("routes")).getAsJsonObject();
//        }
//        catch (JsonParseException e)
//        {
//            throw new ApiRequestException("The given routes-parameter was no valid JSON!", 3);
//        }
//        catch (IllegalStateException e)
//        {
//            throw new ApiRequestException("The given routes-parameter was no JSON object!", 4);
//        }
//
//        HashMap<String, Object> responses = new HashMap<String, Object>();
//
//        ApiResponse apiResponse = new ApiResponse(this.manager.getDefaultSerializer());
//
//        for (Map.Entry<String, JsonElement> entry : routes.entrySet())
//        {
//            request.params.clear();
//            String route = String.valueOf(entry.getKey());
//            // debug("Route: " + route); -- TODO fix logging
//            String controllerName = route;
//            String actionName = null;
//            int delimPosition = route.indexOf("/");
//            if (delimPosition > -1)
//            {
//                controllerName = route.substring(0, delimPosition);
//                actionName = route.substring(delimPosition + 1);
//            }
//
//            ApiController controller = this.manager.getController(controllerName);
//            if (controller != null)
//            {
//                // debug("Got controller '" + controller.getClass().getSimpleName() + "'"); -- TODO fix logging
//                ApiAction action = controller.getAction(actionName);
//
//                JsonElement paramsElement = entry.getValue();
//                if (paramsElement.isJsonObject())
//                {
//                    JsonElement elem;
//                    for (Map.Entry<String, JsonElement> paramsEntry : paramsElement.getAsJsonObject().entrySet())
//                    {
//                        elem = paramsEntry.getValue();
//                        if (elem.isJsonPrimitive())
//                        {
//                            request.params.put(paramsEntry.getKey(), elem.getAsString());
//                        }
//                    }
//                }
//                try
//                {
//                    if (action != null)
//                    {
//                        if (this.checkRequiredParameters(action, request))
//                        {
//                            // debug("Running action '" + action.getClass().getSimpleName() + "'"); -- TODO fix logging
//                            action.execute(request, apiResponse);
//                        }
//                    }
//                    else
//                    {
//                        // debug("Running default action"); -- TODO fix logging
//                        controller.defaultAction(request, apiResponse);
//                    }
//                    responses.put(route, apiResponse.getContent());
//                    response.setHeaders(apiResponse.getHeaders());
//                    apiResponse.clearHeaders().setContent(null);
//                }
//                catch (ApiRequestException e)
//                {
//                    throw new ApiRequestException(e.getMessage(), -e.getCode());
//                }
//                catch (Throwable t)
//                {
//                    t.printStackTrace(System.err);
//                    // ApiBukkit.error("A route threw an unknown exception!", t); -- TODO fix logging
//                    throw new ApiRequestException("A route has thrown an unknown exception!", 2);
//                }
//            }
//            else
//            {
//                throw new ApiRequestException("Controller '" + controllerName + "' could not be found!", 1);
//            }
//            response.setContent(responses);
//        }
    }

    private boolean checkRequiredParameters(ApiAction action, ApiRequest request)
    {
        for (String param : action.getParameters())
        {
            if (!request.params.containsKey(param))
            {
                return false;
            }
        }
        return true;
    }

    //@Action(authenticate = false)
    public void testing(ApiRequest request, ApiResponse response)
    {
    }
}