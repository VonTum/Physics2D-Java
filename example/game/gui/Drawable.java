package game.gui;

import game.util.Color;

abstract class Drawable {
	protected final Color color;
	
	protected Drawable(Color color){
		this.color = color;
	}
	
	public void drawAndColor(){
		Screen.color(color);
		draw();
	}
	public abstract void draw();
}