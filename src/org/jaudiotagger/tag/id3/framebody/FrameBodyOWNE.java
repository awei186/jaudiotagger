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

import org.jaudiotagger.tag.datatype.*;
import org.jaudiotagger.tag.InvalidTagException;

import java.io.IOException;


public class FrameBodyOWNE extends AbstractID3v2FrameBody implements ID3v24FrameBody
{
    /**
     * Creates a new FrameBodyOWNE datatype.
     */
    public FrameBodyOWNE()
    {
        //        this.setObject("Text Encoding", new Byte((byte) 0));
        //        this.setObject("Price Paid", "");
        //        this.setObject("Date Of Purchase", "");
        //        this.setObject("Seller", "");
    }

    public FrameBodyOWNE(FrameBodyOWNE body)
    {
        super(body);
    }

    /**
     * Creates a new FrameBodyOWNE datatype.
     *
     * @param textEncoding   DOCUMENT ME!
     * @param pricePaid      DOCUMENT ME!
     * @param dateOfPurchase DOCUMENT ME!
     * @param seller         DOCUMENT ME!
     */
    public FrameBodyOWNE(byte textEncoding, String pricePaid, String dateOfPurchase, String seller)
    {
        this.setObjectValue(DataTypes.OBJ_TEXT_ENCODING, new Byte(textEncoding));
        this.setObjectValue(DataTypes.OBJ_PRICE_PAID, pricePaid);
        this.setObjectValue(DataTypes.OBJ_PURCHASE_DATE, dateOfPurchase);
        this.setObjectValue(DataTypes.OBJ_SELLER_NAME, seller);
    }

    /**
     * Creates a new FrameBodyOWNE datatype.
     *
     * @param file DOCUMENT ME!
     * @throws IOException         DOCUMENT ME!
     * @throws InvalidTagException DOCUMENT ME!
     */
    public FrameBodyOWNE(java.io.RandomAccessFile file, int frameSize)
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
        return "OWNE";
    }

    /**
     * DOCUMENT ME!
     */
    protected void setupObjectList()
    {
        objectList.add(new NumberHashMap(DataTypes.OBJ_TEXT_ENCODING, this, 1));
        objectList.add(new StringNullTerminated(DataTypes.OBJ_PRICE_PAID, this));
        objectList.add(new StringDate(DataTypes.OBJ_PURCHASE_DATE, this));
        objectList.add(new StringSizeTerminated(DataTypes.OBJ_SELLER_NAME, this));
    }
}