/* Inflater.java - Decompress a data stream
   Copyright (C) 1999, 2000, 2001, 2003  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.util.zip;

import org.apidesign.bck2brwsr.emul.lang.System;

/**
 * This class provides support for general purpose decompression using the
 * popular ZLIB compression library. The ZLIB compression library was
 * initially developed as part of the PNG graphics standard and is not
 * protected by patents. It is fully described in the specifications at
 * the <a href="package-summary.html#package_description">java.util.zip
 * package description</a>.
 *
 * <p>The following code fragment demonstrates a trivial compression
 * and decompression of a string using <tt>Deflater</tt> and
 * <tt>Inflater</tt>.
 *
 * <blockquote><pre>
 * try {
 *     // Encode a String into bytes
 *     String inputString = "blahblahblah\u20AC\u20AC";
 *     byte[] input = inputString.getBytes("UTF-8");
 *
 *     // Compress the bytes
 *     byte[] output = new byte[100];
 *     Deflater compresser = new Deflater();
 *     compresser.setInput(input);
 *     compresser.finish();
 *     int compressedDataLength = compresser.deflate(output);
 *
 *     // Decompress the bytes
 *     Inflater decompresser = new Inflater();
 *     decompresser.setInput(output, 0, compressedDataLength);
 *     byte[] result = new byte[100];
 *     int resultLength = decompresser.inflate(result);
 *     decompresser.end();
 *
 *     // Decode the bytes into a String
 *     String outputString = new String(result, 0, resultLength, "UTF-8");
 * } catch(java.io.UnsupportedEncodingException ex) {
 *     // handle
 * } catch (java.util.zip.DataFormatException ex) {
 *     // handle
 * }
 * </pre></blockquote>
 *
 * @see         Deflater
 * @author      David Connelly
 *
 */

/* Written using on-line Java Platform 1.2 API Specification
 * and JCL book.
 * Believed complete and correct.
 */

/**
 * Inflater is used to decompress data that has been compressed according 
 * to the "deflate" standard described in rfc1950.
 *
 * The usage is as following.  First you have to set some input with
 * <code>setInput()</code>, then inflate() it.  If inflate doesn't
 * inflate any bytes there may be three reasons:
 * <ul>
 * <li>needsInput() returns true because the input buffer is empty.
 * You have to provide more input with <code>setInput()</code>.  
 * NOTE: needsInput() also returns true when, the stream is finished.
 * </li>
 * <li>needsDictionary() returns true, you have to provide a preset 
 *     dictionary with <code>setDictionary()</code>.</li>
 * <li>finished() returns true, the inflater has finished.</li>
 * </ul>
 * Once the first output byte is produced, a dictionary will not be
 * needed at a later stage.
 *
 * @author John Leuner, Jochen Hoenicke
 * @author Tom Tromey
 * @date May 17, 1999
 * @since JDK 1.1
 */
public class Inflater
{
  /* Copy lengths for literal codes 257..285 */
  private static final int CPLENS[] = 
  { 
    3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
    35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258
  };
  
  /* Extra bits for literal codes 257..285 */  
  private static final int CPLEXT[] = 
  { 
    0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
    3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
  };

  /* Copy offsets for distance codes 0..29 */
  private static final int CPDIST[] = {
    1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
    257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145,
    8193, 12289, 16385, 24577
  };
  
  /* Extra bits for distance codes */
  private static final int CPDEXT[] = {
    0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
    7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 
    12, 12, 13, 13
  };

  /* This are the state in which the inflater can be.  */
  private static final int DECODE_HEADER           = 0;
  private static final int DECODE_DICT             = 1;
  private static final int DECODE_BLOCKS           = 2;
  private static final int DECODE_STORED_LEN1      = 3;
  private static final int DECODE_STORED_LEN2      = 4;
  private static final int DECODE_STORED           = 5;
  private static final int DECODE_DYN_HEADER       = 6;
  private static final int DECODE_HUFFMAN          = 7;
  private static final int DECODE_HUFFMAN_LENBITS  = 8;
  private static final int DECODE_HUFFMAN_DIST     = 9;
  private static final int DECODE_HUFFMAN_DISTBITS = 10;
  private static final int DECODE_CHKSUM           = 11;
  private static final int FINISHED                = 12;

  /** This variable contains the current state. */
  private int mode;

  /**
   * The adler checksum of the dictionary or of the decompressed
   * stream, as it is written in the header resp. footer of the
   * compressed stream.  <br>
   *
   * Only valid if mode is DECODE_DICT or DECODE_CHKSUM.
   */
  private int readAdler;
  /** 
   * The number of bits needed to complete the current state.  This
   * is valid, if mode is DECODE_DICT, DECODE_CHKSUM,
   * DECODE_HUFFMAN_LENBITS or DECODE_HUFFMAN_DISTBITS.  
   */
  private int neededBits;
  private int repLength, repDist;
  private int uncomprLen;
  /**
   * True, if the last block flag was set in the last block of the
   * inflated stream.  This means that the stream ends after the
   * current block.  
   */
  private boolean isLastBlock;

