package game.input;

import game.Physics2D;
import game.gui.Screen;

import org.lwjgl.glfw.GLFW;

import physics2D.math.Vec2;
import physics2D.physics.Part;
import physics2D.physics.World;

public class StandardInputHandler implements InputHandler {
	
	private World world;
	
	private boolean[] mouseDown = new boolean[6];
	
	private Vec2 lastMousePos = Vec2.ZERO;
	
	public StandardInputHandler(World w) {
		this.world = w;
	}
	
	@Override
	public void mouseDown(Vec2 screenPos, int button, int modifiers) {
		mouseDown[button] = true;
		if(button == GLFW.GLFW_MOUSE_BUTTON_1){
			Vec2 worldPos = Screen.mouseToWorldCoords(screenPos);
			Part selectedPart = world.getPartAt(worldPos);
			if(selectedPart != null){
				onClickPart(worldPos, selectedPart);
			}
			world.grabBlock(worldPos);
		}
	}
	
	public void onClickPart(Vec2 worldPos, Part clickedPart){
		//ConvexPolygon p = (ConvexPolygon) clickedPart.shape;
		//ConvexPolygon cp = p.transformToCFrame(new CFrame(p.getCenterOfMass().neg()));
		//System.out.println(String.format("%.20f", cp.getNewI()));
		
		//System.out.println(p.getCenterOfMass() + "<=>" + p.getNewCOM());
	}

	@Override
	public void mouseUp(Vec2 screenPos, int button, int modifiers) {
		mouseDown[button] = false;
		if(button == GLFW.GLFW_MOUSE_BUTTON_1){
			world.dropBlock();
		}
	}
	
	@Override
	public void mouseMove(Vec2 newScreenPos) {
		if(mouseDown[0])
			world.dragBlock(Screen.mouseToWorldCoords(newScreenPos));
		
		if(mouseDown[1])
			Screen.camera.move(Screen.mouseToWorldCoords(lastMousePos).subtract(Screen.mouseToWorldCoords(newScreenPos)));
		
		lastMousePos = newScreenPos;
	}

	@Override
	public void keyDown(int key, int scancode, int modifiers) {
		keyDownOrRepeat(key, scancode, modifiers);
		switch(key){
		case GLFW.GLFW_KEY_P:
			Physics2D.SIMULATION_PAUSED = !Physics2D.SIMULATION_PAUSED;
			System.out.println("Simulation "+((Physics2D.SIMULATION_PAUSED)?"paused":"unpaused"));
			break;
		
		}
	}
	
	private void keyDownOrRepeat(int key, int scancode, int modifiers) {
		switch(key){
		case GLFW.GLFW_KEY_KP_ADD:
			Physics2D.SIMULATION_SPEED *= 1.1;
			System.out.println("Simulation speed: " + Physics2D.SIMULATION_SPEED);
			break;
		case GLFW.GLFW_KEY_KP_SUBTRACT:
			Physics2D.SIMULATION_SPEED /= 1.1;
			System.out.println("Simulation speed: " + Physics2D.SIMULATION_SPEED);
			break;
		case GLFW.GLFW_KEY_O:
			Physics2D.runTick(world);
			System.out.println("Proceeded one step");
			break;
		case GLFW.GLFW_KEY_G:
			System.out.println("Garbagecollecting!");
			System.gc();
			break;
		}
		
		System.out.println(GLFW.glfwGetKeyName(key, scancode));
	}
	
	@Override
	public void keyUp(int key, int scancode, int modifiers) {
		
	}
	
	@Override
	public void keyRepeat(int key, int scancode, int modifiers) {
		keyDownOrRepeat(key, scancode, modifiers);
	}
	
	@Override
	public void scroll(double xscroll, double yscroll) {
		Screen.camera.zoom(Math.pow(1.3, yscroll), Screen.mouseToScreenCoords(Screen.getMousePos()));
	}
}
