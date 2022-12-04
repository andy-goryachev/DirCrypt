// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;


/**
 * Logger for when verbose output is requested.
 */
public class Logger
{
	private final boolean on;


	public Logger(boolean on)
	{
		this.on = on;
	}
	
	
	public void printf(String fmt, Object ... args)
	{
		if(on)
		{
			String msg = String.format(fmt, args);
			System.out.println(msg);
		}
	}
}
