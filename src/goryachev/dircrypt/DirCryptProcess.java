// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.FileTools;
import goryachev.common.util.Hex;
import goryachev.common.util.UserException;
import goryachev.memsafecrypto.bc.Blake2bDigest;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


/**
 * Dir Crypt Process.
 * 
 * TODO log elapsed time
 * TODO log data size
 */
public class DirCryptProcess
{
	private static final int BUFFER_SIZE = 65536;
	
	
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
		log.log("HEADER CREATED", "itemCount", h.getEntryCount());
		
		KeyMaterial km = futureKey.get();
		km.checkError();
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		XSalsaRandomAccessFile rf = new XSalsaRandomAccessFile(f, true, km.key, km.iv);
		try
		{
			// write random
			byte[] rnd = km.iv.toByteArray();
			rf.writeUnencrypted(rnd);
			
			// write signature
			rf.writeLong(FileFormatV1.SIGNATURE);
						
			// write header-size byte array
			int size = h.getLengthInBytes();
			log.log("HEADER", "size", size);
			rf.writeInt(size);
			
			// store offset
			// TODO use file offset?
			long offset = rnd.length + 8 + 4;
			
			byte[] nullHeader = new byte[size];
			rf.write(nullHeader);
			
			// write files, set hash values, check for differences
			int count = h.getEntryCount();
			log.log("HEADER", "itemCount", count);
			
			for(int i=0; i<count; i++)
			{
				HeaderEntry en = h.getEntry(i);
				
				if(en.getType() == EntryType.FILE)
				{
					log.log("ENCRYPT", "file", en.getName());
					byte[] hash = encryptFile(en, rf, buffer);
					en.setHash(hash);
					log.log(() -> {
						log.log("HASH", "name", en.getName(), "hash", Hex.toHexString(hash));
					});
				}
			}
			
			// set offset
			rf.seek(offset);
			
			// write header
			h.write(new OutputStreamWrapper(rf));
			
			// check cur.offset == offset + header.size
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


	public static void decrypt(Logger log, String pass, File inputFile, File destDir) throws Exception
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
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
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
			int size = rf.readInt();
			log.log("HEADER", "size", size);
			if(size < 0)
			{
				throw new Exception("Format error: header size");
			}
			Header h = Header.read(log, new InputStreamWrapper(rf, size));
			log.log("READ header");
			
			// validate header
			// TODO
			
			// extract files
			int count = h.getEntryCount();
			for(int i=0; i<count; i++)
			{
				HeaderEntry en = h.getEntry(i);
				
				// switch won't work because it creates resource not closed warning
				if(en.getType() == EntryType.DIR)
				{
					File dir = new File(destDir, en.getPath());
					dir.mkdirs();
					// TODO check if exists?
				}
				else if(en.getType() == EntryType.FILE)
				{
					log.log("EXTRACT", "file", en.getName());
					byte[] hash = decryptFile(en, rf, destDir, buffer);
					
					// compare hash
					if(!Arrays.equals(en.getHash(), hash))
					{
						throw new UserException("hash mismatch file=" + en); // TODO file path
					}
				}
			}
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
	
	
	private static byte[] encryptFile(HeaderEntry en, XSalsaRandomAccessFile rf, byte[] buf) throws Exception
	{
		File f = en.getFile();

		try(InputStream in = new BufferedInputStream(new FileInputStream(f)))
		{
			try(OutputStream out = new OutputStreamWrapper(rf))
			{
				return copyWithDigest(in, out, buf);
			}
		}
	}
	
	
	private static byte[] decryptFile(HeaderEntry en, XSalsaRandomAccessFile rf, File destDir, byte[] buf) throws Exception
	{
		String path = en.getPath();
		File f = new File(destDir, path);

		// TODO check if exists
		
		long len = en.getFileLength();
		try(InputStream in = new InputStreamWrapper(rf, len))
		{
			try(OutputStream out = new FileOutputStream(f))
			{
				return copyWithDigest(in, out, buf);
			}
		}
	}
	
	
	private static byte[] copyWithDigest(InputStream in, OutputStream out, byte[] buf) throws Exception
	{
		Blake2bDigest digest = new Blake2bDigest(FileFormatV1.FILE_HASH_SIZE_BITS);

		for(;;)
		{
			if(Thread.interrupted())
			{
				throw new InterruptedException();
			}
			
			int rd = in.read(buf);
			if(rd < 0)
			{
				byte[] b = new byte[FileFormatV1.FILE_HASH_SIZE_BYTES];
				digest.doFinal(b, 0);
				return b;
			}
			else if(rd > 0)
			{
				out.write(buf, 0, rd);
				digest.update(buf, 0, rd);
			}
		}
	}
}
