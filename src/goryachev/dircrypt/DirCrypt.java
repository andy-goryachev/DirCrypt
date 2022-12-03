// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
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
		}
		catch(UserException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			
			CmdArgs.usage();
			System.exit(-2);
		}
	}
}
