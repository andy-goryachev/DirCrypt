// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;


/**
 * Header Entry Attributes packed in a single byte.
 * 
 * TODO or perhaps use this to read/write
 * 
 * BitSet
 * {
 *   readOnly : 1
 *   executable : 1
 *   hidden : 1 ??
 *   symlink : 1 ??
 *   reserved : 2
 *   type: 2
 * }
 * 
 */
public class Attr
{
	private static final int MASK_TYPE = 0x03;
	private static final int TYPE_FILE = 0x00;
	private static final int TYPE_DIR = 0x01;
	private static final int TYPE_END = 0x02;
	private static final int MASK_READONLY = 0x80;
	private static final int MASK_EXECUTABLE = 0x40;
	private static final int MASK_HIDDEN = 0x20;
	
	private byte value;
	
	
	public Attr(EntryType t)
	{
	}
	
	
	public EntryType getEntryType()
	{
		int v = (value & MASK_TYPE);
		switch(v)
		{
		case TYPE_DIR:
			return EntryType.DIR;
		case TYPE_END:
			return EntryType.END;
		case TYPE_FILE:
			return EntryType.FILE;
		default:
			throw new RuntimeException(); // TODO format exception?
		}
	}
	
	
	public boolean isReadOnly()
	{
		return getBit(MASK_READONLY);
	}
	
	
	public void setReadOnly(boolean on)
	{
		setBit(MASK_READONLY, on);
	}
	
	
	public boolean isExecutable()
	{
		return getBit(MASK_EXECUTABLE);
	}
	
	
	public void setExecutable(boolean on)
	{
		setBit(MASK_EXECUTABLE, on);
	}
	
	
	private boolean getBit(int mask)
	{
		return (value & mask) != 0;
	}
	
	
	private void setBit(int mask, boolean on)
	{
		// TODO
	}
}