  /**
   * The total number of inflated bytes.
   */
  private long totalOut;
  /**
   * The total number of bytes set with setInput().  This is not the
   * value returned by getTotalIn(), since this also includes the 
   * unprocessed input.
   */
  private long totalIn;
  /**
   * This variable stores the nowrap flag that was given to the constructor.
   * True means, that the inflated stream doesn't contain a header nor the
   * checksum in the footer.
   */
  private boolean nowrap;

  private StreamManipulator input;
  private OutputWindow outputWindow;
  private InflaterDynHeader dynHeader;
  private InflaterHuffmanTree litlenTree, distTree;
  private Adler32 adler;

  /**
   * Creates a new inflater.
   */
  public Inflater ()
  {
    this (false);
  }

  /**
   * Creates a new inflater.
   * @param nowrap true if no header and checksum field appears in the
   * stream.  This is used for GZIPed input.  For compatibility with
   * Sun JDK you should provide one byte of input more than needed in
   * this case.
   */
  public Inflater (boolean nowrap)
  {
    this.nowrap = nowrap;
    this.adler = new Adler32();
    input = new StreamManipulator();
    outputWindow = new OutputWindow();
    mode = nowrap ? DECODE_BLOCKS : DECODE_HEADER;
  }

  /**
   * Finalizes this object.
   */
  protected void finalize ()
  {
    /* Exists only for compatibility */
  }

  /**
   * Frees all objects allocated by the inflater.  There's no reason
   * to call this, since you can just rely on garbage collection (even
   * for the Sun implementation).  Exists only for compatibility
   * with Sun's JDK, where the compressor allocates native memory.
   * If you call any method (even reset) afterwards the behaviour is
   * <i>undefined</i>.  
   * @deprecated Just clear all references to inflater instead.
   */
  public void end ()
  {
    outputWindow = null;
    input = null;
    dynHeader = null;
    litlenTree = null;
    distTree = null;
    adler = null;
  }

  /**
   * Returns true, if the inflater has finished.  This means, that no
   * input is needed and no output can be produced.
   */
  public boolean finished() 
  {
    return mode == FINISHED && outputWindow.getAvailable() == 0;
  }

  /**
   * Gets the adler checksum.  This is either the checksum of all
   * uncompressed bytes returned by inflate(), or if needsDictionary()
   * returns true (and thus no output was yet produced) this is the
   * adler checksum of the expected dictionary.
   * @returns the adler checksum.
   */
  public int getAdler()
  {
    return needsDictionary() ? readAdler : (int) adler.getValue();
  }
  
  /**
   * Gets the number of unprocessed input.  Useful, if the end of the
   * stream is reached and you want to further process the bytes after
   * the deflate stream.  
   * @return the number of bytes of the input which were not processed.
   */
  public int getRemaining()
  {
    return input.getAvailableBytes();
  }
  
  /**
   * Gets the total number of processed compressed input bytes.
   * @return the total number of bytes of processed input bytes.
   */
  public int getTotalIn()
  {
    return (int)getBytesRead();
  }
  
  /**
   * Gets the total number of output bytes returned by inflate().
   * @return the total number of output bytes.
   */
  public int getTotalOut()
  {
    return (int)totalOut;
  }
  
  public long getBytesWritten() {
     return totalOut;
  }

  public long getBytesRead() {
    return totalIn - getRemaining();
  }
  

  /**
   * Inflates the compressed stream to the output buffer.  If this
   * returns 0, you should check, whether needsDictionary(),
   * needsInput() or finished() returns true, to determine why no 
   * further output is produced.
   * @param buffer the output buffer.
   * @return the number of bytes written to the buffer, 0 if no further
   * output can be produced.  
   * @exception DataFormatException if deflated stream is invalid.
   * @exception IllegalArgumentException if buf has length 0.
   */
  public int inflate (byte[] buf) throws DataFormatException
  {
    return inflate (buf, 0, buf.length);
  }

  /**
   * Inflates the compressed stream to the output buffer.  If this
   * returns 0, you should check, whether needsDictionary(),
   * needsInput() or finished() returns true, to determine why no 
   * further output is produced.
   * @param buffer the output buffer.
   * @param off the offset into buffer where the output should start.
   * @param len the maximum length of the output.
   * @return the number of bytes written to the buffer, 0 if no further
   * output can be produced.  
   * @exception DataFormatException if deflated stream is invalid.
   * @exception IndexOutOfBoundsException if the off and/or len are wrong.
   */
  public int inflate (byte[] buf, int off, int len) throws DataFormatException
  {
    /* Special case: len may be zero */
    if (len == 0)
      return 0;
    /* Check for correct buff, off, len triple */
    if (0 > off || off > off + len || off + len > buf.length)
      throw new ArrayIndexOutOfBoundsException();
    int count = 0;
    int more;
    do
      {
	if (mode != DECODE_CHKSUM)
	  {
	    /* Don't give away any output, if we are waiting for the
	     * checksum in the input stream.
	     *
	     * With this trick we have always:
	     *   needsInput() and not finished() 
	     *   implies more output can be produced.  
	     */
	    more = outputWindow.copyOutput(buf, off, len);
	    adler.update(buf, off, more);
	    off += more;
	    count += more;
	    totalOut += more;
	    len -= more;
	    if (len == 0)
	      return count;
	  }
      }
    while (decode() || (outputWindow.getAvailable() > 0
			&& mode != DECODE_CHKSUM));
    return count;
  }

