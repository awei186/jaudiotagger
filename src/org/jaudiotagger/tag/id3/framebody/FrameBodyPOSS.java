/**
 *  Amended @author : Paul Taylor
 *  Initial @author : Eric Farng
 *
 *  Version @version:$Id$
 *
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Description:
 *
 */
package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.datatype.NumberHashMap;
import org.jaudiotagger.tag.datatype.NumberVariableLength;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.InvalidTagException;

import java.io.IOException;


public class FrameBodyPOSS extends AbstractID3v2FrameBody implements ID3v24FrameBody
{
    /**
     * Creates a new FrameBodyPOSS datatype.
     */
    public FrameBodyPOSS()
    {
        //        this.setObject(ObjectNumberHashMap.OBJ_TIME_STAMP_FORMAT, new Byte((byte) 0));
        //        this.setObject("Position", new Long(0));
    }

    public FrameBodyPOSS(FrameBodyPOSS body)
    {
        super(body);
    }

    /**
     * Creates a new FrameBodyPOSS datatype.
     *
     * @param timeStampFormat DOCUMENT ME!
     * @param position        DOCUMENT ME!
     */
    public FrameBodyPOSS(byte timeStampFormat, long position)
    {
        this.setObjectValue(DataTypes.OBJ_TIME_STAMP_FORMAT, new Byte(timeStampFormat));
        this.setObjectValue(DataTypes.OBJ_POSITION, new Long(position));
    }

    /**
     * Creates a new FrameBodyPOSS datatype.
     *
     * @param file DOCUMENT ME!
     * @throws IOException         DOCUMENT ME!
     * @throws InvalidTagException DOCUMENT ME!
     */
    public FrameBodyPOSS(java.io.RandomAccessFile file, int frameSize)
        throws IOException, InvalidTagException
    {
        super(file, frameSize);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getIdentifier()
    {
        return "POSS";
    }

    /**
     * DOCUMENT ME!
     */
    protected void setupObjectList()
    {
        objectList.add(new NumberHashMap(DataTypes.OBJ_TIME_STAMP_FORMAT, this, 1));
        objectList.add(new NumberVariableLength(DataTypes.OBJ_POSITION, this, 1));
    }
}