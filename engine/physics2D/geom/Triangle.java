package physics2D.geom;

import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class Triangle implements ConvexPolygon {
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

	@Override
	public Vec2[] getSATDirections() {
		return new Vec2[]{v2.subtract(v1), v3.subtract(v2), v1.subtract(v3)};
	}

	@Override
	public Triangle scale(double factor) {
		return new Triangle(v1.mul(factor), v2.mul(factor), v3.mul(factor));
	}

	@Override
	public Triangle transformToCFrame(CFrame frame) {
		return new Triangle(frame.localToGlobal(v1), frame.localToGlobal(v2), frame.localToGlobal(v3));
	}

	@Override
	public Triangle translate(Vec2 offset) {
		return new Triangle(v1.add(offset), v2.add(offset), v3.add(offset));
	}

	@Override
	public Triangle rotate(RotMat2 rotation) {
		return new Triangle(rotation.mul(v1), rotation.mul(v2), rotation.mul(v3));
	}
	
	@Override
	public Triangle rotate(double angle){
		return rotate(RotMat2.rotTransform(angle));
	}
	
	@Override
	public ConvexPolygon leftSlice(Vec2 origin, Vec2 direction) {
		return SimpleConvexPolygon.leftSlice(getCorners(), origin, direction);
	}
}
