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
package de.cubeisland.engine.core.bukkit;

import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.CoreConfiguration;

public class BukkitCoreConfiguration extends CoreConfiguration
{
    @Comment("Whether to prevent Bukkit from kicking players for spamming")
    public boolean preventSpamKick = false;

    @Comment("Whether to replace the vanilla standard commands with improved ones")
    @Name("commands.improve-vanilla")
    public boolean improveVanilla = true;

    @Comment({"The enhanced system introduces a few user experience improvements,", "but my cause problems with different plugins that inject commands (ex. MCore)"})
    @Name("commands.use-enhanced-system")
    public boolean useEnhancedSystem = false;

    @Comment("This the string that will be prepended to commands that get overridden by one of our commands")
    @Name("commands.default-fallback")
    public String defaultFallback = "fallback";

    @Comment("This allows the CubeEngine to act when signals are send to the Minecraft server")
    public boolean catchSystemSignals = true;
}
