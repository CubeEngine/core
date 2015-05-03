/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command.sender;

import java.util.Locale;
import de.cubeisland.engine.core.sponge.SpongeCore;
import org.spongepowered.api.util.command.source.ConsoleSource;

public class ConsoleCommandSender extends WrappedCommandSender implements ConsoleSource
{
    public static final String NAME = ":console";

    public ConsoleCommandSender(SpongeCore core)
    {
        super(core, core.getGame().getServer().getConsole());
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getDisplayName()
    {
        return this.getCore().getI18n().translate(Locale.getDefault(), "Console");
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

}
