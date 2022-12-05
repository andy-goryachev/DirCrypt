// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.io.DReader;
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
	private final CList<HeaderEntry> entries = new CList<>();
	
	
	public Header()
	{
	}
	
	
	// FIX reading unathenticated data, might run into size problem
	public static Header read(InputStream in) throws IOException
	{
		DReader rd = new DReader(in);
		try
		{
			int size = rd.readInt();
			if(size < 0)
			{
				throw new IOException("Format error (1)");
			}
			
			Header h = new Header();
			for(int i=0; i<size; i++)
			{
				int type = rd.readByte();
				switch(type)
				{
				case FileFormatV1.TYPE_DIR:
					{
						String name = rd.readString();
						h.addDir(name);
					}
					break;
				case FileFormatV1.TYPE_END:
					h.addEnd();
					break;
				case FileFormatV1.TYPE_FILE:
					{
						String name = rd.readString();
						long len = rd.readLong();
						long mod = rd.readLong();
						byte[] hash = rd.readNBytes(FileFormatV1.FILE_HASH_SIZE_BYTES);
						
						HeaderEntry en = h.addFile(name, len, mod);
						en.setHash(hash);
					}
					break;
				default:
					throw new IOException("Format error (2)");
				}
			}
			return h;
		}
		finally
		{
			CKit.close(rd);
		}
	}
	
	
	public int getEntryCount()
	{
		return entries.size();
	}
	
	
	public HeaderEntry getEntry(int ix)
	{
		return entries.get(ix);
	}
	
	
	/** serialized length in bytes */
	public int getLengthInBytes()
	{
		int len = 4; // int size;
		
		int size = entries.size();
		for(int i=0; i<size; i++)
		{
			HeaderEntry en = entries.get(i);
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
				HeaderEntry en = entries.get(i);
				en.write(wr);
			}
			
			wr.flush();
		}
		finally
		{
			CKit.close(wr);
		}
	}

	
	protected void add(HeaderEntry en)
	{
		entries.add(en);
	}


	public void addDir(String name)
	{
		add(new HeaderEntry()
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
				wr.writeByte(FileFormatV1.TYPE_DIR);
				wr.writeString(name);
			}
		});
	}
	

	public void addEnd()
	{
		add(new HeaderEntry()
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


	public HeaderEntry addFile(String name, long len, long mod)
	{
		HeaderEntry en = new HeaderEntry()
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
			
			
			public void setHash(byte[] hash)
			{
				this.hash = hash;
			}
			
			
			public int getLength()
			{
				return OVERHEAD + CKit.getBytes(name).length;
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				if(hash == null)
				{
					throw new IOException("null hash for entry " + this);
				}
				
				wr.writeByte(FileFormatV1.TYPE_FILE);
				wr.writeString(name);
				wr.writeLong(len);
				wr.writeLong(mod);
				wr.write(hash);
			}
		};
		add(en);
		return en;
	}
}
