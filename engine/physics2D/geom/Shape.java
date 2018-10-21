package physics2D.geom;

import java.util.List;

import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Range;
import physics2D.math.Vec2;
import physics2D.physics.DepthWithDirection;

public interface Shape {
	
	/**
	 * returns a list of points of this object that intersect other
	 * 
	 * @param other shape to be intersected
	 * @return All points for which other.containsPoint(p)
	 */
	public List<OrientedPoint> getIntersectionPoints(Shape other);
	public default DepthWithDirection getNormalVecAndDepthToSurface(OrientedPoint point){return getNormalVecAndDepthToSurface(point.position, point.orientation);};
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation);
	public boolean intersects(Shape other);
	public boolean containsPoint(Vec2 point);
	public Vec2[] getDrawingVertexes();
	public double getArea();
	public double getInertialArea();
	public Vec2 getCenterOfMass();
	public BoundingBox getBoundingBox();
	
	public Shape transformToCFrame(CFrame frame);
	public Shape scale(double factor);
	
	public CollisionOutline getCollisionOutline(Shape other);
	public Range getBoundsAlongDirection(NormalizedVec2 direction);
	/**
	 * returns the possible Separating Axis Theorem projection directions applicable to this shape. For polygons for example, this will be normalized vectors along it's edges
	 * @return an array of the found directions
	 */
	public NormalizedVec2[] getSATDirections();
	public Shape union(Shape other);
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
	public Shape leftSlice(Vec2 origin, Vec2 direction);
}
