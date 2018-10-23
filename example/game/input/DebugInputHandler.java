package game.input;

import game.Physics2D;

import org.lwjgl.glfw.GLFW;

import physics2D.Debug;
import physics2D.physics.World;

public class DebugInputHandler extends StandardInputHandler {

	public DebugInputHandler(World w) {
		super(w);
	}
	
	@Override
	public void keyDown(int key, int scancode, int modifiers) {
		super.keyDown(key, scancode, modifiers);
		switch(key){
		case GLFW.GLFW_KEY_SPACE:
			Debug.unpause();
		}
	}

}