  /**
   * Returns true, if a preset dictionary is needed to inflate the input.
   */
  public boolean needsDictionary ()
  {
    return mode == DECODE_DICT && neededBits == 0;
  }

  /**
   * Returns true, if the input buffer is empty.
   * You should then call setInput(). <br>
   *
   * <em>NOTE</em>: This method also returns true when the stream is finished.
   */
  public boolean needsInput () 
  {
    return input.needsInput ();
  }

  /**
   * Resets the inflater so that a new stream can be decompressed.  All
   * pending input and output will be discarded.
   */
  public void reset ()
  {
    mode = nowrap ? DECODE_BLOCKS : DECODE_HEADER;
    totalIn = totalOut = 0;
    input.reset();
    outputWindow.reset();
    dynHeader = null;
    litlenTree = null;
    distTree = null;
    isLastBlock = false;
    adler.reset();
  }

  /**
   * Sets the preset dictionary.  This should only be called, if
   * needsDictionary() returns true and it should set the same
   * dictionary, that was used for deflating.  The getAdler()
   * function returns the checksum of the dictionary needed.
   * @param buffer the dictionary.
   * @exception IllegalStateException if no dictionary is needed.
   * @exception IllegalArgumentException if the dictionary checksum is
   * wrong.  
   */
  public void setDictionary (byte[] buffer)
  {
    setDictionary(buffer, 0, buffer.length);
  }

  /**
   * Sets the preset dictionary.  This should only be called, if
   * needsDictionary() returns true and it should set the same
   * dictionary, that was used for deflating.  The getAdler()
   * function returns the checksum of the dictionary needed.
   * @param buffer the dictionary.
   * @param off the offset into buffer where the dictionary starts.
   * @param len the length of the dictionary.
   * @exception IllegalStateException if no dictionary is needed.
   * @exception IllegalArgumentException if the dictionary checksum is
   * wrong.  
   * @exception IndexOutOfBoundsException if the off and/or len are wrong.
   */
  public void setDictionary (byte[] buffer, int off, int len)
  {
    if (!needsDictionary())
      throw new IllegalStateException();

    adler.update(buffer, off, len);
    if ((int) adler.getValue() != readAdler)
      throw new IllegalArgumentException("Wrong adler checksum");
    adler.reset();
    outputWindow.copyDict(buffer, off, len);
    mode = DECODE_BLOCKS;
  }

  /**
   * Sets the input.  This should only be called, if needsInput()
   * returns true.
   * @param buffer the input.
   * @exception IllegalStateException if no input is needed.
   */
  public void setInput (byte[] buf) 
  {
    setInput (buf, 0, buf.length);
  }

  /**
   * Sets the input.  This should only be called, if needsInput()
   * returns true.
   * @param buffer the input.
   * @param off the offset into buffer where the input starts.
   * @param len the length of the input.  
   * @exception IllegalStateException if no input is needed.
   * @exception IndexOutOfBoundsException if the off and/or len are wrong.
   */
  public void setInput (byte[] buf, int off, int len) 
  {
    input.setInput (buf, off, len);
    totalIn += len;
  }
  private static final int DEFLATED = 8;
  /**
   * Decodes the deflate header.
   * @return false if more input is needed. 
   * @exception DataFormatException if header is invalid.
   */
  private boolean decodeHeader () throws DataFormatException
  {
    int header = input.peekBits(16);
    if (header < 0)
      return false;
    input.dropBits(16);
    
    /* The header is written in "wrong" byte order */
    header = ((header << 8) | (header >> 8)) & 0xffff;
    if (header % 31 != 0)
      throw new DataFormatException("Header checksum illegal");
    
    if ((header & 0x0f00) != (DEFLATED << 8))
      throw new DataFormatException("Compression Method unknown");

    /* Maximum size of the backwards window in bits. 
     * We currently ignore this, but we could use it to make the
     * inflater window more space efficient. On the other hand the
     * full window (15 bits) is needed most times, anyway.
     int max_wbits = ((header & 0x7000) >> 12) + 8;
     */
    
    if ((header & 0x0020) == 0) // Dictionary flag?
      {
	mode = DECODE_BLOCKS;
      }
    else
      {
	mode = DECODE_DICT;
	neededBits = 32;      
      }
    return true;
  }
   
  /**
   * Decodes the dictionary checksum after the deflate header.
   * @return false if more input is needed. 
   */
  private boolean decodeDict ()
  {
    while (neededBits > 0)
      {
	int dictByte = input.peekBits(8);
	if (dictByte < 0)
	  return false;
	input.dropBits(8);
	readAdler = (readAdler << 8) | dictByte;
	neededBits -= 8;
      }
    return false;
  }

