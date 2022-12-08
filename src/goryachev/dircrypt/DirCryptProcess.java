// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.common.util.Hex;
import goryachev.common.util.SB;
import goryachev.common.util.UserException;
import goryachev.memsafecrypto.bc.Blake2bDigest;
import goryachev.memsafecrypto.salsa.XSalsaRandomAccessFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
	private static DateTimeFormatter dateFormatter;
	private static DecimalFormat numberFormatter;
	
	
	public static void encrypt(Logger log, String pass, List<File> dirs, File outFile, boolean force) throws Exception
	{
		// create a temp file before generating any key material
		File parent = outFile.getParentFile();
		parent.mkdirs();
		if(!parent.exists() || !parent.isDirectory())
		{
			throw new UserException("Unable to create output directory " + parent);
		}
		
		File file = File.createTempFile("DirCrypt.", null, parent);

		// generate key while scanning the file system
		Future<KeyMaterial> futureKey = generateKey(log, pass, null);

		FileScanner fs = new FileScanner(log, dirs);
		Header h = fs.scan();
		log.log("HEADER CREATED", "itemCount", h.getEntryCount());
		
		KeyMaterial km = futureKey.get();
		km.checkError();
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		XSalsaRandomAccessFile rf = new XSalsaRandomAccessFile(file, true, km.key, km.iv);
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
			log.log("HEADER", "entryCount", count);
			
			for(int i=0; i<count; i++)
			{
				HeaderEntry en = h.getEntry(i);
				
				if(en.getType() == EntryType.FILE)
				{
					File f = en.getFile();
					log.log("ENCRYPT", "file", f, "len", en.getFileLength(), "modified", en.getLastModified());

					byte[] hash = encryptFile(file, rf, buffer);
					en.setHash(hash);
					log.log(() ->
					{
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
		
		if(force)
		{
			if(outFile.exists())
			{
				boolean success = outFile.delete();
				if(!success)
				{
					throw new UserException("Unable to delete output file " + outFile);
				}
			}
		}
		
		boolean success = file.renameTo(outFile);
		if(!success)
		{
			throw new UserException("Unable to rename temp file " + file + " to " + outFile);
		}
	}


	public static void decrypt(Logger log, String pass, File inputFile, File destDir, boolean force) throws Exception
	{
		boolean listing = (destDir == null);
		
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
				throw new UserException("Invalid passphrase or not an encrypted file: " + inputFile);
			}
			
			// read header
			int size = rf.readInt();
			log.log("HEADER", "size", size);
			if(size < 0)
			{
				throw new Exception("Format error: header size");
			}
			// FIX reading unathenticated data, check for: sizes, counts, string lengths
			Header h = Header.read(log, new InputStreamWrapper(rf, size));
			log.log("READ header");
			
			// validate header
			// TODO
			
			int lenColWidth = 0;
			if(listing)
			{
				numberFormatter = new DecimalFormat("#,##0");
				
				dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
					
				long max = h.computeMaxFileLength();
				lenColWidth = formatLength(max).length();
			}
			
			// extract files
			int count = h.getEntryCount();
			for(int i=0; i<count; i++)
			{
				HeaderEntry en = h.getEntry(i);
				
				// switch won't work because it creates resource not closed warning
				if(en.getType() == EntryType.DIR)
				{
					if(!listing)
					{
						File dir = new File(destDir, en.getPath());
						dir.mkdirs();
					}
				}
				else if(en.getType() == EntryType.FILE)
				{
					if(listing)
					{
						String s = formatFileEntry(en, lenColWidth);
						log.print(s);
					}
					else
					{
						String path = en.getPath();
						File f = new File(destDir, path);

						// check if exists
						if(!force)
						{
							if(f.exists())
							{
								throw new UserException("File exists: " + f);
							}
						}

						long len = en.getFileLength();
						log.log("EXTRACT", "file", f, "length", len);
						
						byte[] hash = decryptFile(f, rf, destDir, buffer, len);
						
						// compare hash
						if(!Arrays.equals(en.getHash(), hash))
						{
							// TODO delete file?
							throw new UserException("hash mismatch file=" + en); // TODO file path
						}
					}
				}
			}
		}
		finally
		{
			CKit.close(rf);
		}
	}


	private static Future<KeyMaterial> generateKey(Logger log, String pass, byte[] storedRandomness)
	{
		CompletableFuture<KeyMaterial> f = new CompletableFuture();
		
		new Thread("generating key")
		{
			public void run()
			{
				long start = System.nanoTime();
				KeyMaterial km = KeyMaterial.generate(pass, storedRandomness);
				f.complete(km);
				log.log(() ->
				{
					log.log("KEY generated", "elapsed", (System.nanoTime() - start)/1_000_000_000); 	
				});
			}
		}.start();

		return f;
	}
	
	
	private static byte[] encryptFile(File f, XSalsaRandomAccessFile rf, byte[] buf) throws Exception
	{
		try(InputStream in = new BufferedInputStream(new FileInputStream(f)))
		{
			try(OutputStream out = new OutputStreamWrapper(rf))
			{
				return copyWithDigest(in, out, buf);
			}
		}
	}
	
	
	private static byte[] decryptFile(File f, XSalsaRandomAccessFile rf, File destDir, byte[] buf, long len) throws Exception
	{
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
	
	
	private static String formatLength(long len)
	{
		return numberFormatter.format(len);
	}
	
	
	private static String formatDate(long time)
	{
		LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		return dateFormatter.format(t);
	}
	
	
	private static String formatFileEntry(HeaderEntry en, int width)
	{
		String len = formatLength(en.getFileLength());
		String dat = formatDate(en.getLastModified());
		String path = en.getPath();
		
		SB sb = new SB();
		sb.sp(width - len.length());
		sb.append(len);
		sb.sp();
		sb.append(dat);
		sb.sp(2);
		sb.append(path);
		return sb.toString();
	}
}
