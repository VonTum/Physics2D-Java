package physics2D.geom;

import java.util.Arrays;
import java.util.List;

import physics2D.Debug;
import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public interface ConvexPolygon extends Convex, Polygon{
	
	@Override
	public ConvexPolygon leftSlice(Vec2 origin, Vec2 direction);
	
	@Override
	public ConvexPolygon scale(double factor);
	
	@Override
	public ConvexPolygon transformToCFrame(CFrame frame);
	
	@Override
	public ConvexPolygon translate(Vec2 offset);
	
	@Override
	public ConvexPolygon rotate(RotMat2 rotation);
	
	@Override
	public default ConvexPolygon rotate(double angle){return rotate(RotMat2.rotTransform(angle));}
	
	@Override
	public default boolean containsPoint(Vec2 point) {
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
	public default Vec2[] getSATDirections(){
		Vec2[] corners = getCorners();
		Vec2[] directions = new Vec2[corners.length];
		for(int i = 0; i < corners.length-1; i++)
			directions[i] = corners[i+1].subtract(corners[i]);
		directions[corners.length-1] = corners[0].subtract(corners[corners.length-1]);
		
		return directions;
	}

	@Override
	public default List<? extends ConvexPolygon> convexDecomposition() {
		return Arrays.asList(new ConvexPolygon[]{this});
	}
	
	@Override
	public default Triangle[] divideIntoTriangles(){
		Vec2[] corners = getCorners();
		Triangle[] triangles = new Triangle[corners.length-2];
		
		for(int i = 0; i < corners.length-2; i++)
			triangles[i] = new Triangle(corners[0], corners[i+1], corners[i+2]);
		
		return triangles;
	}

	@Override
	public default Convex intersection(Convex other) {
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
	
	public default ConvexPolygon intersection(ConvexPolygon other){
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
}