  /**
   * Decodes the huffman encoded symbols in the input stream.
   * @return false if more input is needed, true if output window is
   * full or the current block ends.
   * @exception DataFormatException if deflated stream is invalid.  
   */
  private boolean decodeHuffman () throws DataFormatException
  {
    int free = outputWindow.getFreeSpace();
    while (free >= 258)
      {
	int symbol;
	switch (mode)
	  {
	  case DECODE_HUFFMAN:
	    /* This is the inner loop so it is optimized a bit */
	    while (((symbol = litlenTree.getSymbol(input)) & ~0xff) == 0)
	      {
		outputWindow.write(symbol);
		if (--free < 258)
		  return true;
	      } 
	    if (symbol < 257)
	      {
		if (symbol < 0)
		  return false;
		else
		  {
		    /* symbol == 256: end of block */
		    distTree = null;
		    litlenTree = null;
		    mode = DECODE_BLOCKS;
		    return true;
		  }
	      }
		
	    try
	      {
		repLength = CPLENS[symbol - 257];
		neededBits = CPLEXT[symbol - 257];
	      }
	    catch (ArrayIndexOutOfBoundsException ex)
	      {
		throw new DataFormatException("Illegal rep length code");
	      }
	    /* fall through */
	  case DECODE_HUFFMAN_LENBITS:
	    if (neededBits > 0)
	      {
		mode = DECODE_HUFFMAN_LENBITS;
		int i = input.peekBits(neededBits);
		if (i < 0)
		  return false;
		input.dropBits(neededBits);
		repLength += i;
	      }
	    mode = DECODE_HUFFMAN_DIST;
	    /* fall through */
	  case DECODE_HUFFMAN_DIST:
	    symbol = distTree.getSymbol(input);
	    if (symbol < 0)
	      return false;
	    try 
	      {
		repDist = CPDIST[symbol];
		neededBits = CPDEXT[symbol];
	      }
	    catch (ArrayIndexOutOfBoundsException ex)
	      {
		throw new DataFormatException("Illegal rep dist code");
	      }
	    /* fall through */
	  case DECODE_HUFFMAN_DISTBITS:
	    if (neededBits > 0)
	      {
		mode = DECODE_HUFFMAN_DISTBITS;
		int i = input.peekBits(neededBits);
		if (i < 0)
		  return false;
		input.dropBits(neededBits);
		repDist += i;
	      }
	    outputWindow.repeat(repLength, repDist);
	    free -= repLength;
	    mode = DECODE_HUFFMAN;
	    break;
	  default:
	    throw new IllegalStateException();
	  }
      }
    return true;
  }

  /**
   * Decodes the adler checksum after the deflate stream.
   * @return false if more input is needed. 
   * @exception DataFormatException if checksum doesn't match.
   */
  private boolean decodeChksum () throws DataFormatException
  {
    while (neededBits > 0)
      {
	int chkByte = input.peekBits(8);
	if (chkByte < 0)
	  return false;
	input.dropBits(8);
	readAdler = (readAdler << 8) | chkByte;
	neededBits -= 8;
      }
    if ((int) adler.getValue() != readAdler)
      throw new DataFormatException("Adler chksum doesn't match: "
				    +Integer.toHexString((int)adler.getValue())
				    +" vs. "+Integer.toHexString(readAdler));
    mode = FINISHED;
    return false;
  }

  /**
   * Decodes the deflated stream.
   * @return false if more input is needed, or if finished. 
   * @exception DataFormatException if deflated stream is invalid.
   */
  private boolean decode () throws DataFormatException
  {
    switch (mode) 
      {
      case DECODE_HEADER:
	return decodeHeader();
      case DECODE_DICT:
	return decodeDict();
      case DECODE_CHKSUM:
	return decodeChksum();

      case DECODE_BLOCKS:
	if (isLastBlock)
	  {
	    if (nowrap)
	      {
		mode = FINISHED;
		return false;
	      }
	    else
	      {
		input.skipToByteBoundary();
		neededBits = 32;
		mode = DECODE_CHKSUM;
		return true;
	      }
	  }

	int type = input.peekBits(3);
	if (type < 0)
	  return false;
	input.dropBits(3);

	if ((type & 1) != 0)
	  isLastBlock = true;
	switch (type >> 1)
	  {
	  case DeflaterConstants.STORED_BLOCK:
	    input.skipToByteBoundary();
	    mode = DECODE_STORED_LEN1;
	    break;
	  case DeflaterConstants.STATIC_TREES:
	    litlenTree = InflaterHuffmanTree.defLitLenTree;
	    distTree = InflaterHuffmanTree.defDistTree;
	    mode = DECODE_HUFFMAN;
	    break;
	  case DeflaterConstants.DYN_TREES:
	    dynHeader = new InflaterDynHeader();
	    mode = DECODE_DYN_HEADER;
	    break;
	  default:
	    throw new DataFormatException("Unknown block type "+type);
	  }
	return true;

      case DECODE_STORED_LEN1:
	{
	  if ((uncomprLen = input.peekBits(16)) < 0)
	    return false;
	  input.dropBits(16);
	  mode = DECODE_STORED_LEN2;
	}
	/* fall through */
      case DECODE_STORED_LEN2:
	{
	  int nlen = input.peekBits(16);
	  if (nlen < 0)
	    return false;
	  input.dropBits(16);
	  if (nlen != (uncomprLen ^ 0xffff))
	    throw new DataFormatException("broken uncompressed block");
	  mode = DECODE_STORED;
	}
	/* fall through */
      case DECODE_STORED:
	{
	  int more = outputWindow.copyStored(input, uncomprLen);
	  uncomprLen -= more;
	  if (uncomprLen == 0)
	    {
	      mode = DECODE_BLOCKS;
	      return true;
	    }
	  return !input.needsInput();
	}

      case DECODE_DYN_HEADER:
	if (!dynHeader.decode(input))
	  return false;
	litlenTree = dynHeader.buildLitLenTree();
	distTree = dynHeader.buildDistTree();
	mode = DECODE_HUFFMAN;
	/* fall through */
      case DECODE_HUFFMAN:
      case DECODE_HUFFMAN_LENBITS:
      case DECODE_HUFFMAN_DIST:
      case DECODE_HUFFMAN_DISTBITS:
	return decodeHuffman();
      case FINISHED:
	return false;
      default:
	throw new IllegalStateException();
      }	
  }


