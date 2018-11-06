package physics2D.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import physics2D.Debug;
import physics2D.geom.Shape;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.Mat2;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Vec2;

public abstract class RigidBody implements Locatable {
	
	public double mass = 0;
	public double inertia = 0;
	
	/** cumulative force that has been applied so far this tick */
	public Vec2 totalForce = Vec2.ZERO;
	/** cumulative moment that has been applied so far this tick<br>counterclockwise is positive */
	public double totalMoment = 0;
	
	public boolean anchored = false;
	
	public final List<Part> parts = new ArrayList<>();
	
	public Vec2 centerOfMassRelative = Vec2.ZERO;
	
	protected BoundingBox boundsCache = new BoundingBox(0.0, 0.0, 0.0, 0.0);
	
	public RigidBody(){}
	
	public void addPart(Shape s, CFrame relativePos, PhysicalProperties properties){
		parts.add(new Part(this, s, relativePos, properties));
		recalculate();
	}
	
	/**
	 * Detaches the given part from this polygon, removing it from this;
	 * 
	 * @param p the part to detach
	 * @return true if the shape was detached succesfully
	 */
	public boolean detachPart(Part p){
		for(Iterator<Part> partIter = this.parts.iterator(); partIter.hasNext(); ){
			Part part = partIter.next();
			if(part == p){
				partIter.remove();
				recalculate();
				return true;
			}
		}
		
		return false;
	}
	
	public void recalculate(){
		recalculateCenterOfMass();
		recalculateInertia();
		boundsCache = calculateBoundingBox();
	}
	
	private void recalculateCenterOfMass(){
		Vec2 weighedAverage = Vec2.ZERO;
		double totalMass = 0;
		
		for(Part p:parts){
			double partMass = p.getMass();
			totalMass += partMass;
			Vec2 weighedCOM = p.relativeCFrame.localToGlobal(p.getLocalCenterOfMass()).mul(partMass);
			weighedAverage = weighedAverage.add(weighedCOM);
		}
		
		Vec2 centerOfMass = weighedAverage.div(totalMass);
		
		this.centerOfMassRelative = centerOfMass;
		
		this.mass = totalMass;
	}
	
	private void recalculateInertia(){
		double totalInertia = 0;
		
		for(Part p:parts){
			Vec2 localCOM = p.relativeCFrame.localToGlobal(p.getLocalCenterOfMass());
			Vec2 delta = localCOM.subtract(centerOfMassRelative);
			
			totalInertia += p.getInertia() + delta.lengthSquared() * p.getMass();
		}
		
		this.inertia = totalInertia;
	}
	
	private BoundingBox calculateBoundingBox(){
		BoundingBox[] boxes = new BoundingBox[parts.size()];
		for(int i = 0; i < parts.size(); i++)
			boxes[i] = parts.get(i).getBoundingBox();
		
		return BoundingBox.mergeBoxes(boxes);
	}
	
	public BoundingBox getBoundingBox(){return boundsCache;}
	
	@Override
	public abstract CFrame getCFrame();
	
	public Vec2 getLocalCenterOfMass(){
		return centerOfMassRelative;
	}
	
	public Vec2 getCenterOfMass(){
		return getCFrame().localToGlobal(centerOfMassRelative);
	}
	
	public abstract Vec2 getSpeedOfPoint(Vec2 point);
	public abstract Vec2 getSpeedOfRelPoint(Vec2 relPoint);
	
	public void update(double deltaT){
		
		totalForce = Vec2.ZERO;
		totalMoment = 0.0;
		
		boundsCache = calculateBoundingBox();
	}
	
	public void interactWith(RigidBody otherObj){
		if(this.boundsCache.intersects(otherObj.boundsCache)){
			for(Part cur:parts){
				for(Part other:otherObj.parts){
					cur.interactWith(other);
				}
			}
		}
	}
	
	/**
	 * Applies a given force to the object.
	 * 
	 * @param force force vector to be applied
	 * @param attachment origin of the force <br><i>global</i>
	 */
	public void applyForce(Vec2 force, Vec2 attachment){
		if(anchored) return;
		Vec2 relative = attachment.subtract(getCenterOfMass());
		totalForce = totalForce.add(force);
		totalMoment += relative.cross(force);
		
		Debug.logForce(this, getCFrame().globalToLocal(attachment), force);
	}
	
