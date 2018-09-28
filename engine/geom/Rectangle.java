package geom;

import math.Vec2;

public class Rectangle extends ConvexPolygon {
	
	public double width, height;
	
	public Rectangle(double width, double height) {
		super(new Vec2[]{	new Vec2(width/2, height/2), 
							new Vec2(-width/2, height/2), 
							new Vec2(-width/2, -height/2), 
							new Vec2(width/2, -height/2)});
		
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
	public Vec2 getCenterOfMass(){
		return Vec2.ZERO;
	}
	
	@Override
	public boolean containsPoint(Vec2 point) {
		// Vec2 localPoint = getCFrame().globalToLocal(point);
		return Math.abs(point.x) <= width/2 && Math.abs(point.y) <= height/2;
	}
	
	@Override
	public String toString(){
		return String.format("Rectangle(w=%.9f, h=%.9f)", width, height);
	}
	
	/*@Override
	public Iterator<Vec2> iterator(){
		return new Iterator<Vec2>() {
			double curX = -width/2, curY = -height/2;
			@Override
			public boolean hasNext() {
				return curX < width/2 && curY < height/2;
			}

			@Override
			public Vec2 next() {
				Vec2 v = new Vec2(curX, curY);
				curX += width * 0.05;
				if(curX >= width/2){
					curX = -width/2;
					curY += height * 0.05;
				}
				return getCFrame().localToGlobal(v);
			}
		};
	}*/
}
