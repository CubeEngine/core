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
package de.cubeisland.engine.vote;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.util.time.Duration;

public class VoteConfiguration extends YamlConfiguration
{
    @Option("vote-reward")
    public double votereward = 100.0;
    @Comment("{PLAYER} will be replaced with the player-name\n" +
             "{MONEY} will be replaced with the money the player receives\n" +
             "{AMOUNT} will be replaced with the amount of times that player voted")
    @Option("vote-broadcast")
    public String votebroadcast = "&6{PLAYER} voted!";
    @Option("vote-message")
    public String votemessage = "&aYou received {MONEY} for voting {AMOUNT} times!";

    @Comment("Players will receive a bonus if they vote multiple times in given time-frame")
    @Option("vote-bonus-time")
    public Duration votebonustime = new Duration(TimeUnit.HOURS.toMillis(36));
}
