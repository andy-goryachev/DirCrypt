// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.io.DWriter;
import java.io.File;
import java.io.IOException;


/**
 * Header Entry.
 */
public abstract class HeaderEntry
{
	public abstract EntryType getType();
	
	public abstract int getLength();
	
	public abstract void write(DWriter wr) throws IOException;
	
	public String getName() { return null; }
	
	public long getLastModified() { return 0L; }
	
	public long getFileLength() { return -1L; }
	
	public void setHash(byte[] hash) { throw new UnsupportedOperationException(); }
	
	public File getFile() { throw new UnsupportedOperationException(); }
	
	public void setFile(File f) { throw new UnsupportedOperationException(); }
	
	//
	
	private transient HeaderEntry parent;
	
	
	public String toString()
	{
		String name = getName();
		return getType() + (name == null ? "" : " " + name);
	}
}