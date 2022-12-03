// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import java.io.File;
import java.util.List;


/**
 * Tests File Scanner.
 */
public class TestFileScanner
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void load() throws Exception
	{
		File dir = new File(".");
		Header h = new FileScanner(List.of(dir)).scan();
		TF.notNull(h);
	}
}
