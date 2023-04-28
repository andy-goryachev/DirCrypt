// Copyright © 2022-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.Hex;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Header contains directory structure.
 */
public class Header
{
	private final CList<HeaderEntry> entries = new CList<>();
	private transient HeaderEntry lastDir;
	
	
	public Header()
	{
	}
	
	
	public static Header read(Logger log, InputStream in) throws IOException
	{
		DReader rd = new DReader(in);
		try
		{
			int count = rd.readInt();
			log.log("HEADER", "entryCount", count);
			if(count < 0)
			{
				throw new IOException("Format error: entry count");
			}
			
			Header h = new Header();
			for(int i=0; i<count; i++)
			{
				int type = rd.readByte();
				switch(type)
				{
				case FileFormatV1.TYPE_DIR:
					{
						String name = readString(rd);
						h.addDir(name);
						log.log("HEADER dir", "name", name);
					}
					break;
				case FileFormatV1.TYPE_END:
					h.addEnd();
					log.log("HEADER enddir");
					break;
				case FileFormatV1.TYPE_FILE:
					{
						String name = readString(rd);
						long len = rd.readLong();
						long mod = rd.readLong();
						byte[] hash = rd.readNBytes(FileFormatV1.FILE_HASH_SIZE_BYTES);
						
						HeaderEntry en = h.addFile(name, len, mod);
						en.setHash(hash);
						log.log(() ->
						{
							log.log("HEADER file", "name", name, "len", len, "modified", mod, "hash", Hex.toHexString(hash));
						});
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
			int count = entries.size();
			wr.writeInt(count);
			
			for(int i=0; i<count; i++)
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
		HeaderEntry en = new HeaderEntry()
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
				return 1 + stringLength(name);
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				wr.writeByte(FileFormatV1.TYPE_DIR);
				writeString(wr, name);
			}
		};

		if(lastDir != null)
		{
			en.setParent(lastDir);
		}
		lastDir = en;

		add(en);
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
		
		lastDir = lastDir.getParent(); 
	}


	public HeaderEntry addFile(String name, long len, long mod)
	{
		HeaderEntry en = new HeaderEntry()
		{
			private byte[] hash;
			private File file;
			
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
			
			
			public byte[] getHash()
			{
				return hash;
			}
			
			
			public void setHash(byte[] hash)
			{
				this.hash = hash;
			}


			public File getFile()
			{
				return file;
			}


			public void setFile(File f)
			{
				this.file = f;
			}


			public int getLength()
			{
				return OVERHEAD + stringLength(name);
			}
			
			
			public void write(DWriter wr) throws IOException
			{
				if(hash == null)
				{
					throw new IOException("null hash for entry " + this);
				}
				
				wr.writeByte(FileFormatV1.TYPE_FILE);
				writeString(wr, name);
				wr.writeLong(len);
				wr.writeLong(mod);
				wr.write(hash);
			}
		};
		
		en.setParent(lastDir);
		add(en);
		return en;
	}
	
	
	protected static int stringLength(String s)
	{
		return 4 + CKit.getBytes(s).length;
	}
	
	
	protected static void writeString(DWriter wr, String s) throws IOException
	{
		byte[] b = CKit.getBytes(s);
		wr.writeInt(b.length);
		wr.write(b);
	}
	
	
	protected static String readString(DReader rd) throws IOException
	{
		int len = rd.readInt();
		byte[] b = rd.readFully(len);
		return new String(b, CKit.CHARSET_UTF8);
	}
	
	
	public long computeMaxFileLength()
	{
		long max = 0;
		for(HeaderEntry en: entries)
		{
			if(en.getType() == EntryType.FILE)
			{
				long len = en.getFileLength();
				if(len > max)
				{
					max = len;
				}
			}
		}
		return max;
	}
}
