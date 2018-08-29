package geom;

import physics.PhysicalProperties;
import math.CFrame;
import math.Vec2;

public class Triangle extends ConvexPolygon {
	
	private final double baseWidth;
	private final Vec2 top;
	
	public Triangle(PhysicalProperties properties, CFrame cframe, double baseWidth, Vec2 top) {
		super(properties, cframe, new Vec2[]{top.subtract(top.div(3)), new Vec2(-baseWidth/2, 0).subtract(top.div(3)), new Vec2(baseWidth/2, 0).subtract(top.div(3))});
		// TODO Auto-generated constructor stub
		this.baseWidth = baseWidth;
		this.top = top;
	}
	
	

	@Override
	public double getArea() {
		return baseWidth / 2 * top.y;
	}
	
	@Override
	public double getInertialArea() {
		return baseWidth*top.y*(baseWidth*baseWidth/48 + top.lengthSquared()/36);
	}
	
	@Override
	public Vec2 getCenterOfMass() {
		/*Vertex2[] vertexes = getVertexes();
		WorldVec2 firstBisector = new WorldVec2(vertexes[0].position, vertexes[0].orientation);
		WorldVec2 secondBisector = new WorldVec2(vertexes[1].position, vertexes[1].orientation);
		return firstBisector.intersect(secondBisector);*/
		
		//Vec2 localCenter = top.div(3);
		//return getCFrame().localToGlobal(localCenter);
		
		return getCFrame().position;
	}

}
