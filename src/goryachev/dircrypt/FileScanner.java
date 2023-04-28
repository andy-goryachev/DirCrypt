// Copyright © 2022-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CComparator;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Scans file system, generates Header.
 */
public class FileScanner
{
	private final Logger log;
	private final List<File> dirs;
	
	
	public FileScanner(Logger log, List<File> dirs)
	{
		this.log = log;
		this.dirs = dirs;
	}
	
	
	public Header scan() throws Exception
	{
		Header h = new Header();
		
		for(File dir: dirs)
		{
			scan(h, dir);
		}
		return h;
	}


	protected void scan(Header h, File dir)
	{
		File[] files = dir.listFiles();
		if(files != null)
		{
			Arrays.sort(files, getFileComparator());
			
			for(File f: files)
			{
				String name = f.getName();
				
				if(f.isDirectory())
				{
					log.log("SCAN dir", "name", name);
					
					h.addDir(name);
					scan(h, f);
					h.addEnd();
					
					log.log("SCAN enddir");
				}
				else if(f.isFile())
				{
					long len = f.length();
					long mod = f.lastModified();
					
					log.log("SCAN file", "name", name, "len", len, "modified", mod);
					HeaderEntry en = h.addFile(name, len, mod);
					en.setFile(f);
				}
				else
				{
					log.log("SCAN unknown type", "name", name);
				}
			}
		}
	}
	
	
	protected Comparator<File> getFileComparator()
	{
		return new CComparator<File>()
		{
			public int compare(File a, File b)
			{
				if(a.isDirectory())
				{
					if(!b.isDirectory())
					{
						return -1;
					}
				}
				else
				{
					if(b.isDirectory())
					{
						return 1;
					}
				}
				return collate(a.getName(), b.getName());
			}
		};
	}
}
