// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.UserException;


/**
 * DirCrypt.
 */
public class DirCrypt
{
	public static void main(String[] args)
	{
		try
		{
			CmdArgs a = CmdArgs.parse(args);
			if(a.encrypt)
			{
				// TODO directories
				String outFile = a.outputDir;
			}
			else if(a.decrypt)
			{
				String inputFile = a.inputFile;
				if(CKit.isBlank(inputFile))
				{
					throw err("Missing input file");
				}
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
	
	
	private static UserException err(String text)
	{
		return new UserException(text);
	}
}
