package physics2D.geom;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;

import physics2D.Debug;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.Range;
import physics2D.math.Vec2;

/**
 * Describes polygons, all polygons must be counter-clockwise oriënted
 */
public interface Polygon extends Shape {
	
	public Vec2[] getCorners();
	
	public ConvexPolygon[] convexDecomposition();
	
	public static ConvexPolygon[] convexDecomposition(Vec2[] polygon){
		ArrayList<Vec2[]> polyList = new ArrayList<Vec2[]>();
		convexDecompose(polygon, polyList);
		
		ConvexPolygon[] decomp = new ConvexPolygon[polyList.size()];
		for(int i = 0; i < decomp.length; i++)
			decomp[i] = new ConvexPolygon(polyList.get(i));
		
		return decomp;
	}
	
	
	public static void convexDecompose(Vec2[] polygon, ArrayList<Vec2[]> curList){
		int L = polygon.length;
		
		for(int i = 0; i < L; i++){
			Vec2 prev = polygon[(i-1+L)%L];
			Vec2 cur = polygon[i];
			Vec2 next = polygon[(i+1)%L];
			Vec2 prevToCur = cur.subtract(prev);
			Vec2 curToNext = next.subtract(cur);
			
			if(isConcave(prevToCur, curToNext)){
				// handle concave corner
				
				int bestCorner = -1;
				double bestDist = Double.POSITIVE_INFINITY;
				
				Debug.logPoint(cur, game.util.Color.RED);
				
				// iterates over all corners from i+1, stops at the first concave corner
				for(int j = (i+2)%L; j != (i-1+L)%L; j=(j+1)%L){
					// TODO finish
					
					Vec2 otherPrev = polygon[(j-1+L)%L];
					Vec2 otherCur = polygon[j];
					Vec2 otherNext = polygon[(j+1)%L];
					Vec2 otherPrevToCur = otherCur.subtract(otherPrev);
					Vec2 otherCurToNext = otherNext.subtract(otherCur);
					
					Vec2 delta = otherCur.subtract(cur);
					
					if((curToNext.cross(delta) >= 0 || 
						prevToCur.cross(delta) >= 0) &&
						liesBetween(otherPrevToCur, otherCurToNext, delta.neg())){
						
						Debug.logPoint(polygon[j], game.util.Color.GREEN);
						
						if(delta.lengthSquared() < bestDist){
							bestDist = delta.lengthSquared();
							bestCorner = j;
							
							
						}
					}
				}
				
				Debug.logPoint(polygon[bestCorner], game.util.Color.YELLOW);
				break;
			}
		}
	}
	
	public static boolean liesBetween(Vec2 prevToCur, Vec2 curToNext, Vec2 tested){
		if(isConvex(prevToCur, curToNext)){
			return curToNext.cross(tested) >= 0 &&
					prevToCur.cross(tested) >= 0;
		}else{
			return curToNext.cross(tested) >= 0 ||
					prevToCur.cross(tested) >= 0;
		}
	}
	
	/**
	 * Determines if a vertex is convex<br><br>
	 * 
	 * Inverse of {@link #isConcave(Vec2 prevToCur, Vec2 curToNext)}
	 * 
	 * @param prevToCur vector from the previous point to the current point, <code>cur-prev</code>
	 * @param curToNext vector from the current point to the next point, <code>next-cur</code>
	 * @return Whether or not the vertex between prev-cur-next is convex
	 */
	public static boolean isConvex(Vec2 prevToCur, Vec2 curToNext){
		return prevToCur.cross(curToNext) >= 0;
	}
	
	/**
	 * Determines if a vertex is convex<br><br>
	 * 
	 * Inverse of {@link #isConcave(Vec2 prev, Vec2 cur, Vec2 next)}
	 * 
	 * @param previous previous vertex
	 * @param current current vertex
	 * @param next next vertex
	 * @return Whether or not the vertex between prev-cur-next is convex
	 */
	public static boolean isConvex(Vec2 previous, Vec2 current, Vec2 next){
		return isConvex(current.subtract(previous), next.subtract(current));
	}
	
	/**
	 * Determines if a vertex is convex<br><br>
	 * 
	 * Inverse of {@link #isConcave(Vec2[] polygon, int i)}
	 * 
	 * @param polygon the polygon in which to check for convexness
	 * @param i index of corner to check
	 * @return Whether or not the vertex polygon[i] is convex
	 */
	public static boolean isConvex(Vec2[] polygon, int i){
		int L = polygon.length;
		Vec2 prev = polygon[(i-1+L)%L];
		Vec2 cur = polygon[i];
		Vec2 next = polygon[(i+1)%L];
		return isConvex(cur.subtract(prev), next.subtract(cur));
	}
	
