package physics2D.geom;

import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class RegularPolygon extends SimpleConvexPolygon {
	
	private final Vec2 startPoint;
	
	private static Vec2[] getRegularPolygonWithCornerCount(int n, Vec2 startPoint){
		Vec2[] corners = new Vec2[n];
		RotMat2 rotation = new RotMat2(2*Math.PI/n);
		corners[0] = startPoint;
		Vec2 cur = startPoint;
		for(int i = 1; i < n; i++)
			corners[i] = (cur = rotation.mul(cur));
		
		return corners;
	}
	
	public RegularPolygon(int cornerCount, Vec2 startPoint) {
		super(getRegularPolygonWithCornerCount(cornerCount, startPoint));
		this.startPoint = startPoint;
	}
	
	@Override
	public double getArea() {
		return startPoint.cross(getCorners()[1].subtract(getCorners()[0])) / 2 * getCorners().length;
	}

	@Override
	public double getInertialArea() {
		Vec2 edge = getCorners()[1].subtract(getCorners()[0]);
		double triangleInertia = startPoint.cross(edge) * edge.lengthSquared() / 48 + Math.pow(startPoint.cross(edge), 3) / (4*edge.lengthSquared());
		return triangleInertia * getCorners().length;
	}
	
	@Override
	public Vec2 getCenterOfMass() {
		return Vec2.ZERO;
	}
}
