// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CComparator;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Scans file system, generates Header.
 */
public class FileScanner
{
	private final List<File> dirs;
	
	
	public FileScanner(List<File> dirs)
	{
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
				if(f.isDirectory())
				{
					String name = f.getName();
					// modified?
					h.addDir(name);
					scan(h, f);
					h.addEnd();
				}
				else if(f.isFile())
				{
					String name = f.getName();
					long len = f.length();
					long mod = f.lastModified();
					short attr = 0;
					h.addFile(name, len, mod, attr);
				}
				else
				{
					// FIX what could it be?
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