	/**
	 * Determines if a vertex is concave<br><br>
	 * 
	 * Inverse of {@link #isConvex(Vec2 prevToCur, Vec2 curToNext)}
	 * 
	 * @param prevToCur vector from the previous point to the current point, <code>cur-prev</code>
	 * @param curToNext vector from the current point to the next point, <code>next-cur</code>
	 * @return Whether or not the vertex between prev-cur-next is concave
	 */
	public static boolean isConcave(Vec2 prevToCur, Vec2 curToNext){
		return prevToCur.cross(curToNext) < 0;
	}
	
	/**
	 * Determines if a vertex is concave<br><br>
	 * 
	 * Inverse of {@link #isConvex(Vec2 prev, Vec2 cur, Vec2 next)}
	 * 
	 * @param previous previous vertex
	 * @param current current vertex
	 * @param next next vertex
	 * @return Whether or not the vertex between prev-cur-next is concave
	 */
	public static boolean isConcave(Vec2 previous, Vec2 current, Vec2 next){
		return isConcave(current.subtract(previous), next.subtract(current));
	}
	
	/**
	 * Determines if a vertex is concave<br><br>
	 * 
	 * Inverse of {@link #isConvex(Vec2[] polygon, int i)}
	 * 
	 * @param polygon the polygon in which to check for concaveness
	 * @param i index of corner to check
	 * @return Whether or not the vertex polygon[i] is concave
	 */
	public static boolean isConcave(Vec2[] polygon, int i){
		int L = polygon.length;
		Vec2 prev = polygon[(i-1+L)%L];
		Vec2 cur = polygon[i];
		Vec2 next = polygon[(i+1)%L];
		return isConcave(cur.subtract(prev), next.subtract(cur));
	}
	
	/*@Override
	public default List<OrientedPoint> getIntersectionPoints(Shape other) {
		List<OrientedPoint> points = new ArrayList<>();
		
		for(Vec2 c:getCorners())
			if(other.containsPoint(c))
				points.add(new OrientedPoint(c, new NormalizedVec2(Double.NaN)));
		
		return points;
	}
	
	@Override
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation) {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public default boolean containsPoint(Vec2 point){
		boolean withinShape = false;
		
		Vec2[] vertexes = getCorners();
		
		for(int i = 0; i < vertexes.length; i++){
			Vec2 previous = vertexes[(vertexes.length + i - 1)%vertexes.length].subtract(point);
			Vec2 current = vertexes[i].subtract(point);
			Vec2 next = vertexes[(i+1)%vertexes.length].subtract(point);
			
			// from here on it is assumed 'point' lies at 0, 0 all coordinates are relative to this
			
			boolean goingUp = current.y <= 0 && next.y > 0;
			boolean goingDown = current.y >= 0 && next.y < 0;
			boolean intersectingCorner = current.y == 0;
			
			if(goingUp || goingDown) // check that line y=0 through point crosses this edge
				if(next.x*Math.abs(current.y) + current.x*Math.abs(next.y) >= 0) // check that edge lies to the right of point
					if(!intersectingCorner || previous.y * next.y <= 0)
						withinShape = !withinShape;
			
		}
		
		return withinShape;
	}

	@Override
	public default Vec2[] getDrawingVertexes() {
		return getCorners();
	}

	@Override
	public default double getArea() {
		double total = 0;
		Vec2[] verts = getCorners();
		for(int i = 0; i < verts.length; i++){
			Vec2 A = verts[i];
			Vec2 B = verts[(i+1)%verts.length];
			total += (B.x+A.x)*(B.y-A.y);
		}
		return total/2.0;
	}
	
	@Override
	public default double getInertialArea() {
		double total = 0;
		Vec2[] verts = getCorners();
		Vec2 com = getCenterOfMass();
		for(int i = 0; i < verts.length; i++)
			verts[i] = verts[i].subtract(com);
		
		for(int i = 0; i < verts.length; i++){
			Vec2 A = verts[i];
			Vec2 B = verts[(i+1)%verts.length];
			Vec2 D = B.subtract(A);
			double Dx = D.x, Dy=D.y, Ax=A.x, Ay=A.y;
			
			total += Dx*Dy*(Dx*Dx-Dy*Dy)/4.0+Dx*Dy*(Ax*Dx-Ay*Dy)+Dx*Dy*(Ax*Ax-Ay*Ay)*1.5+Ax*Ax*Ax*Dy-Ay*Ay*Ay*Dx;
		}
		return total/3.0;
	}
	
	/**
	 * Returns the center of mass of this polygon, relative to the local axes of this polygon
	 */
	@Override
	public default Vec2 getCenterOfMass(){
		double x=0,y=0;
		Vec2[] verts = getCorners();
		for(int i = 0; i < verts.length; i++){
			Vec2 A = verts[i];
			Vec2 B = verts[(i+1)%verts.length];
			Vec2 D = B.subtract(A);
			double Dx = D.x, Dy=D.y, Ax=A.x, Ay=A.y;
			
			x+=Dy*(Dx*Dx/3+Dx*Ax+Ax*Ax);
			y+=Dx*(Dy*Dy/3+Dy*Ay+Ay*Ay);
		}
		return new Vec2(x, -y).div(2*getArea());
	}

