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
package de.cubeisland.engine.core.webapi;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.webapi.sender.ApiCommandSender;
import de.cubeisland.engine.core.webapi.sender.ApiServerSender;
import de.cubeisland.engine.core.webapi.sender.ApiUser;

public class CommandController
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final Core core;

    public CommandController(Core core)
    {
        this.core = core;
    }

    @Action
    public ApiResponse command(ApiRequest request, final @Value("cmd") String command)
    {
        User authUser = request.getAuthUser();
        final ApiCommandSender sender = authUser == null ? new ApiServerSender(core, mapper) : new ApiUser(core, authUser, mapper);

        Future<ApiResponse> future = core.getTaskManager().callSync(new Callable<ApiResponse>()
        {
            @Override
            public ApiResponse call() throws Exception
            {
                core.getCommandManager().runCommand(sender, command);
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setContent(sender.flush());
                return apiResponse;
            }
        });
        try
        {
            return future.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return null;
        }
        catch (ExecutionException e)
        {
            return new ApiResponse();
        }
    }
}
