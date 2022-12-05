// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.UserException;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.File;
import java.util.Arrays;
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
		km.checkError();
		
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
			int count = h.getEntryCount();
			for(int i=0; i<count; i++)
			{
				HeaderEntry en = h.getEntry(i);
				
				if(en.getType() == EntryType.FILE)
				{
					byte[] hash = new byte[FileFormatV1.FILE_HASH_SIZE_BYTES]; // FIX
					en.setHash(hash);
				}
			}
			
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
		byte[] storedRandomness = CKit.readBytes(inputFile, FileFormatV1.IV_SIZE_BYTES);
		if(storedRandomness.length != FileFormatV1.IV_SIZE_BYTES)
		{
			throw new UserException("Input file is too short: " + inputFile);
		}
		
		// generate key
		KeyMaterial km = KeyMaterial.generate(pass, storedRandomness);
		km.checkError();
		
		XSalsaRandomAccessFile rf = new XSalsaRandomAccessFile(inputFile, false, km.key, km.iv);
		try
		{
			// read initial randomness
			byte[] initialRandomness = new byte[FileFormatV1.IV_SIZE_BYTES];
			rf.readUnencrypted(initialRandomness);

			// check if it's the same
			if(!Arrays.equals(storedRandomness, initialRandomness))
			{
				throw new UserException("File replaced white reading " + inputFile);
			}
			
			// read and validate signature
			long sig = rf.readLong();
			if(sig != FileFormatV1.SIGNATURE)
			{
				throw new UserException("Not a valid input file: " + inputFile);
			}
			
			// read header
			Header h = Header.read(new InputStreamWrapper(rf));
			
			// validate header
			// TODO
			
			// extract files
			// TODO
		}
		finally
		{
			CKit.close(rf);
		}
	}

	
	private static Future<KeyMaterial> generateKey(String pass, byte[] storedRandomness)
	{
		CompletableFuture<KeyMaterial> f = new CompletableFuture();
		
		new Thread("generating key")
		{
			public void run()
			{
				KeyMaterial km = KeyMaterial.generate(pass, storedRandomness);
				f.complete(km);
			}
		}.start();

		return f;
	}
}
