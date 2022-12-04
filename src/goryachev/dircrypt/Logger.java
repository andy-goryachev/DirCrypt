// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.SB;


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
	
	
	public void log(String event, Object ... nameValuePairs)
	{
		if(on)
		{
			SB sb = new SB();
			sb.append(event);
			
			for(int i=0; i<nameValuePairs.length; )
			{
				Object k = nameValuePairs[i++];
				Object v = nameValuePairs[i++];
			
				sb.sp();
				sb.append(k);
				sb.append("=");
				sb.append(v);
			}
			System.out.println(sb.toString());
		}
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
