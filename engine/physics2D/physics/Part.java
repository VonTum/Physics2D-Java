package physics2D.physics;

import java.util.List;

import physics2D.geom.Shape;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.OrientedPoint;
import physics2D.math.Vec2;

public class Part {
	
	public final Shape shape;
	public CFrame relativeCFrame;
	public PhysicalProperties properties;
	public final Physical parent;
	
	public Part(Physical parent, Shape shape, CFrame relativeCFrame, PhysicalProperties properties) {
		this.shape = shape;
		this.relativeCFrame = relativeCFrame;
		this.parent = parent;
		this.properties = properties;
	}
	
	public CFrame getGlobalCFrame(){
		return parent.cframe.localToGlobal(relativeCFrame);
	}
	
	public Shape getGlobalShape(){
		return shape.transformToCFrame(getGlobalCFrame());
	}
	
	public double getMass() {
		return shape.getArea() * properties.density;
	}
	
	public double getInertia() {
		return shape.getInertialArea() * properties.density;
	}

	public BoundingBox getBoundingBox() {
		return getGlobalShape().getBoundingBox();
	}

	public Vec2 getCenterOfMass() {
		return getGlobalShape().getCenterOfMass();
	}

	public boolean containsPoint(Vec2 point) {
		return getGlobalShape().containsPoint(point);
	}
	
	public Vec2 getSpeedOfPoint(Vec2 point){
		return parent.getSpeedOfPoint(point);
	}
	
	public List<OrientedPoint> getIntersectionPoints(Part other){
		return getGlobalShape().getIntersectionPoints(other.getGlobalShape());
	}
	
	@Override
	public String toString(){
		return String.format("Part{shape: %s, rCF: %s, properties: %s, parent: %s}", shape, relativeCFrame, properties, parent);
	}
}
