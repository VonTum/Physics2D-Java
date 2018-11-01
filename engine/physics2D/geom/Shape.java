package physics2D.geom;

import java.util.List;

import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.Range;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface Shape {
	
	public default boolean intersects(Shape other){
		//quickfail on disjunct bounding boxes
		if(!getBoundingBox().intersects(other.getBoundingBox())) return false;
		
		List<? extends Convex> decomp = this.convexDecomposition();
		List<? extends Convex> otherDecomp = other.convexDecomposition();
		
		BoundingBox[] bounds = new BoundingBox[decomp.size()];
		for(int i = 0; i < decomp.size(); i++)
			bounds[i] = decomp.get(i).getBoundingBox();
		BoundingBox[] otherBounds = new BoundingBox[otherDecomp.size()];
		for(int i = 0; i < otherDecomp.size(); i++)
			otherBounds[i] = otherDecomp.get(i).getBoundingBox();
		
		for(int i = 0; i < decomp.size(); i++)
			for(int j = 0; j < otherDecomp.size(); j++)
				if(bounds[i].intersects(otherBounds[j]))
					if(decomp.get(i).intersects(otherDecomp.get(j)))
						return true;
			
		return false;
	}
	public boolean containsPoint(Vec2 point);
	public Vec2[] getDrawingVertexes();
	public double getArea();
	public double getInertialArea();
	public Vec2 getCenterOfMass();
	public BoundingBox getBoundingBox();
	
	public Shape transformToCFrame(CFrame frame);
	public Shape translate(Vec2 offset);
	public Shape rotate(RotMat2 rotation);
	public default Shape rotate(double angle){return rotate(RotMat2.rotTransform(angle));};
	public Shape scale(double factor);
	
	public Range getBoundsAlongDirection(Vec2 direction);
	
	public default Shape union(Shape other){throw new NotImplementedException();};
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
	//public Shape leftSlice(Vec2 origin, Vec2 direction);
	/**
	 * returns a convex decomposition of this object, may be an approximation
	 */
	public List<? extends Convex> convexDecomposition();
}
