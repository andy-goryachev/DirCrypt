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
	public boolean force;
	public String inputFile;
	public String outputFile;
	public String destination;
	public String passPhrase;
	public boolean listing;
	public boolean verbose;
	public boolean usage;
	
	
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
				case "--dest":
					a.destination = p.nextToken();
					break;
				case "--enc":
					a.encrypt = true;
					break;
				case "--force":
					a.force = true;
					break;
				case "--help":
					a.usage = true;
					break;
				case "--in":
					a.inputFile = p.nextToken();
					break;
				case "--list":
					a.listing = true;
					break;
				case "--out":
					a.outputFile = p.nextToken();
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
			"USAGE\n" +
			"Encrypt:\n" +
			"  java -jar DirCrypt.jar --enc --out FILE [options] dir1 dir2 ...\n" +
			"Decrypt:\n" +
			"  java -jar DirCrypt.jar --dec --in FILE [options] --dest OUTDIR\n" +
			"List Contents:\n" +
			"  java -jar DirCrypt.jar --list --in FILE --list\n" +
			"Options:\n" +
			"  --dest DESTINATION_DIRECTORY\n" +
			"  --help\n" +
			"  --in INPUT_FILE\n" +
			"  --list list file contents\n" +
			"  --out OUTPUT_FILE\n" +
			"  --pass PASSPHRASE\n" +
			"  --verbose log diagnostic messages to stdout\n" +
			"";
	}
}
