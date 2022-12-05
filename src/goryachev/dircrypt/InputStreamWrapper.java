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
	
	
	public InputStreamWrapper(XSalsaRandomAccessFile f)
	{
		this.file = f;
	}


	public int read() throws IOException
	{
		return file.readByte();
	}
	
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return file.read(b, off, len);
	}
}
