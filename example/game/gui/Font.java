package game.gui;

import static org.lwjgl.opengl.GL30.GL_QUADS;
import static org.lwjgl.opengl.GL30.GL_RGBA;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30.glBegin;
import static org.lwjgl.opengl.GL30.glBindTexture;
import static org.lwjgl.opengl.GL30.glDeleteTextures;
import static org.lwjgl.opengl.GL30.glEnable;
import static org.lwjgl.opengl.GL30.glEnd;
import static org.lwjgl.opengl.GL30.glGenTextures;
import static org.lwjgl.opengl.GL30.glTexCoord2i;
import static org.lwjgl.opengl.GL30.glTexImage2D;
import static org.lwjgl.opengl.GL30.glTexParameteri;
import static org.lwjgl.opengl.GL30.glVertex2f;
import static org.lwjgl.opengl.GL30.glMatrixMode;
import static org.lwjgl.opengl.GL30.glScaled;
import static org.lwjgl.opengl.GL30.GL_TEXTURE;
import static org.lwjgl.opengl.GL30.GL_MODELVIEW;
import game.gui.FontData.CharDisplayData;
import game.util.Dimentions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import physics2D.math.Vec2;
import de.matthiasmann.twl.utils.PNGDecoder;

public class Font {
	
	public final int id;
	public final int textureWidth;
	public final int textureHeight;
	
	public final FontData font;
	
	public static final int TAB_SIZE = 3;
	
