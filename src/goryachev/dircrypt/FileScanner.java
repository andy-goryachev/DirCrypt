// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
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
					log.printf("SCAN dir %s", name);
					
					h.addDir(name);
					scan(h, f);
					h.addEnd();
					
					log.printf("SCAN dir END");
				}
				else if(f.isFile())
				{
					long len = f.length();
					long mod = f.lastModified();
					
					log.printf("SCAN file name=%s len=%d mod=%d", name, len, mod);
					// TODO skip non-readable files?
					// TODO rwxh
					boolean readOnly = !f.canWrite();
					boolean hidden = f.isHidden(); // TODO do we need attributes?
					//Files.getPosixFilePermissions()
					h.addFile(name, len, mod, readOnly);
				}
				else
				{
					// FIX what could it be?
					log.printf("SCAN unknown type name=%s", name);
				}
			}
		}
	}
	
	
	protected Comparator<File> getFileComparator()
	{
		return new CComparator<>()
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