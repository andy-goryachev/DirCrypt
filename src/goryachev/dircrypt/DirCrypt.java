// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.UserException;
import java.io.File;
import java.util.List;


/**
 * DirCrypt Command.
 * 
 * TODO --force 
 */
public class DirCrypt
{
	public static void main(String[] args)
	{
		try
		{
			CmdArgs a = CmdArgs.parse(args);

			Logger log = new Logger(a.verbose);
			
			if(a.usage)
			{
				System.out.println(CmdArgs.usage());
				System.exit(0);
			}
			else if(a.encrypt)
			{
				String pass = checkPassphrase(a.passPhrase);
				File outFile = checkOutputFile(a.outputFile);
				List<File> dirs = checkDirectories(a.dirs);
				
				DirCryptProcess.encrypt(log, pass, dirs, outFile);
			}
			else if(a.listing)
			{
				String pass = checkPassphrase(a.passPhrase);
				String inputFile = a.inputFile;
				File in = checkInputFile(inputFile);
				
				DirCryptProcess.decrypt(log, pass, in, null);
			}
			else if(a.decrypt)
			{
				String pass = checkPassphrase(a.passPhrase);
				String inputFile = a.inputFile;
				File in = checkInputFile(inputFile);
				File destDir = checkDestDir(a.destination);
				
				DirCryptProcess.decrypt(log, pass, in, destDir);
			}
			else
			{
				throw new UserException("Required: --enc, --dec, or --list.");
			}
		}
		catch(UserException e)
		{
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		catch(Throwable e)
		{
			e.printStackTrace(System.out);
			System.exit(-2);
		}
	}
	
	
	private static String checkPassphrase(String s)
	{
		if(CKit.isBlank(s))
		{
			throw err("Passphrase is required");
		}
		return s;
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
	
	
	private static File checkDestDir(String dir)
	{
		if(CKit.isBlank(dir))
		{
			throw err("Missing destination directory");
		}
		
		File f = new File(dir);
		f.mkdirs();
		
		if(f.exists() && f.isDirectory())
		{
			return f;
		}
		
		throw err("Unable to create destination directory: " + dir);
	}


	private static UserException err(String text)
	{
		return new UserException(text);
	}
}
