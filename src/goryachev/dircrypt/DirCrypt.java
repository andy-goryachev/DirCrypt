// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.UserException;
import java.io.File;
import java.util.List;


/**
 * DirCrypt Command.
 */
public class DirCrypt
{
	public static void main(String[] args)
	{
		try
		{
			CmdArgs a = CmdArgs.parse(args);
			a.validate();
			
			Logger log = new Logger(a.verbose);
			
			if(a.encrypt)
			{
				String s = a.outputDir;
				File outFile = checkOutputFile(s);
				List<File> dirs = checkDirectories(a.dirs);
				
				DirCryptProcess.encrypt(log, dirs, outFile);
			}
			else if(a.decrypt)
			{
				String inputFile = a.inputFile;
				File in = checkInputFile(inputFile);
				
				DirCryptProcess.decrypt(log, in);
			}
			else
			{
				throw new UserException("Must specify --enc (encrypt) or --dec (decrypt).");
			}
		}
		catch(UserException e)
		{
			System.out.println(e.getMessage());
			System.out.println("\n");
			System.out.println(CmdArgs.usage());
			
			System.exit(-1);
		}
		catch(Throwable e)
		{
			e.printStackTrace(System.out);
			System.out.println("\n");
			System.out.println(CmdArgs.usage());

			System.exit(-2);
		}
	}
	
	
	private static List<File> checkDirectories(List<String> dirs)
	{
		CList<File> fs = new CList<>();
		
		if(dirs.size() == 0)
		{
			File f = new File(".");
			fs.add(f);
		}
		else
		{
			for(String s: dirs)
			{
				File f = new File(s);
				if(f.exists() && f.isDirectory())
				{
					fs.add(f);
				}
				else
				{
					throw err("Missing input directory: " + s);
				}
			}
		}
		
		return fs;
	}


	private static File checkInputFile(String fname)
	{
		if(CKit.isNotBlank(fname))
		{
			File f = new File(fname);
			if(f.exists() && f.isFile())
			{
				return f;
			}
		}
		throw err("Missing input file");
	}
	
	
	private static File checkOutputFile(String fname)
	{
		if(CKit.isNotBlank(fname))
		{
			File f = new File(fname);
			if(f.exists())
			{
				// TODO overwrite option?
				throw err("File already exists: " + f);
			}
			return f;
		}
		throw err("Missing output file");
	}


	private static UserException err(String text)
	{
		return new UserException(text);
	}
}