	public Font(URL fntURL) throws IOException {
		long startTime = System.currentTimeMillis();
		this.font = FontData.parseFont(fntURL);
		System.out.printf("Font load time: %dms\n", System.currentTimeMillis()-startTime);
		
		// print the parsed data
		System.out.println(font.toString().replaceAll("},", "},\n"));
		
		URL imgURL = new URL(fntURL, font.pages[0]);
		System.out.println(fntURL);
		System.out.println(imgURL);
		
		try(InputStream input = imgURL.openStream()) {
			
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
			
			//enable textures and generate an ID
			glEnable(GL_TEXTURE_2D);
			id = glGenTextures();
			
			//bind texture
			bind();
			
			//pass RGBA data to OpenGL
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
			
			GL30.glGenerateMipmap(GL_TEXTURE_2D);
			
			//setup parameters
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
			
			unbind();
			
			glMatrixMode(GL_TEXTURE);
			glScaled(1f/textureWidth, 1f/textureHeight, 1.0);
			glMatrixMode(GL_MODELVIEW);
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
		float actualFontSize = getActualFontSize(fontSize);
		float actualLineSpacing = getActualLineSpacing(lineSpacing);
		String[] lines = text.split("\n");
		bind();
		for(String line:lines){
			drawLine(line, x, y, actualFontSize);
			y -= actualFontSize + actualLineSpacing;
		}
		unbind();
	}
	
	public void drawStringRightAligned(String text, float fontSize, float lineSpacing, float x, float y) {
		float actualFontSize = getActualFontSize(fontSize);
		float actualLineSpacing = getActualLineSpacing(lineSpacing);
		String[] lines = text.split("\n");
		bind();
		for(String line:lines){
			drawLineRightAligned(line, x, y, actualFontSize);
			y -= actualFontSize + actualLineSpacing;
		}
		unbind();
	}
	
	public void drawStringBottom(String text, float fontSize, float lineSpacing, float x, float y){
		float actualFontSize = getActualFontSize(fontSize);
		float actualLineSpacing = getActualLineSpacing(lineSpacing);
		float textHeight = getTextHeightActuals(text, actualFontSize, actualLineSpacing);

		drawString(text, fontSize, lineSpacing, x, y+textHeight);
	}
	
	public void drawStringRightAlignedBottom(String text, float fontSize, float lineSpacing, float x, float y){
		float actualFontSize = getActualFontSize(fontSize);
		float actualLineSpacing = getActualLineSpacing(lineSpacing);
		float textHeight = getTextHeightActuals(text, actualFontSize, actualLineSpacing);

		drawStringRightAligned(text, fontSize, lineSpacing, x, y+textHeight);
	}
	
	private void drawLine(String s, float xpos, float ypos, float height){
		float x = xpos;
		float y = ypos;
		
		float f2sCoords = height / font.lineHeight;
		
		char lastChar = '\n';
		
		for(char c:s.toCharArray()){
			if(c == '\t'){
				x = getNextTabTarget(x, height);
				lastChar = c;
				continue;
			}
			CharDisplayData data = font.charData.get(c);
			
			x += font.kernings.getOrDefault(new FontData.CharPair(lastChar, c), 0)*f2sCoords;
			
			int u = data.x;
			int v = data.y;
			
			float w = data.w*f2sCoords;
			float h = data.h*f2sCoords;
			
			drawTexturedRectangle(x+data.xoffset*f2sCoords, y-data.yoffset*f2sCoords, w, h, u, v, data.w, data.h);
			
			x += data.xadvance*f2sCoords;
			lastChar = c;
		}
	}
	
	private void drawLineRightAligned(String s, float xpos, float ypos, float height){
		drawLine(s, xpos-getLineLength(s, height), ypos, height);
	}
	
	private float getNextTabTarget(float curX, float actualFontSize){
		return (float) (Math.ceil(curX / (actualFontSize*TAB_SIZE))+0.1f) * actualFontSize*TAB_SIZE;
	}
	
	public float getActualFontSize(float fontSize){
		double screenHeight = Screen.getWindowSize().height;
		double actualFontSize = fontSize / screenHeight;
		return (float) actualFontSize;
	}
	
	public float getActualLineSpacing(float lineSpacing){
		double screenHeight = Screen.getWindowSize().height;
		double actualLineSpacing = lineSpacing / screenHeight;
		return (float) actualLineSpacing;
	}
	
	public Dimentions getTextDimentions(String text, float fontSize, float lineSpacing){
		float actualFontSize = getActualFontSize(fontSize);
		
		String[] lines = text.split("\n");
		
		return new Dimentions(getTextWidth(text, actualFontSize), lines.length*actualFontSize+(lines.length-1)*getActualLineSpacing(lineSpacing));
	}
	
	public float getTextHeight(String text, float fontSize, float lineSpacing){
		return getTextHeightActuals(text, getActualFontSize(fontSize), getActualLineSpacing(lineSpacing));
	}
	
	public float getTextWidth(String text, float fontSize){
		return getTextWidthActualFontSize(text, getActualFontSize(fontSize));
	}
	
	private float getTextHeightActuals(String text, float actualFontSize, float actualLineSpacing){
		int newLineCount = 0; for(char c:text.toCharArray()) if(c == '\n') newLineCount++;
		
		return (newLineCount+1)*actualFontSize+newLineCount*actualLineSpacing;
		
	}
	
	private float getTextWidthActualFontSize(String text, float actualFontSize){
		float maxWidth = 0;
		
		String[] lines = text.split("\n");
		for(String line:lines)
			maxWidth = Math.max(maxWidth, getLineLength(line, actualFontSize));
		
		return maxWidth;
	}
	
	public void drawString(String text, float fontSize, float lineSpacing, Vec2 pos){drawString(text, fontSize, lineSpacing, (float) pos.x, (float) pos.y);}
	public void drawStringRightAligned(String text, float fontSize, float lineSpacing, Vec2 position){drawStringRightAligned(text, fontSize, lineSpacing, (float) position.x, (float) position.y);}
	
	private float getLineLength(String line, float actualFontSize){
		float f2sCoords = actualFontSize / font.lineHeight;
		
		float curX = 0;
		char lastChar = '\n';
		for(char c:line.toCharArray()){
			curX += (font.charData.get(c).xadvance+font.kernings.getOrDefault(new FontData.CharPair(lastChar, c), 0))*f2sCoords;
			lastChar = c;
		}
		
		return curX;
	}
	
	/**
	 * Draws a textured rectangle at screen pos x, y. bottom corner
	 * 
	 * u and v are in texture coords, integers
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param u
	 * @param v
	 * @param du
	 * @param dv
	 */
	private void drawTexturedRectangle(float x, float y, float w, float h, int u, int v, int du, int dv){
		glBegin(GL_QUADS);
		glTexCoord2i(u, v);
		glVertex2f(x, y);
		glTexCoord2i(u, v+dv);
		glVertex2f(x, y-h);
		glTexCoord2i(u+du, v+dv);
		glVertex2f(x+w, y-h);
		glTexCoord2i(u+du, v);
		glVertex2f(x+w, y);
		glEnd();
	}
}
