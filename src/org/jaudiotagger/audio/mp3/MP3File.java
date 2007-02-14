/**
 *  @author : Paul Taylor
 *  @author : Eric Farng
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
 */
package org.jaudiotagger.audio.mp3;

import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.lyrics3.AbstractLyrics3;
import org.jaudiotagger.tag.lyrics3.Lyrics3v2;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.virtual.VirtualMetaDataContainer;
import org.jaudiotagger.logging.*;
import org.jaudiotagger.audio.ReadOnlyFileException;
import org.jaudiotagger.audio.InvalidAudioFrameException;

import java.io.*;

import java.util.logging.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

/**
 * This class represets a physical MP3 File
 */
public class MP3File extends org.jaudiotagger.audio.AbstractAudioFile
{
    protected static AbstractTagDisplayFormatter tagFormatter;

    /**
     * Logger Object
     */
    public static Logger logger = LogFormatter.getLogger();

    /**
     * the ID3v2 tag that this file contains.
     */
    private AbstractID3v2Tag id3v2tag = null;

    /**
     * Representation of the idv2 tag as a idv24 tag
     */
    private ID3v24Tag id3v2Asv24tag = null;

    /**
     * The Lyrics3 tag that this file contains.
     */
    private AbstractLyrics3 lyrics3tag = null;


    /**
     * The ID3v1 tag that this file contains.
     */
    private ID3v1Tag id3v1tag = null;

    /**
     * Creates a new empty MP3File datatype that is not associated with a
     * specific file.
     */
    public MP3File()
    {
    }

