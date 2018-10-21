package physics2D.geom;

import physics2D.math.Vec2;

public class Triangle{
	public final Vec2 v1, v2, v3;
	
	public Triangle(Vec2 v1, Vec2 v2, Vec2 v3){
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
	
	public double getArea() {
		return v2.subtract(v1).cross(v3.subtract(v1)) / 2;
	}
	
	public double getInertialArea() {
		Vec2 v2t = v2.subtract(v1);
		Vec2 v3t = v3.subtract(v1);
		
		return getArea() * (v3t.subtract(v2t).lengthSquared() + v2t.dot(v3t))/18;
	}
	
	public Vec2 getCenterOfMass() {
		return Vec2.avg(v1, v2, v3);
	}
	
	public Vec2[] getCorners() {
		return new Vec2[]{v1, v2, v3};
	}
	
	@Override
	public String toString(){
		return String.format("Triangle(%s, %s, %s)", v1, v2, v3);
	}
}
