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
package org.cubeengine.libcube.service.command.example;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.cubeengine.libcube.service.command.annotation.Command;
import org.cubeengine.libcube.service.command.DispatcherCommand;
import org.spongepowered.api.command.CommandCause;

@Command(name = "sub1", desc = "child command 1")
public class ChildExampleCommand1 extends DispatcherCommand {

    @Command(desc = "Does a foo")
    public void foo(CommandCause cause, String firstParam) {
        cause.sendMessage(Identity.nil(), Component.text(firstParam));
    }
}
