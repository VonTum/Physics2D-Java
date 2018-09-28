package game.input;

import math.Vec2;

public interface InputHandler {
	public void mouseDown(Vec2 screenPos, int button, int modifiers);
	public void mouseUp(Vec2 screenPos, int button, int modifiers);
	public void mouseMove(Vec2 newScreenPos);
	public void scroll(double xscroll, double yscroll);
	
	public void keyDown(int key, int scancode, int modifiers);
	public void keyUp(int key, int scancode, int modifiers);
	public void keyRepeat(int key, int scancode, int modifiers);
}
