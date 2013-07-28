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
package de.cubeisland.engine.basics.storage;

import de.cubeisland.engine.core.storage.TwoKeyModel;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.TwoKeyEntity;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Pair;

import static de.cubeisland.engine.core.storage.database.Index.IndexType.FOREIGN_KEY;

@TwoKeyEntity(tableName = "ignorelist", firstPrimaryKey = "key", secondPrimaryKey = "ignore", indices = {
    @Index(value = FOREIGN_KEY, fields = "key", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "ignore", f_table = "user", f_field = "key")
})
public class IgnoreList implements TwoKeyModel<Long, Long>
{

    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long ignore;

    IgnoreList(User user, User ignore)
    {
        this.key = user.key;
        this.ignore = ignore.key;
    }

    @Override
    public Pair<Long, Long> getId()
    {
        return new Pair<>(key, ignore);
    }

    @Override
    public void setId(Pair<Long, Long> id)
    {
        this.key = id.getLeft();
        this.ignore = id.getRight();
    }
}
