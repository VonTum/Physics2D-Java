package math;

public class BoundingBox {
	public final double xmin, xmax, ymin, ymax;
	
	public BoundingBox(double xmin, double ymin, double xmax, double ymax){
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
	
	public BoundingBox(Vec2 botLeft, Vec2 topRight){
		this(botLeft.x, botLeft.y, topRight.x, topRight.y);
	}
	
	public double getWidth(){return xmax-xmin;}
	public double getHeight(){return ymax-ymin;}
	
	@Override
	public String toString(){
		return "BoundingBox("+new Vec2(xmin, ymin)+","+new Vec2(xmax, ymax)+")";
	}
}
