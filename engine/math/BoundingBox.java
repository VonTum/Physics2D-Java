package math;

import static java.lang.Math.min;
import static java.lang.Math.max;


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
	
	public boolean intersects(BoundingBox b){
		return xmin < b.xmax && b.xmin < xmax && 
				ymin < b.ymax && b.ymin < ymax;
	}
	

	public static BoundingBox mergeBoxes(BoundingBox[] boxes) {
		if(boxes.length == 0) return new BoundingBox(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		BoundingBox total = boxes[0];
		for(int i = 1; i < boxes.length; i++)
			total = total.merge(boxes[i]);
		
		return total;
	}
	
	public BoundingBox merge(BoundingBox o){
		return new BoundingBox(
				min(xmin, o.xmin),
				min(ymin, o.ymin),
				max(xmax, o.xmax),
				max(ymax, o.ymax)
		);
	}
}
