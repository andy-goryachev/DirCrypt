// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import java.io.IOException;
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
		int len = 4; // int size;
		
		int size = entries.size();
		for(int i=0; i<size; i++)
		{
			Entry en = entries.get(i);
			len += en.getLength();
		}
		
		return len;
	}
	
	
	public void write(OutputStream out) throws Exception
	{
		DWriter wr = new DWriter(out);
		try
		{
			int size = entries.size();
			wr.writeInt(size);
			
			for(int i=0; i<size; i++)
			{
				Entry en = entries.get(i);
				en.write(wr);
			}
			
			wr.flush();
		}
		finally
		{
			CKit.close(wr);
		}
	}

	
	protected void add(Entry en)
	{
		entries.add(en);
	}


	public void addDir(String name)
	{
		add(new Entry()
		{
			public EntryType getType()
			{
				return EntryType.DIR;
			}


			public String getName()
			{
				return name;
			}


			public int getLength()
			{
				return 1 + CKit.getBytes(name).length;
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				byte[] b = CKit.getBytes(name);
				
				wr.writeByte(FileFormatV1.TYPE_DIR);
				wr.writeByteArray(b);
			}
		});
	}
	

	public void addEnd()
	{
		add(new Entry()
		{
			public EntryType getType()
			{
				return EntryType.END;
			}


			public int getLength()
			{
				return 1;
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				wr.writeByte(FileFormatV1.TYPE_END);
			}
		});
	}


	public void addFile(String name, long len, long mod)
	{
		add(new Entry()
		{
			private byte[] hash;
			private static final int OVERHEAD =
				1 + // type
				8 + // length
				8 + // last modified
				FileFormatV1.FILE_HASH_SIZE_BYTES; // hash
			
			
			public EntryType getType()
			{
				return EntryType.FILE;
			}


			public String getName()
			{
				return name;
			}


			public long getFileLength()
			{
				return len;
			};


			public long getLastModified()
			{
				return mod;
			}
			
			
			public int getLength()
			{
				return OVERHEAD + CKit.getBytes(name).length;
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				wr.writeByte(FileFormatV1.TYPE_FILE);
				wr.writeString(name);
				wr.writeLong(len);
				wr.writeLong(mod);
				wr.writeByteArray(hash);
			}


			public byte[] getHash()
			{
				return hash;
			}


			public void setHash(byte[] hash)
			{
				this.hash = hash;
			}
		});
	}

	
	//
	
	
	public static abstract class Entry
	{
		public abstract EntryType getType();
		
		public abstract int getLength();
		
		public abstract void write(DWriter wr) throws IOException;
		
		public String getName() { return null; }
		
		public long getLastModified() { return 0L; }
		
		public long getFileLength() { return -1L; }
		
		public byte[] getHash() { throw new UnsupportedOperationException(); }
		
		public void setHash(byte[] hash) { throw new UnsupportedOperationException(); }
		
		//
		
		private transient Entry parent;
		
		
		public String toString()
		{
			String name = getName();
			return getType() + (name == null ? "" : " " + name);
		}
	}
}