    interface DeflaterConstants {
      final static boolean DEBUGGING = false;

      final static int STORED_BLOCK = 0;
      final static int STATIC_TREES = 1;
      final static int DYN_TREES    = 2;
      final static int PRESET_DICT  = 0x20;

      final static int DEFAULT_MEM_LEVEL = 8;

      final static int MAX_MATCH = 258;
      final static int MIN_MATCH = 3;

      final static int MAX_WBITS = 15;
      final static int WSIZE = 1 << MAX_WBITS;
      final static int WMASK = WSIZE - 1;

      final static int HASH_BITS = DEFAULT_MEM_LEVEL + 7;
      final static int HASH_SIZE = 1 << HASH_BITS;
      final static int HASH_MASK = HASH_SIZE - 1;
      final static int HASH_SHIFT = (HASH_BITS + MIN_MATCH - 1) / MIN_MATCH;

      final static int MIN_LOOKAHEAD = MAX_MATCH + MIN_MATCH + 1;
      final static int MAX_DIST = WSIZE - MIN_LOOKAHEAD;

      final static int PENDING_BUF_SIZE = 1 << (DEFAULT_MEM_LEVEL + 8);
      final static int MAX_BLOCK_SIZE = Math.min(65535, PENDING_BUF_SIZE-5);

      final static int DEFLATE_STORED = 0;
      final static int DEFLATE_FAST   = 1;
      final static int DEFLATE_SLOW   = 2;

      final static int GOOD_LENGTH[] = { 0,4, 4, 4, 4, 8,  8,  8,  32,  32 };
      final static int MAX_LAZY[]    = { 0,4, 5, 6, 4,16, 16, 32, 128, 258 };
      final static int NICE_LENGTH[] = { 0,8,16,32,16,32,128,128, 258, 258 };
      final static int MAX_CHAIN[]   = { 0,4, 8,32,16,32,128,256,1024,4096 };
      final static int COMPR_FUNC[]  = { 0,1, 1, 1, 1, 2,  2,  2,   2,   2 };
    }
    private static class InflaterHuffmanTree {
      private final static int MAX_BITLEN = 15;
      private short[] tree;

      public static InflaterHuffmanTree defLitLenTree, defDistTree;

      static
      {
        try 
          {
        byte[] codeLengths = new byte[288];
        int i = 0;
        while (i < 144)
          codeLengths[i++] = 8;
        while (i < 256)
          codeLengths[i++] = 9;
        while (i < 280)
          codeLengths[i++] = 7;
        while (i < 288)
          codeLengths[i++] = 8;
        defLitLenTree = new InflaterHuffmanTree(codeLengths);

        codeLengths = new byte[32];
        i = 0;
        while (i < 32)
          codeLengths[i++] = 5;
        defDistTree = new InflaterHuffmanTree(codeLengths);
          } 
        catch (DataFormatException ex)
          {
        throw new IllegalStateException
          ("InflaterHuffmanTree: static tree length illegal");
          }
      }

      /**
       * Constructs a Huffman tree from the array of code lengths.
       *
       * @param codeLengths the array of code lengths
       */
      public InflaterHuffmanTree(byte[] codeLengths) throws DataFormatException
      {
        buildTree(codeLengths);
      }

      private void buildTree(byte[] codeLengths) throws DataFormatException
      {
        int[] blCount = new int[MAX_BITLEN+1];
        int[] nextCode = new int[MAX_BITLEN+1];
        for (int i = 0; i < codeLengths.length; i++)
          {
        int bits = codeLengths[i];
        if (bits > 0)
          blCount[bits]++;
          }

        int code = 0;
        int treeSize = 512;
        for (int bits = 1; bits <= MAX_BITLEN; bits++)
          {
        nextCode[bits] = code;
        code += blCount[bits] << (16 - bits);
        if (bits >= 10)
          {
            /* We need an extra table for bit lengths >= 10. */
            int start = nextCode[bits] & 0x1ff80;
            int end   = code & 0x1ff80;
            treeSize += (end - start) >> (16 - bits);
          }
          }
        if (code != 65536)
          throw new DataFormatException("Code lengths don't add up properly.");

        fillTable1(treeSize, code, blCount);

        for (int i = 0; i < codeLengths.length; i++)
          {
        int bits = codeLengths[i];
        if (bits == 0)
          continue;
        code = nextCode[bits];
        int revcode = bitReverse(code);
        if (bits <= 9)
          {
            do
              {
            tree[revcode] = (short) ((i << 4) | bits);
            revcode += 1 << bits;
              }
            while (revcode < 512);
          }
        else
          {
            int subTree = tree[revcode & 511];
            int treeLen = 1 << (subTree & 15);
            subTree = -(subTree >> 4);
            do
              { 
            tree[subTree | (revcode >> 9)] = (short) ((i << 4) | bits);
            revcode += 1 << bits;
              }
            while (revcode < treeLen);
          }
        nextCode[bits] = code + (1 << (16 - bits));
          }
      }
      private final static String bit4Reverse =
        "\000\010\004\014\002\012\006\016\001\011\005\015\003\013\007\017";
      static short bitReverse(int value) {
            return (short) (bit4Reverse.charAt(value & 0xf) << 12
                    | bit4Reverse.charAt((value >> 4) & 0xf) << 8
                    | bit4Reverse.charAt((value >> 8) & 0xf) << 4
                    | bit4Reverse.charAt(value >> 12));
      }
      
