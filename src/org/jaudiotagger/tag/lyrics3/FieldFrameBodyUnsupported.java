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
 */
package org.jaudiotagger.tag.lyrics3;

import java.io.IOException;
import java.io.RandomAccessFile;


public class FieldFrameBodyUnsupported extends AbstractLyrics3v2FieldFrameBody
{
    /**
     * DOCUMENT ME!
     */
    private byte[] value = null;

    /**
     * Creates a new FieldBodyUnsupported datatype.
     */
    public FieldFrameBodyUnsupported()
    {
        //        this.value = new byte[0];
    }

    public FieldFrameBodyUnsupported(FieldFrameBodyUnsupported copyObject)
    {
        super(copyObject);
        this.value = (byte[]) copyObject.value.clone();
    }

    /**
     * Creates a new FieldBodyUnsupported datatype.
     *
     * @param value DOCUMENT ME!
     */
    public FieldFrameBodyUnsupported(byte[] value)
    {
        this.value = value;
    }

    /**
     * Creates a new FieldBodyUnsupported datatype.
     *
     * @param file DOCUMENT ME!
     * @throws java.io.IOException DOCUMENT ME!
     */
    public FieldFrameBodyUnsupported(RandomAccessFile file)
        throws java.io.IOException
    {
        this.read(file);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getIdentifier()
    {
        return "ZZZ";
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean isSubsetOf(Object obj)
    {
        if ((obj instanceof FieldFrameBodyUnsupported) == false)
        {
            return false;
        }

        FieldFrameBodyUnsupported object = (FieldFrameBodyUnsupported) obj;

        String subset = new String((byte[]) this.value);
        String superset = new String((byte[]) object.value);

        if (superset.indexOf(subset) < 0)
        {
            return false;
        }

        return super.isSubsetOf(obj);
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean equals(Object obj)
    {
        if ((obj instanceof FieldFrameBodyUnsupported) == false)
        {
            return false;
        }

        FieldFrameBodyUnsupported object = (FieldFrameBodyUnsupported) obj;

        if (java.util.Arrays.equals(this.value, object.value) == false)
        {
            return false;
        }

        return super.equals(obj);
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public void read(RandomAccessFile file)
        throws IOException
    {
        int size;
        byte[] buffer = new byte[5];

        // read the 5 character size
        file.read(buffer, 0, 5);
        size = Integer.parseInt(new String(buffer, 0, 5));

        value = new byte[size];

        // read the SIZE length description
        file.read(value);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString()
    {
        return getIdentifier() + " : " + (new String(value));
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public void write(RandomAccessFile file)
        throws IOException
    {
        int offset = 0;
        String str;
        byte[] buffer = new byte[5];

        str = Integer.toString(value.length);

        for (int i = 0; i < (5 - str.length()); i++)
        {
            buffer[i] = (byte) '0';
        }

        offset += (5 - str.length());

        for (int i = 0; i < str.length(); i++)
        {
            buffer[i + offset] = (byte) str.charAt(i);
        }

        file.write(buffer);

        file.write(value);
    }
}