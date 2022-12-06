// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.IOException;
import java.io.InputStream;


/**
 * InputStream Wrapper.
 */
public class InputStreamWrapper
	extends InputStream
{
	private final XSalsaRandomAccessFile file;
	private long available;
	
	
	public InputStreamWrapper(XSalsaRandomAccessFile f, long length)
	{
		this.file = f;
		this.available = length;
	}


	public int read() throws IOException
	{
		if(available == 0)
		{
			return -1;
		}
		
		int rv = file.readByte();
		if(rv >= 0)
		{
			available--;
		}
		return rv;
	}
	
	
	public int read(byte[] b, int off, int length) throws IOException
	{
		if(available == 0)
		{
			return -1;
		}
		
		int len = (int)Math.min(available, length);
		int rv = file.read(b, off, len);
		if(rv > 0)
		{
			available -= rv;
		}
		return rv;
	}
}
