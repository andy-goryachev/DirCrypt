// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;


/**
 * Tests POSIX Attributes.
 */
public class TestPosix
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	//@Test
	public void posix() throws Exception
	{
		// great, does not work on windows
		File f = new File(".");
		Path p = f.toPath();
		Set<PosixFilePermission> a = Files.getPosixFilePermissions(p);
		D.print(a);
	}
}
