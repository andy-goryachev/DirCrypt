// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CKit;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.bc.Blake2bDigest;
import goryachev.memsafecrypto.bc.SCrypt;
import java.security.SecureRandom;


/**
 * Key Material.
 */
public class KeyMaterial
{
	public final CByteArray key;
	public final CByteArray salt;
	public final CByteArray iv;
	public final Exception error;
	
	
	public KeyMaterial(CByteArray key, CByteArray salt, CByteArray iv, Exception error)
	{
		this.key = key;
		this.salt = salt;
		this.iv = iv;
		this.error = error;
	}
	
	
	public static KeyMaterial generate(String pass, byte[] storedRandomness)
	{
		try
		{
			byte[] b = storedRandomness;
			if(b == null)
			{
				b = new byte[FileFormatV1.IV_SIZE_BYTES];
				SecureRandom rnd = new SecureRandom();
				rnd.nextBytes(b);
			}
			CByteArray iv = CByteArray.readOnly(b);
			
			// salt = hashed iv
			CByteArray salt = hash(b);
			
			CByteArray pw = CByteArray.readOnly(pass.getBytes(CKit.CHARSET_UTF8));
			
			int n = FileFormatV1.SCRYPT_N;
			int r = FileFormatV1.SCRYPT_R;
			int p = FileFormatV1.SCRYPT_P;
			
			// generate key with scrypt
			// P - passphrase
			// S - salt
			// N - cpu/memory cost
			// r - block mix size parameter
			// p - parallelization parameter
			CByteArray k = SCrypt.generate(pw, salt, n, r, p, FileFormatV1.KEY_SIZE_BYTES);
			return new KeyMaterial(k, salt, iv, null);
		}
		catch(Exception e)
		{
			return new KeyMaterial(null, null, null, new Exception(e));
		}
		catch(Throwable e)
		{
			return new KeyMaterial(null, null, null, new Exception(e));
		}
	}
	
	
	private static CByteArray hash(byte[] b)
	{
		byte[] sig = toByteArray(FileFormatV1.SIGNATURE);

		Blake2bDigest d = new Blake2bDigest(FileFormatV1.SALT_SIZE_BYTES * 8);
		d.update(sig, 0, sig.length);
		d.update(b, 0, b.length);
		
		CByteArray out = new CByteArray(FileFormatV1.SALT_SIZE_BYTES);
		d.doFinal(out, 0);
		return out;
	}


	private static byte[] toByteArray(long x)
	{
		byte[] b = new byte[8];
		b[0] = (byte)(x >> 56);
		b[1] = (byte)(x >> 48);
		b[2] = (byte)(x >> 40);
		b[3] = (byte)(x >> 32);
		b[4] = (byte)(x >> 24);
		b[5] = (byte)(x >> 16);
		b[6] = (byte)(x >>  8);
		b[7] = (byte)(x);
		return b;
	}
}
