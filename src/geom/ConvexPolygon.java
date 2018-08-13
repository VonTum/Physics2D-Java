package geom;

import math.CFrame;
import math.Vec2;
import math.Vertex2;
import physics.PhysicalProperties;

public abstract class ConvexPolygon extends Polygon {
	
	public ConvexPolygon(PhysicalProperties properties, CFrame cframe, Vec2[] polygon) {
		super(properties, cframe, polygon);
	}
	
	public ConvexPolygon(PhysicalProperties properties, CFrame cframe, Vertex2[] vertexes) {
		super(properties, cframe, vertexes);
	}
	
	@Override
	public boolean containsPoint(Vec2 point) {
		for(Vertex2 vertex: getVertexes()){
			Vec2 relativeVec = vertex.position.subtract(point);
			if(vertex.normalVec.dot(relativeVec) < 0)
				return false;
		}
		
		return true;
	}
}
