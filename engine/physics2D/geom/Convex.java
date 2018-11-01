package physics2D.geom;

import java.util.ArrayList;
import java.util.List;

import physics2D.math.Range;
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
	 * Returns the shortest distance this Convex would have to move to exit {@code other}
	 * 
	 * movement can only happen along SAT directions
	 * 
	 * @param other
	 * @return
	 */
	public default Vec2 getNearestExit(Convex other){
		Vec2[] SATDirs = getSATDirections();
		
		Vec2 bestDir = new Vec2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		
		for(Vec2 dir:SATDirs){
			Range r1 = getBoundsAlongDirection(dir);
			Range r2 = other.getBoundsAlongDirection(dir);
			if(r1.isDisjunct(r2)) return null;
			double d1 = r2.max-r1.min;
			double d2 = r1.max-r2.min;
			
			double w = (d1 < d2)? -d1:d2;
			
			Vec2 depthVec = dir.reProject(w);
			if(depthVec.lengthSquared() < bestDir.lengthSquared()){
				bestDir = depthVec;
			}
		}
		
		return bestDir.rotate90CounterClockwise();
	}
	
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
