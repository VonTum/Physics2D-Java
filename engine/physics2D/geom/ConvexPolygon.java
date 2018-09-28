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
		final int DEFAULT = -1;
		int edgeEnterIndex = DEFAULT;
		Vec2 enterPos = null;
		int edgeLeaveIndex = DEFAULT;
		Vec2 leavePos = null;
		
		for(int i = 0; i < vertexes.length; i++){
			Vec2 firstPos = vertexes[i].position;
			Vec2 secondPos = vertexes[(i+1)%vertexes.length].position;
			
			double firstRightSided = firstPos.subtract(origin).cross(direction);
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
		}
		
		if(edgeEnterIndex == DEFAULT || edgeLeaveIndex == DEFAULT)
			return new NullShape();
		
		int edgeCount = (edgeEnterIndex-edgeLeaveIndex + vertexes.length) % vertexes.length +2;
		
		Vec2[] newShape = new Vec2[edgeCount];
		
		newShape[0] = leavePos;
		for(int i = 1; i < edgeCount-1; i++)
			newShape[i] = vertexes[(edgeLeaveIndex+i)%vertexes.length].position;
		newShape[edgeCount-1] = enterPos;
		
		Debug.logPoint(enterPos, game.util.Color.ORANGE);
		Debug.logPoint(leavePos, game.util.Color.RED);
		return new TransformedConvexPolygon(Vertex2.convertToVertexes(newShape), 0, 0, null);
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
