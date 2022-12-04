// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CList;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Header contains directory structure.
 */
public class Header
{
	private final CList<Entry> entries = new CList<>();
	
	
	public Header()
	{
	}
	
	
	public static Header read(InputStream in)
	{
		// TODO
		return null;
	}
	
	
	/** serialized length in bytes */
	public int getLengthInBytes()
	{
		// TODO
		return 0;
	}
	
	
	public void write(OutputStream out) throws Exception
	{
		// TODO
	}

	
	protected void add(Entry en)
	{
		entries.add(en);
	}


	public void addDir(String name)
	{
		add(new Entry()
		{
			public EntryType getType() { return EntryType.DIR; }
			public String getName() { return name; }
		});
	}
	

	public void addEnd()
	{
		add(new Entry()
		{
			public EntryType getType() { return EntryType.END; }
		});
	}


	public void addFile(String name, long len, long mod, boolean readOnly)
	{
		add(new Entry()
		{
			public EntryType getType() { return EntryType.FILE; }
			public String getName() { return name; }
			public long getLength() { return len; };
			public long getLastModified() { return mod; }
			public boolean isReadOnly() { return readOnly; };
		});
	}
	
	
	//
	
	
	public static abstract class Entry
	{
		public abstract EntryType getType();
		
		public String getName() { return null; }
		
		public long getLength() { return -1L; };
		
		public long getLastModified() { return 0L; }
		
		public boolean isReadOnly() { return false; };
		
		//
		
		private transient Entry parent;
		
		
		public String toString()
		{
			String name = getName();
			return getType() + (name == null ? "" : " " + name);
		}
	}
}
