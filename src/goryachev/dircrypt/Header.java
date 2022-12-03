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
	// TODO random?
	// TODO scrypt parameters?
	//private final long version;
	private final CList<Entry> entries = new CList<>();
	
	
	public Header()
	{
	}
	
	
	public static Header load(InputStream in)
	{
		// TODO
		return null;
	}
	
	
	/** serialized length in bytes */
	public int getByteCount()
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
		System.err.println(en); // FIX
		entries.add(en);
	}


	public void addDir(String name)
	{
		Entry en = new Entry(EntryType.DIR, name, 0, 0, (short)0);
		add(en);
	}
	

	public void addEnd()
	{
		Entry en = new Entry(EntryType.END, null, 0, 0, (short)0);
		add(en);
	}


	public void addFile(String name, long len, long mod, short attr)
	{
		Entry en = new Entry(EntryType.FILE, name, len, mod, attr);
		add(en);
	}
}
