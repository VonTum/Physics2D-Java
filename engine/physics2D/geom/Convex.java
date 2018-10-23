package physics2D.geom;

import java.util.ArrayList;
import java.util.List;

import physics2D.math.Vec2;

public interface Convex extends Shape {
	/**
	 * returns the possible Separating Axis Theorem projection directions applicable to this shape. For polygons for example, this will be normalized vectors along it's edges
	 * @return an array of the found directions
	 */
	public Vec2[] getSATDirections();
	public Convex intersection(Convex other);
	public Convex leftSlice(Vec2 origin, Vec2 direction);
	
	/**
	 * Returns a list containing just this
	 */
	@Override
	public default List<? extends Convex> convexDecomposition(){
		List<Convex> convex = new ArrayList<>();
		convex.add(this);
		return convex;
	}
	
	public default boolean intersects(Convex other){
		for(Vec2 direction:getSATDirections())
			if(getBoundsAlongDirection(direction).isDisjunct(other.getBoundsAlongDirection(direction)))
				return false;
		for(Vec2 direction:other.getSATDirections())
			if(getBoundsAlongDirection(direction).isDisjunct(other.getBoundsAlongDirection(direction)))
				return false;
		return true;
	}
	
	@Override
	public default boolean intersects(Shape other){
		for(Convex c:other.convexDecomposition())
			if(intersects(c)) return true;
		return false;
	}
}