      /**
       * Reads the next symbol from input.  The symbol is encoded using the
       * huffman tree.
       * @param input the input source.
       * @return the next symbol, or -1 if not enough input is available.
       */
      public int getSymbol(StreamManipulator input) throws DataFormatException
      {
        int lookahead, symbol;
        if ((lookahead = input.peekBits(9)) >= 0)
          {
        if ((symbol = tree[lookahead]) >= 0)
          {
            input.dropBits(symbol & 15);
            return symbol >> 4;
          }
        int subtree = -(symbol >> 4);
        int bitlen = symbol & 15;
        if ((lookahead = input.peekBits(bitlen)) >= 0)
          {
            symbol = tree[subtree | (lookahead >> 9)];
            input.dropBits(symbol & 15);
            return symbol >> 4;
          }
        else
          {
            int bits = input.getAvailableBits();
            lookahead = input.peekBits(bits);
            symbol = tree[subtree | (lookahead >> 9)];
            if ((symbol & 15) <= bits)
              {
            input.dropBits(symbol & 15);
            return symbol >> 4;
              }
            else
              return -1;
          }
          }
        else
          {
        int bits = input.getAvailableBits();
        lookahead = input.peekBits(bits);
        symbol = tree[lookahead];
        if (symbol >= 0 && (symbol & 15) <= bits)
          {
            input.dropBits(symbol & 15);
            return symbol >> 4;
          }
        else
          return -1;
          }
      }

        private void fillTable1(int treeSize, int code, int[] blCount) {
            /* Now create and fill the extra tables from longest to shortest
             * bit len.  This way the sub trees will be aligned.
             */
            tree = new short[treeSize];
            int treePtr = 512;
            for (int bits = MAX_BITLEN; bits >= 10; bits--) {
                int end = code & 0x1ff80;
                code -= blCount[bits] << (16 - bits);
                int start = code & 0x1ff80;
                final int inc = 1 << 7;
                fillTable2(start, end, inc, treePtr, bits);
            }
        }

        private void fillTable2(int start, int end, final int inc, int treePtr, int bits) {
            for (int i = start; i < end; i += inc) {
                final short br = bitReverse(i);
                tree[br] = (short) ((-treePtr << 4) | bits);
                treePtr += 1 << (bits - 9);
            }
        }
    }
    private static class InflaterDynHeader
    {
      private static final int LNUM   = 0;
      private static final int DNUM   = 1;
      private static final int BLNUM  = 2;
      private static final int BLLENS = 3;
      private static final int LENS   = 4;
      private static final int REPS   = 5;

      private static final int repMin[]  = { 3, 3, 11 };
      private static final int repBits[] = { 2, 3,  7 };


      private byte[] blLens;
      private byte[] litdistLens;

      private InflaterHuffmanTree blTree;

      private int mode;
      private int lnum, dnum, blnum, num;
      private int repSymbol;
      private byte lastLen;
      private int ptr;

      private static final int[] BL_ORDER =
      { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15 };

      public InflaterDynHeader()
      {
      }