	@Override
	public default BoundingBox getBoundingBox(){
		Vec2[] vertexes = getCorners();
		
		double xmin=vertexes[0].x, ymin=vertexes[0].y;
		double xmax=xmin, ymax=ymin;
		
		for(int i = 1; i < vertexes.length; i++){
			xmin = min(xmin, vertexes[i].x);
			ymin = min(ymin, vertexes[i].y);
			xmax = max(xmax, vertexes[i].x);
			ymax = max(ymax, vertexes[i].y);
		}
		
		return new BoundingBox(xmin, ymin, xmax, ymax);
	}

	@Override
	public Polygon transformToCFrame(CFrame frame);
	
	@Override
	public Polygon scale(double factor);

	@Override
	public default Range getBoundsAlongDirection(NormalizedVec2 direction){
		Vec2[] vertexes = getCorners();
		double value = direction.cross(vertexes[0]);
		double min = value;
		double max = value;
		
		for(int i = 1; i < vertexes.length; i++){
			value = direction.cross(vertexes[i]);
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		
		return new Range(min, max);
	}

	/*@Override
	public Shape union(Shape other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape leftSlice(Vec2 origin, Vec2 direction) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	/**
	 * Returns a new polygon, using the given start and end indices
	 * 
	 * The new polygon is equivalent to 
	 * for(int i = start; i != end+1; i++%=poly.length)
	 *     newPoly[i] = poly[(start+i)%poly.length]
	 * 
	 * end can be smaller than start
	 * 
	 * @param poly
	 * @param start starting index
	 * @param end ending index
	 * @return
	 */
	public static Vec2[] polygonFromTo(Vec2[] poly, int start, int end){
		Vec2[] newPoly;
		if(start <= end){
			newPoly = new Vec2[end-start+1];
			
			for(int i = 0; i < newPoly.length; i++)
				newPoly[i] = poly[i+start];
			
		}else{
			int distTillWrap = poly.length-start;
			newPoly = new Vec2[distTillWrap+end+1];
			
			for(int i = 0; i < distTillWrap; i++)
				newPoly[i] = poly[i+start];
			for(int i = 0; i <= end; i++)
				newPoly[i+distTillWrap] = poly[i];
			
		}
		return newPoly;
	}
	
	/**
	 * Returns true if the provided polygon is convex
	 * @param poly the polygon to be tested
	 * @return Whether the given polygon is convex
	 */
	public static boolean isConvex(Vec2[] poly){
		Vec2 previous = poly[poly.length-2];
		Vec2 current = poly[poly.length-1];
		Vec2 prevDelta = current.subtract(previous);
		for(int i = 0; i < poly.length; i++){
			Vec2 next = poly[i];
			Vec2 delta = next.subtract(current);
			
			if(prevDelta.cross(delta) < 0)
				return false;
			
			previous = current;
			current = next;
			prevDelta = delta;
		}
		return true;
	}
	
	/**
	 * Returns the given polygon shifted by {@code shift}<br>
	 * 
	 * This means every vertex is shifted to the next position over {@code shift} times
	 * 
	 * @param polygon the polygon to be shifted
	 * @param shift the amount to shift
	 * @return The shifted polygon
	 */
	public static Vec2[] shifted(Vec2[] polygon, int shift){
		Vec2[] n = new Vec2[polygon.length];
		for(int i = 0; i < polygon.length; i++)
			n[(i+shift)%polygon.length] = polygon[i];
		return n;
	}
}
