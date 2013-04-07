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
package de.cubeisland.cubeengine.irc;

import de.cubeisland.cubeengine.core.module.Module;

/**
 * Represents a Irc
 */
public class Irc extends Module
{
    private IrcConfig config;
    private BotManager mgr;

    @Override
    public void onEnable()
    {
    //        this.mgr = new BotManager(this.config);
    //        this.registerListener(new IrcListener(this));
    //        this.mgr.connect();
    //        this.registerCommands(new Test(this));
    //        this.getFileManager().dropResources(Test.values());
    //        this.registerPermissions(Test.values());
    //        this.flyManager = new FlyManager(this.getDatabase(), this.getInfo().getRevision());
    }

    @Override
    public void onDisable()
    {
    //        this.mgr.clean();
    //        this.mgr = null;
    }

    public BotManager getBotManager()
    {
        return this.mgr;
    }
}
