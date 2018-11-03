package physics2D.geom;

import physics2D.math.CFrame;
import physics2D.math.Vec2;

public class Rectangle extends SimpleConvexPolygon {
	
	public double width, height;
	
	public Rectangle(double width, double height) {
		super(new Vec2[]{	new Vec2(width/2, height/2), 
							new Vec2(-width/2, height/2), 
							new Vec2(-width/2, -height/2), 
							new Vec2(width/2, -height/2)});
		
		this.width = width;
		this.height = height;
	}
	
	public Rectangle(CFrame location, double width, double height){
		super(Polygon.transformToCFrame(new Vec2[]{	new Vec2(width/2, height/2), 
							new Vec2(-width/2, height/2), 
							new Vec2(-width/2, -height/2), 
							new Vec2(width/2, -height/2)}, location));
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public double getArea() {
		return width * height;
	}
	
	@Override
	public double getInertialArea() {
		return width*height*(width*width+height*height)/12;
	}
	
	@Override
	public String toString(){
		return String.format("Rectangle(w=%.9f, h=%.9f)", width, height);
	}
}
