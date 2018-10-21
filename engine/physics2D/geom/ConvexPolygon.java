package physics2D.geom;

import physics2D.Debug;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;

public class ConvexPolygon extends AbstractPolygon {
	
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
		
		return new ConvexPolygon(newVertexes);
	}
	
	@Override
	public ConvexPolygon leftSlice(Vec2 origin, Vec2 direction){
		return new ConvexPolygon(Vertex2.convertToVertexes(leftSlice(getCorners(), origin, direction)));
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
	
	public static Triangle[] divideIntoTriangles(Vec2[] polygon){
		Triangle[] triangles = new Triangle[polygon.length-2];
		Vec2 mainCorner = polygon[polygon.length-1];
		for(int i = 0; i < polygon.length-2; i++){
			triangles[i] = new Triangle(
					mainCorner,
					polygon[i],
					polygon[i+1]
			);
		}
		return triangles;
	}
	
	//TODO ADD @Override
	public ConvexPolygon[] convexDecomposition() {
		return new ConvexPolygon[]{this};
	};
	
	@Override
	public double getArea(){
		double A = 0;
		Vec2[] corners = getCorners();
		for(int i = 0; i < corners.length-1; i++)
			A += corners[i].cross(corners[i+1]);
		A += corners[corners.length-1].cross(corners[0]);
		
		return A/2;
	}
	
	public Vec2 getCenterOfMass(){
		Vec2 total = Vec2.ZERO;
		
		Vec2[] corners = getCorners();
		
		for(int i=0; i < corners.length; i++)
			total = total.add(corners[i].add(corners[(i+1)%corners.length]).mul(corners[i].cross(corners[(i+1)%corners.length])));
		
		return total.div(6*getArea());
	}
	
	@Override
	public double getInertialArea(){
		Vec2 com = getCenterOfMass();
		
		double total = 0;
		
		for(Triangle t:divideIntoTriangles(getCorners()))
			total+=t.getInertialArea()+t.getCenterOfMass().subtract(com).lengthSquared() * t.getArea();
		
		return total;
	}
}
