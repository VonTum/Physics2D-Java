package util;


public class Color {
	
	public static final Color RED = new Color(1.0, 0.0, 0.0, 1.0);
	public static final Color GREEN = new Color(0.0, 1.0, 0.0, 1.0);
	public static final Color BLUE = new Color(0.0, 0.0, 1.0, 1.0);
	public static final Color YELLOW = new Color(1.0, 1.0, 0.0, 1.0);
	public static final Color PURPLE = new Color(1.0, 0.0, 1.0, 1.0);
	public static final Color CYAN = new Color(0.0, 1.0, 1.0, 1.0);
	public static final Color WHITE = new Color(1.0, 1.0, 1.0, 1.0);
	public static final Color BLACK = new Color(0.0, 0.0, 0.0, 1.0);
	public static final Color MAROON = new Color(0.5, 0.0, 0.0, 1.0);
	public static final Color LIGHT_GREY = new Color(0.7, 0.7, 0.7, 1.0);
	public static final Color GREY = new Color(0.5, 0.5, 0.5, 1.0);
	public static final Color DARK_GREY = new Color(0.2, 0.2, 0.2, 1.0);
	public static final Color DEFAULT_BRICK_COLOR = new Color(0.8, 0.8, 0.6, 0.6);
	
	public final double r, g, b, a;
	public Color(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color darker(){
		return darker(0.7);
	}
	
	public Color darker(double factor){
		return new Color(r*factor, g*factor, b*factor, a);
	}
	
	public Color red(double r){
		return new Color(r, g, b, a);
	}
	
	public Color blue(double b){
		return new Color(r, g, b, a);
	}
	
	public Color green(double g){
		return new Color(r, g, b, a);
	}
	
	public Color alpha(double a){
		return new Color(r, g, b, a);
	}
}
