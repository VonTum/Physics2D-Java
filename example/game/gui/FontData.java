package game.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontData {
	
	public final Map<Character, CharDisplayData> charData;
	public final String[] pages;
	public final Map<CharPair, Integer> kernings;
	public final int size, stretchH, lineHeight, base, scaleW, scaleH, pageCount;
	public final boolean bold, italic, unicode, smooth, aa, packed;
	public final String face, charset;
	public final int[] padding, spacing;
	
	private FontData(Map<Character, CharDisplayData> charData, String[] pages,
			Map<CharPair, Integer> kernings, int size, int stretchH,
			int lineHeight, int base, int scaleW, int scaleH, int pageCount,
			boolean bold, boolean italic, boolean unicode, boolean smooth,
			boolean aa, boolean packed, String face, String charset,
			int[] padding, int[] spacing) {
		
		this.charData = charData;
		this.pages = pages;
		this.kernings = kernings;
		this.size = size;
		this.stretchH = stretchH;
		this.lineHeight = lineHeight;
		this.base = base;
		this.scaleW = scaleW;
		this.scaleH = scaleH;
		this.pageCount = pageCount;
		this.bold = bold;
		this.italic = italic;
		this.unicode = unicode;
		this.smooth = smooth;
		this.aa = aa;
		this.packed = packed;
		this.face = face;
		this.charset = charset;
		this.padding = padding;
		this.spacing = spacing;
	}

	/**
	 * Parses a .fnt file and returns a new FontData with it's contents
	 * @param fontFile the URL to the font file
	 * @return FontData
	 * @throws IOException
	 */
	public static FontData parseFont(URL fontFile) throws IOException {
		
		try(InputStream inStream = fontFile.openStream(); 
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream))){
			
			final Map<Character, CharDisplayData> charData = new HashMap<>();
			final String[] pages;
			final Map<CharPair, Integer> kernings = new HashMap<>();
			
			HashMap<String, String> infoArgs = getArgs(reader.readLine().split("\\s+", 2)[1]);
			
			String face = getString(infoArgs, "face");
			int size = getInt(infoArgs, "size");
			boolean bold = getBoolean(infoArgs, "bold");
			boolean italic = getBoolean(infoArgs, "italic");
			String charset = getString(infoArgs, "charset");
			boolean unicode = getBoolean(infoArgs, "unicode");
			int stretchH = getInt(infoArgs, "stretchH");
			boolean smooth = getBoolean(infoArgs, "smooth");
			boolean aa = getBoolean(infoArgs, "aa");
			int[] padding = getIntArray(infoArgs, "padding");
			int[] spacing = getIntArray(infoArgs, "spacing");
			
			
			HashMap<String, String> commonArgs = getArgs(reader.readLine().split("\\s+", 2)[1]);
			int lineHeight = getInt(commonArgs, "lineHeight");
			int base = getInt(commonArgs, "base");
			int scaleW = getInt(commonArgs, "scaleW");
			int scaleH = getInt(commonArgs, "scaleH");
			int pageCount = getInt(commonArgs, "pages");
			boolean packed = getBoolean(commonArgs, "packed");
			
			
			pages = new String[pageCount];
			
			do{
				// foreach page
				String[] line = reader.readLine().split("\\s+", 2);
				String header = line[0];
				String args = line[1];
				HashMap<String, String> a = getArgs(args);
				
				switch(header){
				case "page":
					int id = getInt(a, "id");
					String file = getString(a, "file");
					pages[id] = file;
					break;
				case "char":
					char c = (char) getInt(a, "id");
					int x = getInt(a, "x");
					int y = getInt(a, "y");
					int w = getInt(a, "width");
					int h = getInt(a, "height");
					int xoffset = getInt(a, "xoffset");
					int yoffset = getInt(a, "yoffset");
					int xadvance = getInt(a, "xadvance");
					int page = getInt(a, "page");
					
					charData.put(c, new CharDisplayData(x, y, w, h, xoffset, yoffset, xadvance, page));
					break;
				case "kerning":
					char first = (char) getInt(a, "first");
					char second = (char) getInt(a, "first");
					int amount = getInt(a, "amount");
					
					kernings.put(new CharPair(first, second), amount);
					break;
				}
			}while(reader.ready());
			return new FontData(charData, pages, kernings, size, stretchH, lineHeight, base, scaleW, scaleH, pageCount, bold, italic, unicode, smooth, aa, packed, face, charset, padding, spacing);
		}
	}
	
	private static final Pattern spacePattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	private static HashMap<String, String> getArgs(String argList){
		Matcher m = spacePattern.matcher(argList);
		HashMap<String, String> args = new HashMap<>();
		while (m.find()){
			String[] pair = m.group(1).split("=", 2);
			args.put(pair[0], pair[1]);
		}
		
		return args;
	}
	
	private static int getInt(HashMap<String, String> args, String name){
		return Integer.parseInt(args.get(name));
	}
	
	private static String getString(HashMap<String, String> args, String name){
		return args.get(name).replaceAll("\"", "");
	}
	
	private static boolean getBoolean(HashMap<String, String> args, String name){
		return "1".equals(args.get(name));
	}
	
	private static int[] getIntArray(HashMap<String, String> args, String name){
		String[] parts = args.get(name).split(",");
		int[] values = new int[parts.length];
		for(int i = 0; i < parts.length; i++)
			values[i] = Integer.parseInt(parts[i]);
		
		return values;
	}
	
	public static final class CharDisplayData {
		public final int x, y, w, h, xoffset, yoffset, xadvance, page;
		
		public CharDisplayData(int x, int y, int w, int h, int xoffset, int yoffset, int xadvance, int page) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.xadvance = xadvance;
			this.page = page;
		}
	}
	
	public static final class CharPair{
		public final char first, second;
		
		public CharPair(char first, char second){
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean equals(Object other){
			return (other instanceof CharPair)? equals((CharPair) other):false;
		}
		
		public boolean equals(CharPair other){
			return this.first == other.first && this.second == other.second;
		}
		
		@Override
		public int hashCode(){
			return Character.hashCode(first) ^ Character.hashCode(second);
		}
		
		@Override
		public String toString(){
			return String.format("{%s%s}", first, second);
		}
	}
}
