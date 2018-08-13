package geom;

import java.util.Iterator;
import java.util.stream.Stream;

import physics.DepthWithDirection;
import physics.Locatable;
import physics.Physical;
import physics.PhysicalProperties;
import math.BoundingBox;
import math.CFrame;
import math.NormalizedVec2;
import math.OrientedPoint;
import math.Vec2;

public abstract class Shape implements Locatable, Iterable<Vec2> {
	private static final double SPACING = 0.1;
	
	/**
	 * this Shape's parent physical, if null then cframe is in world coordinates, 
	 * else it's in relative coordinates to it's parent. 
	 */
	public Physical parent = null;
	private CFrame cframe;
	
	public final PhysicalProperties properties;
	
	
	public Shape(PhysicalProperties properties, CFrame cframe){
		this.properties = properties;
		this.cframe = cframe;
	}
	
	public void attach(Physical parent, CFrame relativeCFrame){
		this.parent = parent;
		this.cframe = relativeCFrame;
	}
	
	public void detach(){
		if(parent != null){
			parent.detachShape(this);
			cframe = getCFrame();
			parent = null;
		}
	}
	
	@Override
	public CFrame getCFrame(){
		if(parent == null)
			return cframe;
		else
			return parent.cframe.localToGlobal(cframe);
	}
	
	public Vec2 getSpeedOfPoint(Vec2 point){
		if(parent == null)
			return Vec2.ZERO;
		else
			return parent.getSpeedOfPoint(point);
	}
	
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
	public double getMass(){
		return properties.density * getArea();
	}
	public abstract double getArea();
	public abstract double getInertia();
	public abstract Vec2 getCenterOfMass();
	public abstract BoundingBox getBoundingBox();
	public Iterator<Vec2> iterator(){
		return new Iterator<Vec2>() {
			final BoundingBox bounds = getBoundingBox();
			double curX = bounds.xmin;
			double curY = bounds.ymin;
			@Override
			public boolean hasNext() {
				return curX < bounds.xmax && curY < bounds.ymax;
			}
			
			@Override
			public Vec2 next() {
				Vec2 v;
				do{
					v = new Vec2(curX, curY);
					curX += bounds.getWidth() * SPACING;
					if(curX >= bounds.xmax){
						curX = bounds.xmin;
						curY += bounds.getHeight() * SPACING;
					}
				}while(!containsPoint(v) && hasNext());
				return v;
			}
		};
	}
}
