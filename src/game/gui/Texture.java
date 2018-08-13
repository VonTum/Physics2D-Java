/**
 * Copyright (c) 2012, Matt DesLauriers All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met: 
 *
 *	* Redistributions of source code must retain the above copyright notice, this
 *	  list of conditions and the following disclaimer. 
 *
 *	* Redistributions in binary
 *	  form must reproduce the above copyright notice, this list of conditions and
 *	  the following disclaimer in the documentation and/or other materials provided
 *	  with the distribution. 
 *
 *	* Neither the name of the Matt DesLauriers nor the names
 *	  of his contributors may be used to endorse or promote products derived from
 *	  this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *	ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 *	LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *	CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *	SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *	POSSIBILITY OF SUCH DAMAGE.
 */
package game.gui;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexCoord2i;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import de.matthiasmann.twl.utils.PNGDecoder;

public class Texture {
	static long window;
	
	public static void main(String[] args) throws IOException{
		if(!glfwInit()){
			System.out.println("GLFW Failed to initialize");
			System.exit(1);
		}
		
		GLFWErrorCallback.createPrint(System.err).set();
		
		// long monitor = GLFW.glfwGetMonitors().address(0); // get secondary monitor
		
		window = glfwCreateWindow(920, 920, "Test Prog", 0, 0);
		
		// TODO debug, remove
		GLFW.glfwSetWindowPos(window, -1000, 50);
		
		glfwShowWindow(window);
		
		glfwMakeContextCurrent(window);
		
		GL.createCapabilities();
		
		glClearColor(0.7f,  0.8f, 1.0f, 0.0f);
		
		glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// Texture t = new Texture(Texture.class.getResource("/ascii.png"), GL_NEAREST);
		
	    Font f = new Font(Texture.class.getResource("/ascii.png"), GL_NEAREST);
	    
	    
		while(!GLFW.glfwWindowShouldClose(window)){
			glfwPollEvents();
			glClear(GL_COLOR_BUFFER_BIT);
			
			// debugTexture(t, 0, 0, 1f, -1f);
			
			glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
			Font.Text t = f.new Text("Status:\tAnnihilating\nTime:\tImminent", 96f, 10f);
			t.draw(-0.97f, 0.10001f);
			// f.drawString("Status:\tAnnihilating\nTime:\tImminent", -0.97f, 0.10001f, 96f);
			
			glfwSwapBuffers(window);
		}
		
		f.delete();
	}
	
	public static void debugTexture(Texture tex, float x, float y, float width, float height) {
		//usually glOrtho would not be included in our game loop
		//however, since it's deprecated, let's keep it inside of this debug function which we will remove later
		/*glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		// glOrtho(0, 920, 920, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();*/
		// glEnable(GL_TEXTURE_2D); //likely redundant; will be removed upon migration to "modern GL"
		
		//glMatrixMode(GL_TEXTURE);
		//glScalef(1f/tex.width, 1f/tex.height, 1f);
		//glMatrixMode(GL_MODELVIEW);
		
		//bind the texture before rendering it
		tex.bind();
		
		//setup our texture coordinates
		//(u,v) is another common way of writing (s,t)
		float u = 0f;
		float v = 0f;
		float u2 = 1f;
		float v2 = 1f;
		
		//immediate mode is deprecated -- we are only using it for quick debugging
		glColor4f(1f, 1f, 1f, 1f);
		glBegin(GL_QUADS);
			glTexCoord2f(u, v);
			glVertex2f(x, y);
			glTexCoord2f(u, v2);
			glVertex2f(x, y + height);
			glTexCoord2f(u2, v2);
			glVertex2f(x + width, y + height);
			glTexCoord2f(u2, v);
			glVertex2f(x + width, y);
		glEnd();
	}
	
	public final int target = GL_TEXTURE_2D;
	public final int id;
	public final int width;
	public final int height;
	
	private final int[] char_widths;
	
	public static final int LINEAR = GL_LINEAR;
	public static final int NEAREST = GL_NEAREST;

	public static final int CLAMP = GL_CLAMP;
	public static final int CLAMP_TO_EDGE = GL_CLAMP_TO_EDGE;
	public static final int REPEAT = GL_REPEAT;

	public Texture(URL input) throws IOException {
		this(input, GL_NEAREST);
	}

	public Texture(URL input, int filter) throws IOException {
		this(input, filter, GL_CLAMP_TO_EDGE);
	}

	public Texture(URL inputURL, int filter, int wrap) throws IOException {
		try(InputStream input = inputURL.openStream()) {
			
			//initialize the decoder
			PNGDecoder dec = new PNGDecoder(input);

			//set up image dimensions 
			width = dec.getWidth();
			height = dec.getHeight();
			
			//we are using RGBA, i.e. 4 components or "bytes per pixel"
			final int bpp = 4;
			
			//create a new byte buffer which will hold our pixel data
			ByteBuffer buf = BufferUtils.createByteBuffer(bpp * width * height);
			
			//decode the image into the byte buffer, in RGBA format
			dec.decode(buf, width * bpp, PNGDecoder.Format.RGBA);
			
			//flip the buffer into "read mode" for OpenGL
			buf.flip();
			
			// initialize char widths
			int char_width = width/16;
			int char_height = height/16;
			
			char_widths = new int[256];
			
			for(int row = 0; row < 16; row++){
				for(int col = 0; col < 16; col++){
					int furthest_x = 0;
					for(int y = 0; y < char_height; y++){
						for(int x = furthest_x+1; x < char_width; x++){
							int bufIndex = (row*char_height+y)*width + col*char_width+x;
							if(buf.get(bufIndex*4) != 0)
								furthest_x = x;
						}
					}
					char_widths[row*16+col] = furthest_x+1;
				}
			}
			
			char_widths[asByte(' ')] = 2;
			
			
			//enable textures and generate an ID
			glEnable(target);
			id = glGenTextures();

			//bind texture
			bind();

			//setup unpack mode
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			//setup parameters
			glTexParameteri(target, GL_TEXTURE_MIN_FILTER, filter);
			glTexParameteri(target, GL_TEXTURE_MAG_FILTER, filter);
			glTexParameteri(target, GL_TEXTURE_WRAP_S, wrap);
			glTexParameteri(target, GL_TEXTURE_WRAP_T, wrap);
			
			//pass RGBA data to OpenGL
			glTexImage2D(target, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		}
	}

	public void bind() {
		glBindTexture(target, id);
	}
	
	public void drawString(String s, float startX, float curY, float height){
		float curX = startX;
		for(char c:s.toCharArray()){
			switch(c){
			case '\n':
				curY -= height;
				curX = startX;
				continue;
			case '\t':
				curX = (float) ((Math.ceil(curX / (height*4))+0.1) * height*4);
				continue;
			default:
				float w = (height * (getWidthOfChar(c)+1))/this.width*16;
				drawChar(c, curX, curY, height, height);
				curX += w;
			}
		}
	}
	
	public void drawChar(char c, float x, float y, float dx, float dy){
		byte bc = (byte) c;
		
		this.bind();
		
		byte firstFourBits = (byte) (bc >> 4);
		byte lastFourBits = (byte) (bc & 0x0F);
		
		float u = lastFourBits / 16f;
		float v = firstFourBits / 16f;
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
	
	public int getWidthOfChar(char c){
		return char_widths[asByte(c)];
	}
	
	private int asByte(char c){
		int b = (byte) c;
		if(b < 0)
			return b+256;
		else
			return b;
	}
}