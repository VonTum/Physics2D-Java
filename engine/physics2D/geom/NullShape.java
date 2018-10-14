package physics2D.geom;

import java.util.stream.Stream;

import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Range;
import physics2D.math.Vec2;
import physics2D.physics.DepthWithDirection;

public class NullShape implements Shape {
	
	@Override
	public Stream<? extends OrientedPoint> getIntersectionPoints(Shape other) {
		return Stream.empty();
	}

	@Override
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation) {
		return null;
	}

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
	public CollisionOutline getCollisionOutline(Shape other) {
		return new CollisionOutline(new Vec2[0], new Vec2[0]);
	}

	@Override
	public Range getBoundsAlongDirection(NormalizedVec2 direction) {
		return null;
	}

	@Override
	public NormalizedVec2[] getSATDirections() {
		return new NormalizedVec2[0];
	}

	@Override
	public Shape union(Shape other) {
		return other;
	}

	@Override
	public Shape leftSlice(Vec2 origin, Vec2 direction) {
		return this;
	}

}