      public boolean decode(StreamManipulator input) throws DataFormatException
      {
      decode_loop:
        for (;;)
          {
        switch (mode)
          {
          case LNUM:
            lnum = input.peekBits(5);
            if (lnum < 0)
              return false;
            lnum += 257;
            input.dropBits(5);
    //  	    System.err.println("LNUM: "+lnum);
            mode = DNUM;
            /* fall through */
          case DNUM:
            dnum = input.peekBits(5);
            if (dnum < 0)
              return false;
            dnum++;
            input.dropBits(5);
    //  	    System.err.println("DNUM: "+dnum);
            num = lnum+dnum;
            litdistLens = new byte[num];
            mode = BLNUM;
            /* fall through */
          case BLNUM:
            blnum = input.peekBits(4);
            if (blnum < 0)
              return false;
            blnum += 4;
            input.dropBits(4);
            blLens = new byte[19];
            ptr = 0;
    //  	    System.err.println("BLNUM: "+blnum);
            mode = BLLENS;
            /* fall through */
          case BLLENS:
            while (ptr < blnum)
              {
            int len = input.peekBits(3);
            if (len < 0)
              return false;
            input.dropBits(3);
    //  		System.err.println("blLens["+BL_ORDER[ptr]+"]: "+len);
            blLens[BL_ORDER[ptr]] = (byte) len;
            ptr++;
              }
            blTree = new InflaterHuffmanTree(blLens);
            blLens = null;
            ptr = 0;
            mode = LENS;
            /* fall through */
          case LENS:
            {
              int symbol;
              while (((symbol = blTree.getSymbol(input)) & ~15) == 0)
            {
              /* Normal case: symbol in [0..15] */

    //  		  System.err.println("litdistLens["+ptr+"]: "+symbol);
              litdistLens[ptr++] = lastLen = (byte) symbol;

              if (ptr == num)
                {
                  /* Finished */
                  return true;
                }
            }

              /* need more input ? */
              if (symbol < 0)
            return false;

              /* otherwise repeat code */
              if (symbol >= 17)
            {
              /* repeat zero */
    //  		  System.err.println("repeating zero");
              lastLen = 0;
            }
              else
            {
              if (ptr == 0)
                throw new DataFormatException();
            }
              repSymbol = symbol-16;
              mode = REPS;
            }
            /* fall through */

          case REPS:
            {
              int bits = repBits[repSymbol];
              int count = input.peekBits(bits);
              if (count < 0)
            return false;
              input.dropBits(bits);
              count += repMin[repSymbol];
    //  	      System.err.println("litdistLens repeated: "+count);

              if (ptr + count > num)
            throw new DataFormatException();
              while (count-- > 0)
            litdistLens[ptr++] = lastLen;

              if (ptr == num)
            {
              /* Finished */
              return true;
            }
            }
            mode = LENS;
            continue decode_loop;
          }
          }
      }

      public InflaterHuffmanTree buildLitLenTree() throws DataFormatException
      {
        byte[] litlenLens = new byte[lnum];
        System.arraycopy(litdistLens, 0, litlenLens, 0, lnum);
        return new InflaterHuffmanTree(litlenLens);
      }

      public InflaterHuffmanTree buildDistTree() throws DataFormatException
      {
        byte[] distLens = new byte[dnum];
        System.arraycopy(litdistLens, lnum, distLens, 0, dnum);
        return new InflaterHuffmanTree(distLens);
      }
    }
    /**
     * This class allows us to retrieve a specified amount of bits from
     * the input buffer, as well as copy big byte blocks.
     *
     * It uses an int buffer to store up to 31 bits for direct
     * manipulation.  This guarantees that we can get at least 16 bits,
     * but we only need at most 15, so this is all safe.
     *
     * There are some optimizations in this class, for example, you must
     * never peek more then 8 bits more than needed, and you must first 
     * peek bits before you may drop them.  This is not a general purpose
     * class but optimized for the behaviour of the Inflater.
     *
     * @author John Leuner, Jochen Hoenicke
     */

    private static class StreamManipulator
    {
      private byte[] window;
      private int window_start = 0;
      private int window_end = 0;

      private int buffer = 0;
      private int bits_in_buffer = 0;

      /**
       * Get the next n bits but don't increase input pointer.  n must be
       * less or equal 16 and if you if this call succeeds, you must drop
       * at least n-8 bits in the next call.
       * 
       * @return the value of the bits, or -1 if not enough bits available.  */
      public final int peekBits(int n)
      {
        if (bits_in_buffer < n)
          {
        if (window_start == window_end)
          return -1;
        buffer |= (window[window_start++] & 0xff
               | (window[window_start++] & 0xff) << 8) << bits_in_buffer;
        bits_in_buffer += 16;
          }
        return buffer & ((1 << n) - 1);
      }

      /* Drops the next n bits from the input.  You should have called peekBits
       * with a bigger or equal n before, to make sure that enough bits are in
       * the bit buffer.
       */
      public final void dropBits(int n)
      {
        buffer >>>= n;
        bits_in_buffer -= n;
      }

      /**
       * Gets the next n bits and increases input pointer.  This is equivalent
       * to peekBits followed by dropBits, except for correct error handling.
       * @return the value of the bits, or -1 if not enough bits available. 
       */
      public final int getBits(int n)
      {
        int bits = peekBits(n);
        if (bits >= 0)
          dropBits(n);
        return bits;
      }
      /**
       * Gets the number of bits available in the bit buffer.  This must be
       * only called when a previous peekBits() returned -1.
       * @return the number of bits available.
       */
      public final int getAvailableBits()
      {
        return bits_in_buffer;
      }

      /**
       * Gets the number of bytes available.  
       * @return the number of bytes available.
       */
      public final int getAvailableBytes()
      {
        return window_end - window_start + (bits_in_buffer >> 3);
      }

      /**
       * Skips to the next byte boundary.
       */
      public void skipToByteBoundary()
      {
        buffer >>= (bits_in_buffer & 7);
        bits_in_buffer &= ~7;
      }

      public final boolean needsInput() {
        return window_start == window_end;
      }


