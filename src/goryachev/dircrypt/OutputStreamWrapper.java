// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.IOException;
import java.io.OutputStream;


/**
 * OutputStream Wrapper.
 */
public class OutputStreamWrapper
	extends OutputStream
{
	private final XSalsaRandomAccessFile file;
	
	
	public OutputStreamWrapper(XSalsaRandomAccessFile f)
	{
		this.file = f;
	}


	public void write(int b) throws IOException
	{
		file.writeByte(b);
	}
	
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		file.write(b, off, len);
	}
}
