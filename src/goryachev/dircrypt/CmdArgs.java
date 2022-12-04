// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;
import goryachev.common.util.CList;
import goryachev.common.util.UserException;


/**
 * Command Arguments Parser.
 */
public class CmdArgs
{
	public final CList<String> dirs = new CList();
	public boolean encrypt;
	public boolean decrypt;
	public String inputFile;
	public String outputDir;
	public String passPhrase;
	public boolean verbose;
	
	
	private CmdArgs()
	{
	}
	
	
	public static CmdArgs parse(String[] args) throws UserException
	{
		CommandLineArgumentsParser p = new CommandLineArgumentsParser(args);
		CmdArgs a = new CmdArgs();

		String tok;
		while((tok = p.nextToken()) != null)
		{
			if(tok.startsWith("--"))
			{
				switch(tok)
				{
				case "--dec":
					a.decrypt = true;
					break;
				case "--enc":
					a.encrypt = true;
					break;
				case "--inp":
					a.inputFile = p.nextToken();
					break;
				case "--out":
					a.outputDir = p.nextToken();
					break;
				case "--pass":
					a.passPhrase = p.nextToken();
					break;
				case "--verbose":
					a.verbose = true;
					break;
				default:
					throw new UserException("Unrecognized option: " + tok);
				}
			}
			else
			{
				a.dirs.add(tok);
			}
		}
		return a;
	}
	
	
	public static String usage()
	{
		return
			"Usage:\n" +
			"Encrypt:\n" +
			"  java -jar dirCrypt.jar --enc --out FILE [options] dir1 dir2 ...\n" +
			"Decrypt:\n" +
			"  java -jar dirCrypt.jar --dec --in FILE [options]\n" +
			"Options:\n" +
			"  --in INPUT_FILE\n" +
			"  --out OUTPUT_DIRECTORY\n" +
			"  --pass PASSPHRASE\n" +
			"  --verbose log messages to stdout\n" +
			"";
	}


	public void validate()
	{
		// TODO
	}
}
