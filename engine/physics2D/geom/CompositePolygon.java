package physics2D.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Range;
import physics2D.math.Vec2;
import physics2D.physics.DepthWithDirection;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CompositePolygon implements Polygon {
	
	private final Vec2[] outline;
	private final ConvexPolygon[] decomposition;
	
	
	
	public CompositePolygon(Vec2[] outline) {
		this.outline = outline;
		
		decomposition = Polygon.convexDecomposition(outline);
	}
	
	
	
	public static Triangle[] divideIntoTriangles(Vec2[] polygon){
		Triangle[] result = new Triangle[polygon.length-2];
		int triCount = 0;
		
		List<Vec2> poly = new ArrayList<Vec2>(Arrays.asList(polygon));
		
		int i = 0;
		while(triCount < result.length){
			int L = poly.size();
			int headI = i;
			int testedI = (i+1)%L;
			int nextI = (i+2)%L;
			Vec2 head = poly.get(headI);
			Vec2 tested = poly.get(testedI);
			Vec2 next = poly.get(nextI);
			
			if(isConvex(tested.subtract(head), next.subtract(tested))){
				// add triangle head-tested-next to list
				// remove tested from poly
				
				result[triCount++] = new Triangle(head, tested, next);
				poly.remove(testedI);
				if(headI > testedI){
					// this means removing the vertex has messed with out current index
					i = (i-1+L)%L;
				}
			}else{
				i = (i+1)%L;
			}
		}
		return result;
	}
	
	
	/*private static void convexDecompose(Vec2[] polygon, ArrayList<Vec2[]> curList){
		int L = polygon.length;
		
		ArrayList<Integer> concaveCorners = new ArrayList<>();
		
		for(int i = 0; i < L; i++){
			Vec2 cur = polygon[i];
			Vec2 prev = polygon[(i-1+L) % L];
			Vec2 next = polygon[(i+1) % L];
			
			if(cur.subtract(prev).cross(next.subtract(cur)) < 0){
				concaveCorners.add(i);
			}
		}
		
		int i = 0;
		nextCorner: while(!concaveCorners.isEmpty()){
			int curCornerI = concaveCorners.get(i);
			Vec2 curCorner = polygon[curCornerI];
			Vec2 incoming = polygon[i].subtract(polygon[(i-1+L)%L]);
			Vec2 outgoing = polygon[(i+1)%L].subtract(polygon[i]);
			
			
			
			for(int j = i+1; j < concaveCorners.size(); j++){
				int foundCornerI = concaveCorners.get(j);
				Vec2 c = polygon[foundCornerI];
				Vec2 relative = c.subtract(curCorner);
				
				if(incoming.cross(relative) >= 0 && outgoing.cross(relative) >= 0){
					// connect curCorner to foundCorner
					
					Vec2[] leftSide = Polygon.polygonFromTo(polygon, curCornerI, foundCornerI);
					Vec2[] rightSide = Polygon.polygonFromTo(polygon, foundCornerI, curCornerI);
					
					// TODO Finish
					
						
					
				}
			}
		}
	}*/
	
	private CompositePolygon(Vec2[] outline, ConvexPolygon[] decomposition){
		this.outline = outline;
		this.decomposition = decomposition;
	}
	
	private boolean isConvex(int i){
		int L = outline.length;
		Vec2 cur = outline[i];
		Vec2 prev = outline[(i-1+L) % L];
		Vec2 next = outline[(i+1) % L];
		return cur.subtract(prev).cross(next.subtract(cur)) > 0;
	}
	
	@Override
	public ConvexPolygon[] convexDecomposition() {
		return decomposition;
	};
	
	@Override
	public List<OrientedPoint> getIntersectionPoints(Shape other) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(Shape other) {
		if(!(other instanceof Polygon))
			return other.intersects(this);
		
		Polygon p = (Polygon) other;
		
		ConvexPolygon[] otherDecomp = p.convexDecomposition();
		
		for(ConvexPolygon cp:decomposition)
			for(ConvexPolygon op:otherDecomp)
				if(cp.intersects(op))
					return true;
		
		return false;
	}

	@Override
	public CollisionOutline getCollisionOutline(Shape other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range getBoundsAlongDirection(NormalizedVec2 direction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NormalizedVec2[] getSATDirections() {
		throw new NotImplementedException();
	}

	@Override
	public Shape union(Shape other) {
		throw new NotImplementedException();
	}

	@Override
	public Shape leftSlice(Vec2 origin, Vec2 direction) {
		throw new NotImplementedException();
	}

	@Override
	public Vec2[] getCorners() {
		return outline;
	}

	@Override
	public Polygon transformToCFrame(CFrame frame) {
		ConvexPolygon[] newDecomp = new ConvexPolygon[decomposition.length];
		for(int p = 0; p < decomposition.length; p++)
			newDecomp[p] = decomposition[p].transformToCFrame(frame);
		
		return new CompositePolygon(transformToCFrame(frame, outline), newDecomp);
	}
	
	private static Vec2[] transformToCFrame(CFrame frame, Vec2[] poly){
		Vec2[] newPoly = new Vec2[poly.length];
		for(int i = 0; i < poly.length; i++)
			newPoly[i] = frame.localToGlobal(poly[i]);
		return newPoly;
	}

	@Override
	public Polygon scale(double factor) {
		// TODO Auto-generated method stub
		return null;
	}

}
