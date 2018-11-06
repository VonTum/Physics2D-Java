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
	public final RigidBody parent;
	
	public Part(RigidBody parent, Shape shape, CFrame relativeCFrame, PhysicalProperties properties) {
		this.shape = shape;
		this.relativeCFrame = relativeCFrame;
		this.parent = parent;
		this.properties = properties;
	}
	
	public CFrame getGlobalCFrame(){
		return parent.getCFrame().localToGlobal(relativeCFrame);
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
	
	public Vec2 getLocalCenterOfMass() {
		return shape.getCenterOfMass();
	}
	
	public boolean containsPoint(Vec2 point) {
		return getGlobalShape().containsPoint(point);
	}
	
	public Vec2 getSpeedOfPoint(Vec2 point){
		return parent.getSpeedOfPoint(point);
	}
	
	public void interactWith(Part other) {
		// All coordinates are local to this.getGlobalCFrame()
		CFrame deltaTransform = getGlobalCFrame().globalToLocal(other.getGlobalCFrame());
		CFrame invTransform = deltaTransform.inv();
		
		
		
		for(Convex c:shape.convexDecomposition()){
			for(Convex oc:other.shape.transformToCFrame(deltaTransform).convexDecomposition()){
				if(!c.getBoundingBox().intersects(oc.getBoundingBox())) continue;
				
				Vec2 travelVec1 = c.getNearestExit(oc);
				if(travelVec1 == null) continue;
				Vec2 travelVec2 = oc.getNearestExit(c);
				if(travelVec2 == null) continue;
				
				Convex intersection = c.intersection(oc);
				
				Vec2 forcePoint = intersection.getCenterOfMass();
				
				Debug.logShape(intersection.transformToCFrame(getGlobalCFrame()), Color.PURPLE.fuzzier(0.2));
				
				Part base, intersector;
				Vec2 intersectDepth;
				if(travelVec1.lengthSquared() < travelVec2.lengthSquared()){
					// use travelVec1, c is base
					
					base = this; intersector = other;
					intersectDepth = travelVec1;
				}else{
					// use travelVec2, oc is base
					
					base = other; intersector = this;
					intersectDepth = invTransform.localToGlobalRotation(travelVec2);
					
					forcePoint = invTransform.localToGlobal(forcePoint);
				}
				
				enactTouchyForce(base, intersector, forcePoint, intersectDepth);
			}
		}
	}
	
	/**
	 * 
	 * @param base
	 * @param intersector
	 * @param forceOrigin relative to base's CFrame
	 * @param intersectDepth
	 */
	private static void enactTouchyForce(Part base, Part intersector, Vec2 bForceOrigin, Vec2 bIntersectDepth){
		CFrame deltaCFrame = base.getGlobalCFrame().globalToLocal(intersector.getGlobalCFrame());
		
		Vec2 iForceOrigin = deltaCFrame.globalToLocal(bForceOrigin);
		//Vec2 iIntersectDepth = deltaCFrame.globalToLocalRotation(bIntersectDepth);
		
		Vec2 forceOrigin = base.getGlobalCFrame().localToGlobal(bForceOrigin);
		Vec2 intersectDepth = base.getGlobalCFrame().localToGlobalRotation(bIntersectDepth);
		
		Vec2 FOLocToBase = base.relativeCFrame.localToGlobal(bForceOrigin).subtract(base.parent.centerOfMassRelative);
		Vec2 FOLocToInter = intersector.relativeCFrame.localToGlobal(iForceOrigin).subtract(intersector.parent.centerOfMassRelative);
		
		Vec2 FORelToBase = base.parent.getCFrame().localToGlobalRotation(FOLocToBase);
		Vec2 FORelToInter = intersector.parent.getCFrame().localToGlobalRotation(FOLocToInter);
		
		double inertiaOfPoint = 1/(1/base.parent.getPointInertia(FORelToBase, intersectDepth) + 
										1/intersector.parent.getPointInertia(FORelToInter, intersectDepth));
		
		// Vec2 relativeVelocity = intersector.getSpeedOfPoint(forceOrigin).subtract(base.getSpeedOfPoint(forceOrigin));
		Vec2 relativeVelocity = intersector.parent.getSpeedOfRelPoint(FORelToInter).subtract(base.parent.getSpeedOfRelPoint(FORelToBase));
		
		// if(relativeVelocity.subtract(relativeVelocity2).lengthSquared() > 1E-20) System.out.println(relativeVelocity + " vs " + relativeVelocity2);
		
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
		
		intersector.parent.actionReaction(base.parent, forceOrigin, baseForce);
		intersector.parent.actionReaction(base.parent, forceOrigin, normalForce);
		intersector.parent.actionReaction(base.parent, forceOrigin, frictionForce);
	}
	
	@Override
	public String toString(){
		return String.format("Part{shape: %s, rCF: %s, properties: %s, parent: %s}", shape, relativeCFrame, properties, parent);
	}
}