	public void actionReaction(RigidBody other, Vec2 globalPos, Vec2 force){
		applyForce(force, globalPos);
		other.applyForce(force.neg(), globalPos);
	}
	
	public void actionRotaction(RigidBody other, double torque) {
		this.applyTorque(torque/2);
		other.applyTorque(-torque/2);
	}
	
	/**
	 * Applies a given force to the object. Applied at the object's center of mass
	 * 
	 * @param force force vector to be applied <br><i>local to this physical</i>
	 */
	public void applyForceAtCenterOfMass(Vec2 force){
		if(anchored) return;
		totalForce = totalForce.add(force);
		
		Debug.logForce(this, getCFrame().globalToLocal(getCenterOfMass()), force);
	}
	
	/**
	 * Applies a given torque to the object.
	 * 
	 * @param torque torque
	 */
	public void applyTorque(double torque){
		if(anchored) return;
		totalMoment += torque;
	}
	
	public double getPointInertia(OrientedPoint point){
		return getPointInertia(point.position.subtract(getCenterOfMass()), point.orientation);
	}
	/**
	 * gets the point inertia of a given point in a given direction, the ratio of Force to Acceleration
	 * 
	 * getPointInertia(...) == F / a
	 * 
	 * @param relativePosition position relative to center of mass
	 * @param direction direction, absolute
	 * @return The inertia of the given point in the given direction
	 */
	public double getPointInertia(Vec2 relativePosition, NormalizedVec2 direction){
		if(anchored) return Double.POSITIVE_INFINITY;
		double movementFactor = 1/getMass();
		double rotationFactor = Math.abs(relativePosition.cross(relativePosition.cross(direction)).dot(direction) / getInertia());
		return 1/(movementFactor + rotationFactor);
	}
	
	/**
	 * gets the point inertia of a given point in a given direction, the ratio of Force to Acceleration
	 * 
	 * getPointInertia(...) == F / a
	 * 
	 * @param relativePosition position relative to center of mass
	 * @param direction direction, absolute
	 * @return The inertia of the given point in the given direction
	 */
	public double getPointInertia(Vec2 relativePosition, Vec2 direction){
		if(anchored) return Double.POSITIVE_INFINITY;
		double movementFactor = 1/getMass();
		double rotationFactor = Math.abs(relativePosition.cross(relativePosition.cross(direction)).dot(direction) / (getInertia()*direction.lengthSquared()));
		return 1/(movementFactor + rotationFactor);
	}
	
	public double getLocalPointInertia(Vec2 localPosition, Vec2 localDirection){
		if(anchored) return Double.POSITIVE_INFINITY;
		Vec2 relativePosition = getCFrame().localToGlobalRotation(localPosition.subtract(centerOfMassRelative));
		Vec2 direction = getCFrame().localToGlobalRotation(localDirection);
		double movementFactor = 1/getMass();
		double rotationFactor = Math.abs(relativePosition.cross(relativePosition.cross(direction)).dot(direction) / (getInertia()*direction.lengthSquared()));
		return 1/(movementFactor + rotationFactor);
	}
	
	/**
	 * Returns a matrix describing the given point's reaction to a force
	 * 
	 * <code>PointAccel = M*Force</code>
	 * 
	 * @param point <i>global</i>
	 * @return
	 */
	public Mat2 getPointInertialMatrix(Vec2 point) {
		Vec2 relativePoint = point.subtract(getCenterOfMass());
		double x = relativePoint.x;
		double y = relativePoint.y;
		
		Mat2 massFactor = Mat2.IDENTITY.mul(1/mass);
		Mat2 rotFactor = new Mat2(-y*y, x*y, x*y, -x*x).mul(-1/inertia);
		
		return massFactor.add(rotFactor);
	}
	
	public void anchor() {anchored = true;}
	public void unAnchor() {anchored = false;}
	public boolean isAnchored(){return anchored;}

	public double getMass() {return mass;}
	public double getInertia() {return inertia;}
}
