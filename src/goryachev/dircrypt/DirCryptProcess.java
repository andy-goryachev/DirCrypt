// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import java.io.File;
import java.util.List;


/**
 * Dir Crypt Process.
 */
public class DirCryptProcess
{
	// TODO passphrase
	public static void encrypt(Logger log, List<File> dirs, File outFile) throws Exception
	{
		FileScanner fs = new FileScanner(log, dirs);
		Header h = fs.scan();
		
		// TODO
		// create temp file
		// write random
		// write signature
		// store offset
		// write header-size byte array
		// write files
		// set offset
		// write header
		// check cur.offset == offset + header.size
		// rename
	}


	// TODO passphrase
	public static void decrypt(Logger log, File inputFile) throws Exception
	{
		// TODO
		// read random
		// read signature
		// validate signature
		// read header
		// validate header
		// read files
	}
}
