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
package de.cubeisland.engine.basics.command.moderation.kit;

import de.cubeisland.engine.core.storage.TwoKeyModel;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.TwoKeyEntity;
import de.cubeisland.engine.core.util.Pair;

import static de.cubeisland.engine.core.storage.database.Index.IndexType.FOREIGN_KEY;

@TwoKeyEntity(tableName = "kitsgiven", firstPrimaryKey = "userId", secondPrimaryKey = "kitName", indices = {
    @Index(value = FOREIGN_KEY, fields = "userId", f_table = "user", f_field = "key")
})
public class KitsGiven implements TwoKeyModel<Long, String>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Attribute(type = AttrType.VARCHAR, length = 50)
    public String kitName;
    @Attribute(type = AttrType.INT)
    public int amount;

    @Override
    public Pair<Long, String> getId()
    {
        return new Pair<>(userId, kitName);
    }

    @Override
    public void setId(Pair<Long, String> id)
    {
        this.userId = id.getLeft();
        this.kitName = id.getRight();
    }
}
