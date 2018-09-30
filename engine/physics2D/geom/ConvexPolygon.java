package physics2D.geom;

import physics2D.Debug;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;

public abstract class ConvexPolygon extends Polygon {
	
	public ConvexPolygon(Vec2[] polygon) {
		super(polygon);
	}
	
	public ConvexPolygon(Vertex2[] vertexes) {
		super(vertexes);
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
	
	@Override
	public ConvexPolygon transformToCFrame(CFrame frame){
		Vertex2[] newVertexes = new Vertex2[vertexes.length];
		
		for(int i = 0; i < vertexes.length; i++)
			newVertexes[i] = frame.localToGlobal(vertexes[i]);
		
		return new TransformedConvexPolygon(newVertexes, getArea(), getInertialArea(), frame.localToGlobal(getCenterOfMass()));
	}
	
	@Override
	public Shape leftSlice(Vec2 origin, Vec2 direction){
		return new TransformedConvexPolygon(Vertex2.convertToVertexes(leftSlice(getCorners(), origin, direction)), 0, 0, null);
	}
	
	public static Vec2[] leftSlice(Vec2[] poly, Vec2 origin, Vec2 direction){
		if(poly.length == 0) return poly;
		
		int edgeEnterIndex = -1;
		Vec2 enterPos = null;
		int edgeLeaveIndex = -1;
		Vec2 leavePos = null;
		
		Vec2 firstPos = poly[poly.length-1];
		double firstRightSided = firstPos.subtract(origin).cross(direction);
		
		for(int i = 0; i < poly.length; i++){
			Vec2 secondPos = poly[i];
			
			double secondRightSided = secondPos.subtract(origin).cross(direction);
			
			if(firstRightSided > 0 && secondRightSided <= 0){
				// leaving
				edgeLeaveIndex = i;
				leavePos = Vec2.getIntersection(firstPos, secondPos.subtract(firstPos), origin, direction);
			}else if(firstRightSided <= 0 && secondRightSided > 0){
				//entering
				edgeEnterIndex = i;
				enterPos = Vec2.getIntersection(firstPos, secondPos.subtract(firstPos), origin, direction);
			}
			
			firstPos = secondPos;
			firstRightSided = secondRightSided;
		}
		
		if(edgeEnterIndex == -1 || edgeLeaveIndex == -1)
			return (firstRightSided <= 0)? poly:new Vec2[0];// no intersection and a vertex on one side means entire polygon on that side
		
		int edgeCount = (edgeEnterIndex-edgeLeaveIndex + poly.length) % poly.length +2;
		
		Vec2[] newPoly = new Vec2[edgeCount];
		
		newPoly[0] = leavePos;
		for(int i = 1; i < edgeCount-1; i++)
			newPoly[i] = poly[(edgeLeaveIndex+i-1)%poly.length];
		newPoly[edgeCount-1] = enterPos;
		
		Debug.logPoint(enterPos, game.util.Color.ORANGE);
		Debug.logPoint(leavePos, game.util.Color.RED);
		return newPoly;
	}
	
	public static Vec2[] intersection(Vec2[] poly1, Vec2[] poly2){
		Vec2[] curPoly = poly1;
		for(int i = 0; i < poly2.length; i++)
			curPoly = leftSlice(curPoly, poly2[i], poly2[(i+1)%poly2.length].subtract(poly2[i]));
		
		return curPoly;
	}
	
	public static Vec2[][] divideIntoTriangles(Vec2[] polygon){
		Vec2[][] triangles = new Vec2[polygon.length-2][];
		Vec2 mainCorner = polygon[polygon.length-1];
		for(int i = 0; i < polygon.length-2; i++){
			triangles[i] = new Vec2[]{
					mainCorner,
					polygon[i],
					polygon[i+1]
			};
		}
		return triangles;
	}
	
	private static class TransformedConvexPolygon extends ConvexPolygon {
		
		private final double area;
		private final double inertialArea;
		private final Vec2 centerOfMass;
		
		public TransformedConvexPolygon(Vertex2[] polygon, double area, double inertialArea, Vec2 centerOfMass) {
			super(polygon);
			this.area = area;
			this.inertialArea = inertialArea;
			this.centerOfMass = centerOfMass;
		}

		@Override
		public double getArea() {
			return area;
		}

		@Override
		public double getInertialArea() {
			return inertialArea;
		}

		@Override
		public Vec2 getCenterOfMass() {
			return centerOfMass;
		}
	}
}
