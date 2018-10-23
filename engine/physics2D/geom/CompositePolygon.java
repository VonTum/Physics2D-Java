package physics2D.geom;

import java.util.Arrays;
import java.util.List;

import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Vec2;
import physics2D.physics.DepthWithDirection;

public class CompositePolygon implements Polygon {
	
	private final Vec2[] outline;
	private final ConvexPolygon[] decomposition;
	
	public CompositePolygon(Vec2[] outline) {
		this.outline = outline;
		List<Vec2[]> d = Polygon.convexDecomposition(outline);
		decomposition = new ConvexPolygon[d.size()];
		for(int i = 0; i < d.size(); i++)
			decomposition[i] = new ConvexPolygon(d.get(i));
	}
	
	private CompositePolygon(Vec2[] outline, ConvexPolygon[] decomposition){
		this.outline = outline;
		this.decomposition = decomposition;
	}
	
	@Override
	public List<ConvexPolygon> convexDecomposition() {
		return Arrays.asList(decomposition);
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
		
		List<? extends ConvexPolygon> otherDecomp = p.convexDecomposition();
		
		for(ConvexPolygon cp:decomposition)
			for(ConvexPolygon op:otherDecomp)
				if(cp.intersects(op))
					return true;
		
		return false;
	}

	/*@Override
	public Shape union(Shape other) {
		throw new NotImplementedException();
	}*/

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
	public CompositePolygon scale(double factor) {
		ConvexPolygon[] newConvexes = new ConvexPolygon[decomposition.length];
		for(int i = 0; i < decomposition.length; i++)
			newConvexes[i] = decomposition[i].scale(factor);
		return new CompositePolygon(Polygon.scaled(outline, factor), decomposition);
	}

}
