package physics2D.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import physics2D.Debug;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;
import physics2D.physics.DepthWithDirection;

public class ConvexPolygon implements Convex, Polygon{
	
	public final Vec2[] corners;
	public ConvexPolygon(Vec2[] polygon) {
		this.corners = polygon;
	}
	@Override
	public Vec2[] getCorners() {
		return corners;
	}
	
	@Override
	public boolean containsPoint(Vec2 point) {
		Vec2[] corners = getCorners();
		Vec2 cur = corners[corners.length-1];
		for(int i = 0; i < corners.length; i++){
			Vec2 next = corners[i];
			if(cur.subtract(point).cross(next.subtract(cur)) < 0)
				return false;
			
			cur = next;
		}
		
		return true;
	}
	
	@Override
	public ConvexPolygon leftSlice(Vec2 origin, Vec2 direction){
		return new ConvexPolygon(leftSlice(getCorners(), origin, direction));
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
	
	@Override
	public Vec2[] getSATDirections(){
		Vec2[] corners = getCorners();
		Vec2[] directions = new Vec2[corners.length];
		for(int i = 0; i < corners.length-1; i++)
			directions[i] = corners[i+1].subtract(corners[i]);
		directions[corners.length-1] = corners[0].subtract(corners[corners.length-1]);
		
		return directions;
	}
	
	@Override
	public ConvexPolygon scale(double factor){
		return new ConvexPolygon(Polygon.scaled(getCorners(), factor));
	}

	@Override
	public List<? extends ConvexPolygon> convexDecomposition() {
		return Arrays.asList(new ConvexPolygon[]{this});
	}
	
	@Override
	public Triangle[] divideIntoTriangles(){
		Vec2[] corners = getCorners();
		Triangle[] triangles = new Triangle[corners.length-2];
		
		for(int i = 0; i < corners.length-2; i++)
			triangles[i] = new Triangle(corners[0], corners[i+1], corners[i+2]);
		
		return triangles;
	}

	@Override
	public Convex intersection(Convex other) {
		Vec2[] corners = getCorners();
		Vec2 curCorner = corners[corners.length-1];
		Convex currentConv = other;
		for(int i = 0; i < corners.length; i++){
			Vec2 next = corners[i];
			Vec2 sliceOrigin = curCorner;
			Vec2 sliceDirection = next.subtract(curCorner);
			currentConv = currentConv.leftSlice(sliceOrigin, sliceDirection);
			curCorner = next;
		}
		return currentConv;
	}
	
	public ConvexPolygon intersection(ConvexPolygon other){
		Vec2[] corners = getCorners();
		Vec2 curCorner = corners[corners.length-1];
		ConvexPolygon currentConv = other;
		for(int i = 0; i < corners.length; i++){
			Vec2 next = corners[i];
			Vec2 sliceOrigin = curCorner;
			Vec2 sliceDirection = next.subtract(curCorner);
			currentConv = currentConv.leftSlice(sliceOrigin, sliceDirection);
			curCorner = next;
		}
		return currentConv;
	}
	
	@Override
	public ConvexPolygon transformToCFrame(CFrame frame){
		return new ConvexPolygon(Polygon.transformToCFrame(getCorners(), frame));
	}
	
	@Override
	public ConvexPolygon translate(Vec2 offset){
		return new ConvexPolygon(Polygon.translate(getCorners(), offset));
	}
	
	@Override
	public ConvexPolygon rotate(RotMat2 rotation){
		return new ConvexPolygon(Polygon.rotate(getCorners(), rotation));
	}
	
	@Override
	public ConvexPolygon rotate(double angle){return rotate(RotMat2.rotTransform(angle));}

	@Override
	public List<OrientedPoint> getIntersectionPoints(Shape other) {
		List<OrientedPoint> points = new ArrayList<OrientedPoint>();
		for(Vertex2 vert:Vertex2.convertToVertexes(getCorners())){
			if(other.containsPoint(vert.position))
				points.add(new OrientedPoint(vert.position, vert.orientation));
		}
		return points;
	}
	
	/**
	 * Gets all normal vectors of all sides, and ranks them based on their 'score'
	 * 
	 * This score is calculated as follows: 
	 * 		score = -normalVec . point.orientation / depth
	 * 
	 * @param point
	 * @return the vector to the nearest surface
	 */
	@Override
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation){
		
		System.out.println("Used getNormalVec1AndDepthToSurface!");
		
		Vertex2[] vertexes = Vertex2.convertToVertexes(getCorners());
		
		double bestDepth = Double.NaN;
		NormalizedVec2 bestNormalVec = null;
		double bestScore = 0;
		
		for(int i = 0; i < vertexes.length; i++){
			// Vec2 normalizedSide = vertexes[(i+1) % vertexes.length].position.subtract(vertexes[i].position).normalize();
			NormalizedVec2 normalVec = vertexes[i].normalVec;
			double depth = normalVec.rotate90CounterClockwise().pointToLineDistance(position.subtract(vertexes[i].position));
			double score = -normalVec.dot(orientation) / depth;
			if(score > bestScore){
				bestScore = score;
				bestDepth = depth;
				bestNormalVec = normalVec;
			}
		}
		
		return new DepthWithDirection(bestNormalVec, bestDepth);
	}
}
