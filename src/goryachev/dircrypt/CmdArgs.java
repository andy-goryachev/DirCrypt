// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.SB;
import goryachev.common.util.UserException;


/**
 * Command Arguments Parser.
 */
public class CmdArgs
{
	private CmdArgs()
	{
	}
	
	
	public static CmdArgs parse(String[] args) throws UserException, Exception
	{
		CommandLineArgumentsParser p = new CommandLineArgumentsParser(args);
		CmdArgs a = new CmdArgs();
		String tok;
		while((tok = p.nextToken()) != null)
		{
			if(tok.startsWith("--"))
			{
				switch(tok)
				{
				
				}
			}
			else
			{
				
			}
		}
		return a;
	}
	
	
	public static String usage()
	{
		return
			"Usage:\n" +
			"  java -jar dirCrypt.jar [options] dir1 dir2 ...\n" +
			"Options:\n";
	}
}
