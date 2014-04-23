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
package de.cubeisland.engine.vote.storage;

import java.sql.Timestamp;

import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.vote.storage.TableVote.TABLE_VOTE;

public class VoteModel extends UpdatableRecordImpl<VoteModel> implements Record3<UInteger, Timestamp, UShort>
{
    public VoteModel()
    {
        super(TABLE_VOTE);
    }

    public VoteModel newVote(User user)
    {
        this.setUserid(user.getEntity().getKey());
        this.setLastvote(new Timestamp(System.currentTimeMillis()));
        this.setVoteamount(UShort.valueOf(1));
        return this;
    }

    public void setUserid(UInteger value) {
        setValue(0, value);
    }

    public UInteger getUserid() {
        return (UInteger) getValue(0);
    }

    public void setLastvote(Timestamp value) {
        setValue(1, value);
    }

    public Timestamp getLastvote() {
        return (Timestamp) getValue(1);
    }

    public void setVoteamount(UShort value) {
        setValue(2, value);
    }

    public UShort getVoteamount() {
        return (UShort) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, Timestamp, UShort> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, Timestamp, UShort> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_VOTE.USERID;
    }

    @Override
    public Field<Timestamp> field2() {
        return TABLE_VOTE.LASTVOTE;
    }

    @Override
    public Field<UShort> field3() {
        return TABLE_VOTE.VOTEAMOUNT;
    }

    @Override
    public UInteger value1() {
        return getUserid();
    }

    @Override
    public Timestamp value2() {
        return getLastvote();
    }

    @Override
    public UShort value3() {
        return getVoteamount();
    }
}