    /**
     * Creates a new MP3File datatype and parse the tag from the given filename.
     *
     * @param filename MP3 file
     * @throws IOException  on any I/O error
     * @throws TagException on any exception generated by this library.
     */
    public MP3File(String filename)
        throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        this(new File(filename));
    }


    /* Load ID3V1tag if exists */
    public static final int LOAD_IDV1TAG = 2;

    /* Load ID3V2tag if exists */
    public static final int LOAD_IDV2TAG = 4;

    /**
     * This option is currently ignored
     */
    public static final int LOAD_LYRICS3 = 8;

    public static final int LOAD_ALL = LOAD_IDV1TAG | LOAD_IDV2TAG | LOAD_LYRICS3;

    /**
     * Creates a new MP3File datatype and parse the tag from the given file
     * Object, files must be writable to use this constructor.
     *
     * @param file        MP3 file
     * @param loadOptions decide what tags to load
     * @throws IOException  on any I/O error
     * @throws TagException on any exception generated by this library.
     */
    public MP3File(File file, int loadOptions)
        throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException

    {
        this(file, loadOptions, false);
    }

    /**
     * Creates a new MP3File datatype and parse the tag from the given file
     * Object, files can be onpened read only if required.
     *
     * @param file        MP3 file
     * @param loadOptions decide what tags to load
     * @param readOnly    causes the files to be opened readonly
     * @throws IOException  on any I/O error
     * @throws TagException on any exception generated by this library.
     */
    public MP3File(File file, int loadOptions, boolean readOnly)
        throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        this.file = file;
        RandomAccessFile newFile;
        logger.info("Reading file:" + "path" + file.getPath() + ":abs:" + file.getAbsolutePath());
        if (file.exists() == false)
        {
            logger.severe("Unable to find:" + file.getPath());
            throw new FileNotFoundException("Unable to find:" + file.getPath());
        }

        /* Unless opened as readonly the file must be writable */
        if (readOnly)
        {
            newFile = new RandomAccessFile(file, "r");
        }
        else
        {
            if (file.canWrite() == false)
            {
                logger.severe("Unable to write:" + file.getPath());
                throw new ReadOnlyFileException("Unable to write to:" + file.getPath());
            }
            newFile = new RandomAccessFile(file, "rw");
        }

        //If exception with MPEG then we should give up no point continuing
        try
        {
            audioHeader = new MP3AudioHeader(file);
        }
        catch (InvalidAudioFrameException iafe)
        {
            throw iafe;
        }


        if ((loadOptions & LOAD_IDV1TAG) != 0)
        {
            logger.finer("Attempting to read id3v1tags");
            try
            {
                id3v1tag = new ID3v11Tag(newFile);
            }
            catch (TagNotFoundException ex)
            {
                logger.info("No ids3v11 tag found");
            }

            try
            {
                if (id3v1tag == null)
                {
                    id3v1tag = new ID3v1Tag(newFile);
                }
            }
            catch (TagNotFoundException ex)
            {
                logger.info("No id3v1 tag found");
            }
        }

        //We know where the Actual Audio starts so load all the file from start to that point into
        //a buffer then we can read the IDv2 information without needing any more file I/O
        int startByte = (int) ((MP3AudioHeader) audioHeader).getMp3StartByte();
        if (startByte >= AbstractID3v2Tag.TAG_HEADER_LENGTH)
        {
            logger.finer("Attempting to read id3v2tags");
            FileInputStream fis = null;
            FileChannel fc = null;
            ByteBuffer bb = null;
            try
            {
                fis = new FileInputStream(file);
                fc = fis.getChannel();
                //Read into Byte Buffer
                bb = ByteBuffer.allocate(startByte);
                fc.read(bb);
            }
            finally
            {
                if (fc != null)
                {
                    fc.close();
                }

                if (fis != null)
                {
                    fis.close();
                }
            }

            bb.rewind();

            if ((loadOptions & LOAD_IDV2TAG) != 0)
            {
                logger.info("Attempting to read id3v2tags");
                try
                {
                    this.setID3v2Tag(new ID3v24Tag(bb));
                }
                catch (TagNotFoundException ex)
                {
                    logger.info("No id3v24 tag found");
                }

                try
                {
                    if (id3v2tag == null)
                    {
                        this.setID3v2Tag(new ID3v23Tag(bb));
                    }
                }
                catch (TagNotFoundException ex)
                {
                    logger.info("No id3v23 tag found");
                }

                try
                {
                    if (id3v2tag == null)
                    {
                        this.setID3v2Tag(new ID3v22Tag(bb));
                    }
                }
                catch (TagNotFoundException ex)
                {
                    logger.info("No id3v22 tag found");
                }
            }
        }
        else
        {
              logger.info("Not enough room for valid id3v2 tag:"+startByte);
        }
        /* TODO
        if ((loadOptions & LOAD_LYRICS3) != 0)
        {
            try
            {
                lyrics3tag = new Lyrics3v2(newFile);
            }
            catch (TagNotFoundException ex)
            {
            }
            try
            {
                if (lyrics3tag == null)
                {
                    lyrics3tag = new Lyrics3v1(newFile);
                }
            }
            catch (TagNotFoundException ex)
            {
            }
        }
        */

        //Create Virtual tag from the ID3v24tag
        if (this.getID3v2TagAsv24() != null)
        {
            this.metaData = new VirtualMetaDataContainer((ID3v24Tag) this.getID3v2TagAsv24());
        }

        newFile.close();


    }

    /**
     * Used by tags when writing to calculate the location of the music file
     *
     * @return the location within the file that the audio starts
     */
    public long getMP3StartByte(File file) throws InvalidAudioFrameException, IOException
    {
        try
        {
            MP3AudioHeader audioHeader = new MP3AudioHeader(file);
            return audioHeader.getMp3StartByte();
        }
        catch (InvalidAudioFrameException iafe)
        {
            throw iafe;
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
    }

    /**
     * Extracts the raw ID3v2 tag data into a file.
     *
     * This provides access to the raw data before manipulation, the data is written from the start of the file
     * to the start of the Audio Data. This is primarily useful for manipulating corrupted tags that are not
     * (fully) loaded using the standard methods. 
     *
     * @param outputFile to write the data to
     * @return
     * @throws TagNotFoundException
     * @throws IOException
     */
    public File extractID3v2TagDataIntoFile(File outputFile) throws TagNotFoundException,IOException
    {
        int startByte = (int) ((MP3AudioHeader) audioHeader).getMp3StartByte();
        if (startByte >= 0)
        {

            //Read byte into buffer
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(startByte);
            fc.read(bb);

            //Write bytes to outputFile
            FileOutputStream out = new FileOutputStream(outputFile);
            out.write(bb.array());
            out.close();
            fc.close();
            fis.close();
            return outputFile;
        }
        throw new TagNotFoundException("There is no ID3v2Tag data in this file");
    }

    /**
     * Return audio header
     */
    public MP3AudioHeader getMP3AudioHeader()
    {
        return (MP3AudioHeader) getAudioHeader();
    }

    /**
     * Returns true if this datatype contains an <code>Id3v1</code> tag
     *
     * @return true if this datatype contains an <code>Id3v1</code> tag
     */
    public boolean hasID3v1Tag
        ()
    {
        return (id3v1tag != null);
    }

    /**
     * Returns true if this datatype contains an <code>Id3v2</code> tag
     *
     * @return true if this datatype contains an <code>Id3v2</code> tag
     */
    public boolean hasID3v2Tag
        ()
    {
        return (id3v2tag != null);
    }

    /**
     * Returns true if this datatype contains a <code>Lyrics3</code> tag
     * TODO disabled until Lyrics3 fixed
     * @return true if this datatype contains a <code>Lyrics3</code> tag
     */
    /*
    public boolean hasLyrics3Tag()
    {
        return (lyrics3tag != null);
    }
    */

    /**
     * Creates a new MP3File datatype and parse the tag from the given file
     * Object.
     *
     * @param file MP3 file
     * @throws IOException  on any I/O error
     * @throws TagException on any exception generated by this library.
     */
    public MP3File(File
        file)
        throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        this(file, LOAD_ALL);
    }

    /**
     * Sets the v1(_1)tag to the tag provided as an argument.
     *
     * @param id3v1tag
     */
    public void setID3v1Tag
        (ID3v1Tag
            id3v1tag)
    {
        logger.info("setting tagv1:v1 tag");
        this.id3v1tag = id3v1tag;
    }

    /**
     * Sets the <code>ID3v1</code> tag for this datatype. A new
     * <code>ID3v1_1</code> datatype is created from the argument and then used
     * here.
     *
     * @param mp3tag Any MP3Tag datatype can be used and will be converted into a
     *               new ID3v1_1 datatype.
     */
    public void setID3v1Tag
        (AbstractTag
            mp3tag)
    {
        logger.info("setting tagv1:abstract");
        id3v1tag = new ID3v11Tag(mp3tag);
    }

    /**
     * Returns the <code>ID3v1</code> tag for this datatype.
     *
     * @return the <code>ID3v1</code> tag for this datatype
     */
    public ID3v1Tag getID3v1Tag
        ()
    {
        return id3v1tag;
    }

    /**
     * Sets the <code>ID3v2</code> tag for this datatype. A new
     * <code>ID3v2_4</code> datatype is created from the argument and then used
     * here.
     *
     * @param mp3tag Any MP3Tag datatype can be used and will be converted into a
     *               new ID3v2_4 datatype.
     */
    public void setID3v2Tag
        (AbstractTag
            mp3tag)
    {
        id3v2tag = new ID3v24Tag(mp3tag);

    }

    /**
     * Sets the v2 tag to the v2 tag provided as an argument.
     * Also store a v24 version of tag as v24 is the interface to be used
     * when talking with client applications.
     *
     * @param id3v2tag
     */
    public void setID3v2Tag
        (AbstractID3v2Tag
            id3v2tag)
    {
        this.id3v2tag = id3v2tag;
        if (id3v2tag instanceof ID3v24Tag)
        {
            this.id3v2Asv24tag = (ID3v24Tag) this.id3v2tag;
        }
        else
        {
            this.id3v2Asv24tag = new ID3v24Tag(id3v2tag);
        }
    }

    /**
     * Set v2 tag ,dont need to set v24 tag because saving
     *
     * @TODO temp its rather messy
     */
    public void setID3v2TagOnly
        (AbstractID3v2Tag
            id3v2tag)
    {
        this.id3v2tag = id3v2tag;
        this.id3v2Asv24tag = null;
    }

    /**
     * Returns the <code>ID3v2</code> tag for this datatype.
     *
     * @return the <code>ID3v2</code> tag for this datatype
     */
    public AbstractID3v2Tag getID3v2Tag
        ()
    {
        return id3v2tag;
    }

    /**
     * Returns a representation of tag as v24
     */
    public AbstractID3v2Tag getID3v2TagAsv24
        ()
    {
        return id3v2Asv24tag;
    }

    /**
     * Sets the <code>Lyrics3</code> tag for this datatype. A new
     * <code>Lyrics3v2</code> datatype is created from the argument and then
     * used here.
     *
     * @param mp3tag Any MP3Tag datatype can be used and will be converted into a
     *               new Lyrics3v2 datatype.
     */
    /*
    public void setLyrics3Tag(AbstractTag mp3tag)
    {
        lyrics3tag = new Lyrics3v2(mp3tag);
    }
    */

    /**
     *
     *
     * @param lyrics3tag
     */
    /*
    public void setLyrics3Tag(AbstractLyrics3 lyrics3tag)
    {
        this.lyrics3tag = lyrics3tag;
    }
    */

    /**
     * Returns the <code>ID3v1</code> tag for this datatype.
     *
     * @return the <code>ID3v1</code> tag for this datatype
     */
    /*
    public AbstractLyrics3 getLyrics3Tag()
    {
        return lyrics3tag;
    }
    */

    /**
     * Remove tag from file
     *
     * @param mp3tag
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void delete
        (AbstractTag
            mp3tag)
        throws FileNotFoundException, IOException
    {
        mp3tag.delete(new RandomAccessFile(this.file, "rw"));
    }

    /**
     * Saves the tags in this datatype to the file referred to by this datatype.
     *
     * @throws IOException  on any I/O error
     * @throws TagException on any exception generated by this library.
     */
    public void save
        ()
        throws IOException, TagException
    {
        save(this.file);
    }

    /**
     * Saves the tags in this datatype to the file argument. It will be saved as
     * TagConstants.MP3_FILE_SAVE_WRITE
     *
     * @param file file to save the this datatype's tags to
     * @throws FileNotFoundException if unable to find file
     * @throws IOException           on any I/O error
     * @throws TagException          on any exception generated by this library.
     */
    public void save
        (File
            file)
        throws FileNotFoundException, IOException, TagException
    {
        logger.info("Saving  : " + file.getAbsolutePath());
        RandomAccessFile rfile = null;
        try
        {
            //ID3v2 Tag
            if (TagOptionSingleton.getInstance().isId3v2Save())
            {
                if (id3v2tag == null)
                {
                    rfile = new RandomAccessFile(file, "rw");
                    (new ID3v24Tag()).delete(rfile);
                    rfile.close();
                }
                else
                {
                    id3v2tag.write(file, ((MP3AudioHeader) this.getAudioHeader()).getMp3StartByte());
                }
            }
            rfile = new RandomAccessFile(file, "rw");

            //Lyrics 3 Tag
            if (TagOptionSingleton.getInstance().isLyrics3Save())
            {
                if (lyrics3tag != null)
                {
                    lyrics3tag.write(rfile);
                }
            }
            //ID3v1 tag
            if (TagOptionSingleton.getInstance().isId3v1Save())
            {
                logger.info("saving v1");
                if (id3v1tag == null)
                {
                    logger.info("deleting v1");
                    (new ID3v1Tag()).delete(rfile);
                }
                else
                {
                    logger.info("saving v1 still");
                    id3v1tag.write(rfile);
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            logger.log(Level.SEVERE, "Problem writing tags to file,Unexpected Exception" + file.getAbsolutePath(), ex);
            throw ex;
        }
        finally
        {
            if (rfile != null)
            {
                rfile.close();
            }
        }
    }

    /**
     * Displays MP3File Structure
     */
    public String displayStructureAsXML
        ()
    {
        createXMLStructureFormatter();
        this.tagFormatter.openHeadingElement("file", this.getFile().getAbsolutePath());
        if (this.getID3v1Tag() != null)
        {
            this.getID3v1Tag().createStructure();
        }
        if (this.getID3v2Tag() != null)
        {
            this.getID3v2Tag().createStructure();
        }
        this.tagFormatter.closeHeadingElement("file");
        return tagFormatter.toString();
    }

    /**
     * Displays MP3File Structure
     */
    public String displayStructureAsPlainText
        ()
    {
        createPlainTextStructureFormatter();
        this.tagFormatter.openHeadingElement("file", this.getFile().getAbsolutePath());
        if (this.getID3v1Tag() != null)
        {
            this.getID3v1Tag().createStructure();
        }
        if (this.getID3v2Tag() != null)
        {
            this.getID3v2Tag().createStructure();
        }
        this.tagFormatter.closeHeadingElement("file");
        return tagFormatter.toString();
    }

    private static void createXMLStructureFormatter
        ()
    {
        tagFormatter = XMLTagDisplayFormatter.getInstanceOf();
    }

    private static void createPlainTextStructureFormatter
        ()
    {
        tagFormatter = PlainTextTagDisplayFormatter.getInstanceOf();
    }

    public static AbstractTagDisplayFormatter getStructureFormatter
        ()
    {
        return tagFormatter;
    }
    //For writing tag to screen


}

