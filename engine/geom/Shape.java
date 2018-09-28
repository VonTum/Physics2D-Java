package geom;

import java.util.stream.Stream;

import physics.DepthWithDirection;
import math.BoundingBox;
import math.CFrame;
import math.NormalizedVec2;
import math.OrientedPoint;
import math.Range;
import math.Vec2;

public abstract class Shape {
	
	/**
	 * returns a list of points of this object that intersect other
	 * 
	 * @param other shape to be intersected
	 * @return All points for which other.containsPoint(p)
	 */
	public abstract Stream<? extends OrientedPoint> getIntersectionPoints(Shape other);
	public DepthWithDirection getNormalVecAndDepthToSurface(OrientedPoint point){return getNormalVecAndDepthToSurface(point.position, point.orientation);}
	public abstract DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation);
	public abstract boolean intersects(Shape other);
	public abstract boolean containsPoint(Vec2 point);
	public abstract Vec2[] getDrawingVertexes();
	public abstract double getArea();
	public abstract double getInertialArea();
	public abstract Vec2 getCenterOfMass();
	public abstract BoundingBox getBoundingBox();
	
	public abstract Shape transformToCFrame(CFrame frame);
	public abstract Shape scale(double factor);
	
	public abstract CollisionOutline getCollisionOutline(Shape other);
	public abstract Range getBoundsAlongDirection(NormalizedVec2 direction);
	/**
	 * returns the possible Separating Axis Theorem projection directions applicable to this shape. For polygons for example, this will be normalized vectors along it's edges
	 * @return an array of the found directions
	 */
	public abstract NormalizedVec2[] getSATDirections();
	public abstract Shape union(Shape other);
	/**
	 * Slices this shape along the given direction, and returns the slice to the left of the cut.
	 * 
	 * A leftSlice of a Shape which is entirely to the left of the slicing axis is the shape itself.
	 * 
	 * A leftSlice of a Shape which is entirely to the right should return a NullShape
	 * 
	 * @param origin
	 * @param direction
	 * @return the left slice of the shape
	 */
	public abstract Shape leftSlice(Vec2 origin, Vec2 direction);
}
