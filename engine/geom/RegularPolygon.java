package geom;

import math.RotMat2;
import math.Vec2;

public class RegularPolygon extends ConvexPolygon {
	
	private final int cornerCount;
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
		this.cornerCount = cornerCount;
		this.startPoint = startPoint;
	}
	
	@Override
	public double getArea() {
		return startPoint.cross(vertexes[1].position.subtract(vertexes[0].position)) / 2 * cornerCount;
	}

	@Override
	public double getInertialArea() {
		Vec2 edge = vertexes[1].position.subtract(vertexes[0].position);
		double triangleInertia = startPoint.cross(edge) * edge.lengthSquared() / 48 + Math.pow(startPoint.cross(edge), 3) / (4*edge.lengthSquared());
		return triangleInertia * cornerCount;
	}
	
	@Override
	public Vec2 getCenterOfMass() {
		return Vec2.ZERO;
	}
}
