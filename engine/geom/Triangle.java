package geom;
import math.Vec2;

public class Triangle extends ConvexPolygon {
	
	private final double baseWidth;
	private final Vec2 top;
	
	public Triangle(double baseWidth, Vec2 top) {
		super(new Vec2[]{top.subtract(top.div(3)), new Vec2(-baseWidth/2, 0).subtract(top.div(3)), new Vec2(baseWidth/2, 0).subtract(top.div(3))});
		// TODO Auto-generated constructor stub
		this.baseWidth = baseWidth;
		this.top = top;
	}
	
	@Override
	public double getArea() {
		return baseWidth*top.y / 2;
	}
	
	@Override
	public double getInertialArea() {
		return getArea()*(baseWidth*baseWidth/24 + top.lengthSquared()/18);
	}
	
	@Override
	public Vec2 getCenterOfMass() {
		return Vec2.ZERO;
	}

}