      /* Copies length bytes from input buffer to output buffer starting
       * at output[offset].  You have to make sure, that the buffer is
       * byte aligned.  If not enough bytes are available, copies fewer
       * bytes.
       * @param length the length to copy, 0 is allowed.
       * @return the number of bytes copied, 0 if no byte is available.  
       */
      public int copyBytes(byte[] output, int offset, int length)
      {
        if (length < 0)
          throw new IllegalArgumentException("length negative");
        if ((bits_in_buffer & 7) != 0)  
          /* bits_in_buffer may only be 0 or 8 */
          throw new IllegalStateException("Bit buffer is not aligned!");

        int count = 0;
        while (bits_in_buffer > 0 && length > 0)
          {
        output[offset++] = (byte) buffer;
        buffer >>>= 8;
        bits_in_buffer -= 8;
        length--;
        count++;
          }
        if (length == 0)
          return count;

        int avail = window_end - window_start;
        if (length > avail)
          length = avail;
        System.arraycopy(window, window_start, output, offset, length);
        window_start += length;

        if (((window_start - window_end) & 1) != 0)
          {
        /* We always want an even number of bytes in input, see peekBits */
        buffer = (window[window_start++] & 0xff);
        bits_in_buffer = 8;
          }
        return count + length;
      }

      public StreamManipulator()
      {
      }

      public void reset()
      {
        window_start = window_end = buffer = bits_in_buffer = 0;
      }

      public void setInput(byte[] buf, int off, int len)
      {
        if (window_start < window_end)
          throw new IllegalStateException
        ("Old input was not completely processed");

        int end = off + len;

        /* We want to throw an ArrayIndexOutOfBoundsException early.  The
         * check is very tricky: it also handles integer wrap around.  
         */
        if (0 > off || off > end || end > buf.length)
          throw new ArrayIndexOutOfBoundsException();

        if ((len & 1) != 0)
          {
        /* We always want an even number of bytes in input, see peekBits */
        buffer |= (buf[off++] & 0xff) << bits_in_buffer;
        bits_in_buffer += 8;
          }

        window = buf;
        window_start = off;
        window_end = end;
      }
    }
    /*
     * Contains the output from the Inflation process.
     *
     * We need to have a window so that we can refer backwards into the output stream
     * to repeat stuff.
     *
     * @author John Leuner
     * @since JDK 1.1
     */

    private static class OutputWindow
    {
      private final int WINDOW_SIZE = 1 << 15;
      private final int WINDOW_MASK = WINDOW_SIZE - 1;

      private byte[] window = new byte[WINDOW_SIZE]; //The window is 2^15 bytes
      private int window_end  = 0;
      private int window_filled = 0;

      public void write(int abyte)
      {
        if (window_filled++ == WINDOW_SIZE)
          throw new IllegalStateException("Window full");
        window[window_end++] = (byte) abyte;
        window_end &= WINDOW_MASK;
      }


      private final void slowRepeat(int rep_start, int len, int dist)
      {
        while (len-- > 0)
          {
        window[window_end++] = window[rep_start++];
        window_end &= WINDOW_MASK;
        rep_start &= WINDOW_MASK;
          }
      }

      public void repeat(int len, int dist)
      {
        if ((window_filled += len) > WINDOW_SIZE)
          throw new IllegalStateException("Window full");

        int rep_start = (window_end - dist) & WINDOW_MASK;
        int border = WINDOW_SIZE - len;
        if (rep_start <= border && window_end < border)
          {
        if (len <= dist)
          {
            System.arraycopy(window, rep_start, window, window_end, len);
            window_end += len;
          }
        else
          {
            /* We have to copy manually, since the repeat pattern overlaps.
             */
            while (len-- > 0)
              window[window_end++] = window[rep_start++];
          }
          }
        else
          slowRepeat(rep_start, len, dist);
      }

      public int copyStored(StreamManipulator input, int len)
      {
        len = Math.min(Math.min(len, WINDOW_SIZE - window_filled), 
               input.getAvailableBytes());
        int copied;

        int tailLen = WINDOW_SIZE - window_end;
        if (len > tailLen)
          {
        copied = input.copyBytes(window, window_end, tailLen);
        if (copied == tailLen)
          copied += input.copyBytes(window, 0, len - tailLen);
          }
        else
          copied = input.copyBytes(window, window_end, len);

        window_end = (window_end + copied) & WINDOW_MASK;
        window_filled += copied;
        return copied;
      }

      public void copyDict(byte[] dict, int offset, int len)
      {
        if (window_filled > 0)
          throw new IllegalStateException();

        if (len > WINDOW_SIZE)
          {
        offset += len - WINDOW_SIZE;
        len = WINDOW_SIZE;
          }
        System.arraycopy(dict, offset, window, 0, len);
        window_end = len & WINDOW_MASK;
      }

      public int getFreeSpace()
      {
        return WINDOW_SIZE - window_filled;
      }

      public int getAvailable()
      {
        return window_filled;
      }

      public int copyOutput(byte[] output, int offset, int len)
      {
        int copy_end = window_end;
        if (len > window_filled)
          len = window_filled;
        else
          copy_end = (window_end - window_filled + len) & WINDOW_MASK;

        int copied = len;
        int tailLen = len - copy_end;

        if (tailLen > 0)
          {
        System.arraycopy(window, WINDOW_SIZE - tailLen,
                 output, offset, tailLen);
        offset += tailLen;
        len = copy_end;
          }
        System.arraycopy(window, copy_end - len, output, offset, len);
        window_filled -= copied;
        if (window_filled < 0)
          throw new IllegalStateException();
        return copied;
      }

      public void reset() {
        window_filled = window_end = 0;
      }
    }
  
}
