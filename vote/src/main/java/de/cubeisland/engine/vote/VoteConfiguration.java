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

import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.annotations.Comment;
import org.joda.time.Duration;

@SuppressWarnings("all")
public class VoteConfiguration extends ReflectedYaml
{
    public double voteReward = 100.0;

    @Comment({"{PLAYER} will be replaced with the player-name",
             "{MONEY} will be replaced with the money the player receives",
             "{AMOUNT} will be replaced with the amount of times that player voted",
             "{VOTEURL} will be replaced with the configured vote-url"})
    public String voteBroadcast = "&6{PLAYER} voted!";

    public String voteMessage = "&aYou received {MONEY} for voting {AMOUNT} times!";

    @Comment("Players will receive a bonus if they vote multiple times in given time-frame")
    public Duration voteBonusTime = new Duration(TimeUnit.HOURS.toMillis(36));

    public String voteUrl = "";
}
