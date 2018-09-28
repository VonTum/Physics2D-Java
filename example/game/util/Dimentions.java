package game.util;

import physics2D.math.Vec2;

public class Dimentions {
	public final double width, height;
	
	public Dimentions(double width, double height){
		this.width = width;
		this.height = height;
	}
	
	public Dimentions expand(double delta){
		return new Dimentions(width+delta, height+delta);
	}
	
	public Vec2 getDiagonal(){
		return new Vec2(width, height);
	}
}
