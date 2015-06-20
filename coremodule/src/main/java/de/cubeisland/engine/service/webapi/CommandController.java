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
package de.cubeisland.engine.service.webapi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cubeisland.engine.service.i18n.I18n;
import de.cubeisland.engine.service.command.CommandManager;
import de.cubeisland.engine.service.task.TaskManager;
import de.cubeisland.engine.service.user.User;
import de.cubeisland.engine.service.webapi.sender.ApiCommandSender;
import de.cubeisland.engine.service.webapi.sender.ApiServerSender;
import de.cubeisland.engine.service.webapi.sender.ApiUser;

public class CommandController
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final TaskManager tm;
    private final CommandManager cm;
    private I18n i18n;

    public CommandController(I18n i18n, TaskManager tm, CommandManager cm)
    {
        this.tm = tm;
        this.cm = cm;
        this.i18n = i18n;
    }

    @Action
    public ApiResponse command(ApiRequest request, final @Value("cmd") String command)
    {
        User authUser = request.getAuthUser();
        final ApiCommandSender sender = authUser == null ? new ApiServerSender(i18n, mapper) : new ApiUser(i18n, authUser, mapper);


        Future<ApiResponse> future =  tm.callSync(() -> {
            cm.runCommand(sender, command);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setContent(sender.flush());
            return apiResponse;
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
