// Copyright © 2022-2023 Andy Goryachev <andy@goryachev.com>
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
	public boolean verify;
	public boolean usage;
	public boolean ignoreErrors;
	public String N;
	public String R;
	public String P;
	public boolean version;
	
	
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
				case "--ignoreErrors":
					a.ignoreErrors = true;
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
				case "--scryptN":
					a.N = p.nextToken();
					break;
				case "--scryptP":
					a.P = p.nextToken();
					break;
				case "--scryptR":
					a.R = p.nextToken();
					break;
				case "--verbose":
					a.verbose = true;
					break;
				case "--verify":
					a.verify = true;
					break;
				case "--version":
					a.version = true;
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
			"  java -jar DirCrypt.jar --enc --out FILE [options] DIR1 DIR2 ...\n" +
			"Decrypt:\n" +
			"  java -jar DirCrypt.jar --dec --in FILE --dest OUTDIR [options]\n" +
			"List Contents:\n" +
			"  java -jar DirCrypt.jar --list --in FILE [options]\n" +
			"Verify Contents:\n" +
			"  java -jar DirCrypt.jar --verify --in FILE [options]\n" +
			"Options:\n" +
			"  --dest DESTINATION_DIRECTORY\n" +
			"  --force - overwrite files\n" +
			"  --help\n" +
			"  --ignoreErrors - restore corrupted files\n" +
			"  --in INPUT_FILE\n" +
			"  --list - lists file contents\n" +
			"  --out OUTPUT_FILE\n" +
			"  --pass PASSPHRASE\n" +
			"  --scryptN VALUE - scrypt N parameter (default=" + FileFormatV1.SCRYPT_N + ")\n" +
			"  --scryptP VALUE - scrypt P parameter (default=" + FileFormatV1.SCRYPT_P + ")\n" +
			"  --scryptR VALUE - scrypt R parameter (default=" + FileFormatV1.SCRYPT_R + ")\n" +
			"  --verbose - logs diagnostic messages to stdout\n" +
			"  --version - prints version string\n" +
			"";
	}
}
