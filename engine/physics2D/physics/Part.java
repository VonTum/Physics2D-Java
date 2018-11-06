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
				Vec2 otherForcePoint = invTransform.localToGlobal(forcePoint);
				
				Debug.logShape(intersection.transformToCFrame(getGlobalCFrame()), Color.PURPLE.fuzzier(0.2));
				
				
				if(travelVec1.lengthSquared() < travelVec2.lengthSquared()){
					// use travelVec1, c is base
					
					Vec2 intersectDepth = getGlobalCFrame().localToGlobalRotation(travelVec1);
					
					enactTouchyForce(this, other, forcePoint, otherForcePoint, intersectDepth);
				}else{
					// use travelVec2, oc is base
					
					Vec2 intersectDepth = getGlobalCFrame().localToGlobalRotation(travelVec2);
					
					enactTouchyForce(other, this, otherForcePoint, forcePoint, intersectDepth);
				}
			}
		}
	}
	
	/**
	 * @param base
	 * @param intersector
	 * @param bForceOrigin relative to base's CFrame
	 * @param iForceOrigin relative to intersector's CFrame
	 * @param intersectDepth in global axes
	 */
	private static void enactTouchyForce(Part base, Part intersector, Vec2 bForceOrigin, Vec2 iForceOrigin, Vec2 intersectDepth){
		
		Vec2 FOLocToBase = base.relativeCFrame.localToGlobal(bForceOrigin).subtract(base.parent.centerOfMassRelative);
		Vec2 FOLocToInter = intersector.relativeCFrame.localToGlobal(iForceOrigin).subtract(intersector.parent.centerOfMassRelative);
		
		Vec2 FORelToBase = base.parent.getCFrame().localToGlobalRotation(FOLocToBase);
		Vec2 FORelToInter = intersector.parent.getCFrame().localToGlobalRotation(FOLocToInter);
		
		double baseInertia = base.parent.getPointInertia(FORelToBase, intersectDepth);
		double intersectorInertia = intersector.parent.getPointInertia(FORelToInter, intersectDepth);
		
		double inertiaOfPoint = 1/(1/baseInertia + 1/intersectorInertia);
		
		Vec2 relativeVelocity = intersector.parent.getSpeedOfRelPoint(FORelToInter).subtract(base.parent.getSpeedOfRelPoint(FORelToBase));
		
		double normalComponent = intersectDepth.dot(relativeVelocity);
		normalComponent *= (normalComponent > 0)? base.properties.getStickynessWith(intersector.properties) : 1.0;
		
		double sidewaysComponent = intersectDepth.cross(relativeVelocity) * base.properties.getFrictionWith(intersector.properties);
		
		double normalAccel = Constants.REPULSION_FACTOR - normalComponent * Constants.VELOCITY_STOP_FACTOR / intersectDepth.lengthSquared();
		
		Vec2 normalForce = intersectDepth.mul(normalAccel*inertiaOfPoint);
		
		Vec2 frictionForce = intersectDepth.rotate90CounterClockwise().reProject(-sidewaysComponent * Constants.VELOCITY_STOP_FACTOR * inertiaOfPoint);
		
		Vec2 totalForce = Vec2.sum(normalForce, frictionForce);

		base.parent.applyForceRelative(totalForce.neg(), FORelToBase);
		intersector.parent.applyForceRelative(totalForce, FORelToInter);
	}
	
	@Override
	public String toString(){
		return String.format("Part{shape: %s, rCF: %s, properties: %s, parent: %s}", shape, relativeCFrame, properties, parent);
	}
}
