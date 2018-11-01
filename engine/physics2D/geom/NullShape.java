package physics2D.geom;

import java.util.Collections;
import java.util.List;

import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.Range;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public final class NullShape implements Shape, Convex {

	@Override
	public boolean intersects(Shape other) {
		return false;
	}

	@Override
	public boolean containsPoint(Vec2 point) {
		return false;
	}

	@Override
	public Vec2[] getDrawingVertexes() {
		return new Vec2[0];
	}

	@Override
	public double getArea() {
		return 0;
	}

	@Override
	public double getInertialArea() {
		return 0;
	}

	@Override
	public Vec2 getCenterOfMass() {
		return null;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return null;
	}

	@Override
	public Shape transformToCFrame(CFrame frame) {
		return this;
	}

	@Override
	public Shape scale(double factor) {
		return this;
	}

	@Override
	public Range getBoundsAlongDirection(Vec2 direction) {
		return null;
	}

	@Override
	public Vec2[] getSATDirections() {
		return new Vec2[0];
	}

	@Override
	public Shape union(Shape other) {
		return other;
	}

	@Override
	public NullShape leftSlice(Vec2 origin, Vec2 direction) {
		return this;
	}
	
	@Override
	public List<NullShape> convexDecomposition(){
		return Collections.emptyList();
	}
	
	@Override
	public Convex intersection(Convex other){
		return this;
	}
	
	@Override public NullShape translate(Vec2 offset){return this;}
	@Override public NullShape rotate(double angle){return this;}
	@Override public NullShape rotate(RotMat2 rotation){return this;}
	
}
