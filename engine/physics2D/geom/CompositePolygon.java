package physics2D.geom;

import java.util.Arrays;
import java.util.List;

import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

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
	public CompositePolygon transformToCFrame(CFrame frame) {
		ConvexPolygon[] newDecomp = new ConvexPolygon[decomposition.length];
		for(int p = 0; p < decomposition.length; p++)
			newDecomp[p] = decomposition[p].transformToCFrame(frame);
		
		return new CompositePolygon(Polygon.transformToCFrame(outline, frame), newDecomp);
	}
	
	@Override
	public CompositePolygon translate(Vec2 offset){
		ConvexPolygon[] d = new ConvexPolygon[decomposition.length];
		for(int i = 0; i < d.length; i++)
			d[i] = decomposition[i].translate(offset);
		return new CompositePolygon(Polygon.translate(outline, offset), d);
	}
	
	@Override
	public CompositePolygon rotate(RotMat2 rotation){
		ConvexPolygon[] d = new ConvexPolygon[decomposition.length];
		for(int i = 0; i < d.length; i++)
			d[i] = decomposition[i].rotate(rotation);
		return new CompositePolygon(Polygon.rotate(outline, rotation), d);
	}
	
	@Override
	public CompositePolygon rotate(double angle){return rotate(RotMat2.rotTransform(angle));}

	@Override
	public CompositePolygon scale(double factor) {
		ConvexPolygon[] newConvexes = new ConvexPolygon[decomposition.length];
		for(int i = 0; i < decomposition.length; i++)
			newConvexes[i] = decomposition[i].scale(factor);
		return new CompositePolygon(Polygon.scaled(outline, factor), decomposition);
	}

}
