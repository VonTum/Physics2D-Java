package physics2D.physics;

import game.util.Color;

import physics2D.Debug;
import physics2D.geom.Convex;
import physics2D.geom.Shape;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.Constants;
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
	
	public void interactWith(Part other) {
		for(Convex c:getGlobalShape().convexDecomposition()){
			for(Convex oc:other.getGlobalShape().convexDecomposition()){
				if(!c.getBoundingBox().intersects(oc.getBoundingBox())) continue;
				
				Vec2 travelVec1 = c.getNearestExit(oc);
				if(travelVec1 == null) continue;
				Vec2 travelVec2 = oc.getNearestExit(c);
				if(travelVec2 == null) continue;
				
				Convex intersection = c.intersection(oc);
				
				Vec2 forcePoint = intersection.getCenterOfMass();
				
				Debug.logShape(intersection, Color.PURPLE.fuzzier(0.2));

				Part applier, applied;
				Vec2 intersectDepth;
				if(travelVec1.lengthSquared() < travelVec2.lengthSquared()){
					// use travelVec1, c is base
					
					applier = other; applied = this;
					intersectDepth = travelVec1;
					
					Debug.logVector(forcePoint, travelVec1, Color.GREEN);
				}else{
					// use travelVec2, oc is base
					
					applier = this; applied = other;
					intersectDepth = travelVec2;
					
					Debug.logVector(forcePoint, travelVec2, Color.RED);
				}
				
				enactTouchyForce(applier, applied, forcePoint, intersectDepth);
			}
		}
	}
	
	private static void enactTouchyForce(Part base, Part intersector, Vec2 forceOrigin, Vec2 intersectDepth){
		double inertiaOfPoint = 1/(1/base.parent.getPointInertia(forceOrigin.subtract(base.parent.getCenterOfMass()), intersectDepth) + 
								1/intersector.parent.getPointInertia(forceOrigin.subtract(intersector.parent.getCenterOfMass()), intersectDepth));
		
		Vec2 relativeVelocity = base.getSpeedOfPoint(forceOrigin).subtract(intersector.getSpeedOfPoint(forceOrigin));
		
		double normalComponent = intersectDepth.dot(relativeVelocity);
		double sidewaysComponent = intersectDepth.cross(relativeVelocity);
		
		
		
		if(normalComponent > 0)
			normalComponent = normalComponent * base.properties.stickyness;
		
		sidewaysComponent = sidewaysComponent * base.properties.friction;
		
		Vec2 normalForce = intersectDepth.reProject(-normalComponent * Constants.VELOCITY_STOP_FACTOR * inertiaOfPoint);
		Vec2 frictionForce = intersectDepth.rotate90CounterClockwise().reProject(-sidewaysComponent * Constants.VELOCITY_STOP_FACTOR * inertiaOfPoint);
		
		Debug.logVector(forceOrigin, normalForce, Color.RED);
		Debug.logVector(forceOrigin, frictionForce, Color.GREY);
		
		Vec2 baseForce = intersectDepth.mul(Constants.REPULSION_FACTOR*inertiaOfPoint);
		
		base.parent.actionReaction(intersector.parent, forceOrigin, baseForce);
		base.parent.actionReaction(intersector.parent, forceOrigin, normalForce);
		base.parent.actionReaction(intersector.parent, forceOrigin, frictionForce);
	}
	
	@Override
	public String toString(){
		return String.format("Part{shape: %s, rCF: %s, properties: %s, parent: %s}", shape, relativeCFrame, properties, parent);
	}
}
