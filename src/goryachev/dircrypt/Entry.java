// Copyright © 2022 Andy Goryachev <andy@goryachev.com>
package goryachev.dircrypt;


/**
 * Header Entry.
 */
public class Entry
{
	public final EntryType type;
	public final String name;
	public final long length;
	public final long modified;
	public final short attributes;
	private transient Entry parent;
	
	
	public Entry(EntryType t, String name, long length, long modified, short attributes)
	{
		this.type = t;
		this.name = name;
		this.length = length;
		this.modified = modified;
		this.attributes = attributes;
	}
	
	
	public String toString()
	{
		return type + (name == null ? "" : " " + name);
	}
}
