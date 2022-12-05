// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.UserException;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


/**
 * Dir Crypt Process.
 */
public class DirCryptProcess
{
	public static void encrypt(Logger log, String pass, List<File> dirs, File outFile) throws Exception
	{
		// create a temp file before generating any key material
		File parent = outFile.getParentFile();
		parent.mkdirs();
		if(!parent.exists() || !parent.isDirectory())
		{
			throw new UserException("Unable to create output directory " + parent);
		}
		
		File f = File.createTempFile("DirCrypt.", null, parent);

		// generate key while scanning the file system
		Future<KeyMaterial> futureKey = generateKey(pass, null);

		FileScanner fs = new FileScanner(log, dirs);
		Header h = fs.scan();
		
		KeyMaterial km = futureKey.get();
		if(km.error != null)
		{
			throw km.error;
		}
		
		XSalsaRandomAccessFile rf = new XSalsaRandomAccessFile(f, true, km.key, km.iv);
		try
		{
			// write random
			byte[] rnd = km.iv.toByteArray();
			rf.writeUnencrypted(rnd);
			
			// write signature
			rf.writeLong(FileFormatV1.SIGNATURE);
			
			// store offset
			// TODO use file offset?
			long offset = rnd.length + 8;
			
			// write header-size byte array
			int sz = h.getLengthInBytes();
			byte[] nullHeader = new byte[sz];
			rf.write(nullHeader);
			
			// write files, set hash values, check for differences
			// TODO
			
			// set offset
			rf.seek(offset);
			
			// write header
			h.write(new OutputStreamWrapper(rf));
			
			// check cur.offset == offset + header.size
			// TODO
			
			// rename temp file
			// TODO
		}
		finally
		{
			CKit.close(rf);
		}
		
		boolean success = f.renameTo(outFile);
		if(!success)
		{
			throw new UserException("Unable to rename temp file " + f + " to " + outFile);
		}
	}


	public static void decrypt(Logger log, String pass, File inputFile) throws Exception
	{
		// TODO
		// read random
		// generate key
		// read signature
		// validate signature
		// read header
		// validate header
		// extract files
	}

	
	private static Future<KeyMaterial> generateKey(String pass, byte[] storedRandomness)
	{
		CompletableFuture<KeyMaterial> f = new CompletableFuture();
		
		new Thread("generating key")
		{
			public void run()
			{
				KeyMaterial k = KeyMaterial.generate(pass, storedRandomness);
				f.complete(k);
			}
		}.start();

		return f;
	}
}
