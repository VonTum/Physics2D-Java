package game.gui;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import math.Vec2;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import util.Dimentions;
import de.matthiasmann.twl.utils.PNGDecoder;

public class Font {
	
	public final int id;
	public final int textureWidth;
	public final int textureHeight;
	
	private final int[] char_widths;
	
	public static final int TAB_SIZE = 3;
	
	public Font(URL inputURL) throws IOException {
		this(inputURL, GL11.GL_LINEAR);
	}

	public Font(URL inputURL, int filter) throws IOException {
		this(inputURL, filter, GL11.GL_REPEAT);
	}

	public Font(URL inputURL, int filter, int wrap) throws IOException {
		try(InputStream input = inputURL.openStream()) {
			
			//initialize the decoder
			PNGDecoder dec = new PNGDecoder(input);

			//set up image dimensions 
			textureWidth = dec.getWidth();
			textureHeight = dec.getHeight();
			
			//we are using RGBA, i.e. 4 components or "bytes per pixel"
			final int bpp = 4;
			
			//create a new byte buffer which will hold our pixel data
			ByteBuffer buf = BufferUtils.createByteBuffer(bpp * textureWidth * textureHeight);
			
			//decode the image into the byte buffer, in RGBA format
			dec.decode(buf, textureWidth * bpp, PNGDecoder.Format.RGBA);
			
			//flip the buffer into "read mode" for OpenGL
			buf.flip();
			
			// initialize char widths
			int char_width = getCharWidthInPixels();
			int char_height = getCharHeightInPixels();
			
			char_widths = new int[256];
			
			for(int row = 0; row < 16; row++){
				for(int col = 0; col < 16; col++){
					int furthest_x = 0;
					for(int y = 0; y < char_height; y++){
						for(int x = furthest_x+1; x < char_width; x++){
							int bufIndex = (row*char_height+y)*textureWidth + col*char_width+x;
							if(buf.get(bufIndex*4) != 0)
								furthest_x = x;
						}
					}
					char_widths[row*16+col] = furthest_x+1;
				}
			}
			
			char_widths[asByte(' ')] = 2;
			
			
			//enable textures and generate an ID
			glEnable(GL_TEXTURE_2D);
			id = glGenTextures();

			//bind texture
			bind();

			//setup unpack mode
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			//setup parameters
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
			
			//pass RGBA data to OpenGL
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
			
			unbind();
		}
	}
	
	public void bind(){
		glBindTexture(GL_TEXTURE_2D, id);
	}
	public void unbind(){
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void delete(){
		glDeleteTextures(id);
	}
	
	public void drawString(String text, float fontSize, float lineSpacing, float x, float y){
		Text t = new Text(text, fontSize, lineSpacing);
		t.draw(x, y);
	}
	
	public void drawString(String text, float fontSize, float lineSpacing, Vec2 pos){
		Text t = new Text(text, fontSize, lineSpacing);
		t.draw(pos);
	}
	
	private void drawChar(char c, float x, float y, float dx, float dy){
		byte bc = (byte) c;
		
		byte firstFourBits = (byte) (bc >> 4);
		byte lastFourBits = (byte) (bc & 0x0F);
		
		float u = lastFourBits / 16f - 1f/textureWidth;
		float v = firstFourBits / 16f - 1f/textureHeight;
		float u2 = u+1f/16;
		float v2 = v+1f/16;
		
		glBegin(GL_QUADS);
			glTexCoord2f(u, v);
			glVertex2f(x, y);
			glTexCoord2f(u, v2);
			glVertex2f(x, y - dy);
			glTexCoord2f(u2, v2);
			glVertex2f(x + dx, y - dy);
			glTexCoord2f(u2, v);
			glVertex2f(x + dx, y);
		glEnd();
	}
	
	
	
	private int getWidthOfChar(char c){
		return char_widths[asByte(c)];
	}
	
	private int asByte(char c){
		int b = (byte) c;
		if(b < 0)
			return b+256;
		else
			return b;
	}
	
	private float getNextTabTarget(float curX, float actualFontSize){
		return (float) (Math.ceil(curX / (actualFontSize*TAB_SIZE))+0.1f) * actualFontSize*TAB_SIZE;
	}
	
	private float computeNextXPos(float curX, char c, float actualFontSize){
		switch(c){
		case '\n':
			return 0;
		case '\t':
			return getNextTabTarget(curX, actualFontSize);
		default:
			return curX+(actualFontSize * (getWidthOfChar(c)+1)*16)/this.textureWidth;
		}
	}
	
	private int getCharHeightInPixels(){return textureHeight / 16;}
	private int getCharWidthInPixels(){return textureWidth / 16;}
	
	public Text createText(String text, float fontSize, float lineSpacing){
		return new Text(text, fontSize, lineSpacing);
	}
	
	public class Text {
		private final float fontSize;
		private final float lineSpacing;
		private final String text;
		
		public Text(String text, float fontSize, float lineSpacing){
			this.text = text;
			this.fontSize = fontSize;
			this.lineSpacing = lineSpacing;
		}
		
		public void draw(Vec2 position){
			draw((float) position.x, (float) position.y);
		}
		
		public void drawRightAligned(Vec2 position){
			drawRightAligned((float) position.x, (float) position.y);
		}
		
		public void drawRightAligned(float x, float y) {
			float actualFontSize = getActualFontSize();
			String[] lines = text.split("\n");
			bind();
			for(String line:lines){
				float actualX = x-getLineLength(line);
				drawLine(line, actualX, y, actualFontSize);
				y -= actualFontSize + getActualLineSpacing();
			}
			unbind();
		}

		public void draw(float x, float y){
			float actualFontSize = getActualFontSize();
			String[] lines = text.split("\n");
			bind();
			for(String line:lines){
				drawLine(line, x, y, actualFontSize);
				y -= actualFontSize + getActualLineSpacing();
			}
			unbind();
		}
		
		private void drawLine(String line, float x, float y, float actualFontSize){
			float curX = 0;
			for(char c:line.toCharArray()){
				switch(c){
				case '\t':
					curX = getNextTabTarget(curX, actualFontSize);
					break;
				default:
					float w = (actualFontSize * (getWidthOfChar(c)+1)*16)/textureWidth;
					drawChar(c, curX+x, y, actualFontSize, actualFontSize);
					curX += w;
					break;
				}
			}
		}
		
		private float getLineLength(String line){
			float actualFontSize = getActualFontSize();
			
			float curX = 0;
			for(char c:line.toCharArray())
				curX = computeNextXPos(curX, c, actualFontSize);
			
			return curX - (actualFontSize*16)/textureWidth;
		}
		
		public Dimentions getTextDimentions(){
			float maxWidth = 0;
			
			String[] lines = text.split("\n");
			for(String line:lines)
				maxWidth = Math.max(maxWidth, getLineLength(line));
			
			return new Dimentions(maxWidth, lines.length*getActualFontSize()+(lines.length-1)*getActualLineSpacing());
		}
		
		public float getActualFontSize(){
			double screenHeight = Screen.getWindowSize().height;
			double actualFontSize = fontSize / screenHeight;
			return (float) actualFontSize;
		}
		
		public float getActualLineSpacing(){
			double screenHeight = Screen.getWindowSize().height;
			double actualLineSpacing = lineSpacing / screenHeight;
			return (float) actualLineSpacing;
		}
	}
}
