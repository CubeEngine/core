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
package de.cubeisland.cubeengine.basics.storage;

import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;

public class BasicUserManager extends SingleKeyStorage<Long, BasicUser>
{
    private final Core core;
    private final Basics module;
    private static final int REVISION = 1;

    public BasicUserManager(Basics module)
    {
        super(module.getCore().getDB(), BasicUser.class, REVISION);
        this.module = module;
        this.core = module.getCore();
        this.initialize();
    }

    public BasicUser getBasicUser(Player player)
    {
        return this.getBasicUser(this.core.getUserManager().getExactUser(player));
    }

    public BasicUser getBasicUser(User user)
    {
        BasicUser model = user.attachOrGet(BasicsAttachment.class, this.module).getBasicUser();
        if (model == null)
        {
            model = this.get(user.getId());
            if (model == null)
            {
                model = new BasicUser(user);
                this.store(model);
            }
            user.get(BasicsAttachment.class).setBasicUser(model);
        }
        return model;
    }

    @Override
    public void update(BasicUser model)
    {
        if (model.muted != null && model.muted.getTime() < System.currentTimeMillis())
        {
            model.muted = null; // remove muted information as it is no longer needed
        }
        super.update(model);
    }
}
