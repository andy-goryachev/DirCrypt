// Copyright © 2022-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;


/**
 * Simplifies parsing of command line arguments.
 */
public class CommandLineArgumentsParser
{
	protected final String[] args;
	private int index;
	
	
	public CommandLineArgumentsParser(String[] args)
	{
		this.args = args;
	}
	
	
	public String nextToken()
	{
		if(index < args.length)
		{
			return args[index++];
		}
		return null;
	}
}
