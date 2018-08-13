package util;

public class Dimentions {
	public final double width, height;
	
	public Dimentions(double width, double height){
		this.width = width;
		this.height = height;
	}
	
	public Dimentions expand(double delta){
		return new Dimentions(width+delta, height+delta);
	}
}
