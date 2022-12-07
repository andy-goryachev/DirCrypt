// Copyright © 2020-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.Crypto;
import goryachev.memsafecrypto.OpaqueBytes;
import goryachev.memsafecrypto.OpaqueChars;
import goryachev.memsafecrypto.bc.Blake2bDigest;
import goryachev.memsafecrypto.bc.SCrypt;
import goryachev.memsafecrypto.salsa.XSalsaTools;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;


/**
 * File Format Version 1 Constants.
 */
public final class FileFormatV1
{
	public static final long SIGNATURE = 0x1DEA_2022_1204_1420L;
	
	public static final int KEY_SIZE_BYTES = 256/8;
	public static final int IV_SIZE_BYTES = 192/8;
	public static final int SALT_SIZE_BYTES = 256/8;

	// TODO increase!
	/** cpu/memory cost */
//	public static final int SCRYPT_N = 16384;
	/** block mix size parameter */
//	public static final int SCRYPT_R = 8;
	/** parallelization parameter */
//	public static final int SCRYPT_P = 32;
	
	// FIX remove, for debugging only
	public static final int SCRYPT_N = 1024;
	public static final int SCRYPT_R = 1;
	public static final int SCRYPT_P = 2;
	
	public static final int TYPE_DIR = 0x00;
	public static final int TYPE_END = 0x01;
	public static final int TYPE_FILE = 0x02;
	public static final int FILE_HASH_SIZE_BITS = 256;
	public static final int FILE_HASH_SIZE_BYTES = FILE_HASH_SIZE_BITS/8;
}
