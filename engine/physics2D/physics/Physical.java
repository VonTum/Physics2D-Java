package physics2D.physics;

import game.util.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import physics2D.Debug;
import physics2D.geom.Shape;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.Constants;
import physics2D.math.Mat2;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class Physical implements Locatable {
	public CFrame cframe;
	
	private Vec2 centerOfMassRelative = Vec2.ZERO;
	
	/** velocity of center of mass */
	public Vec2 velocity = Vec2.ZERO;
	/** angular velocity of center of mass */
	public double angularVelocity = 0;
	
	public Vec2 totalForce = Vec2.ZERO;
	public double totalMoment = 0;	// counterclockwise is positive
	
	public double mass;
	public double inertia;
	
	private boolean anchored = false;
	
	public final List<Part> parts = new ArrayList<>();
	
	public String name = super.toString();
	
	private BoundingBox boundsCache;
	
	/*public Physical(Shape... shapes){
		for(Shape s:shapes)
			this.parts.add(new Part(this, s, s.cframe, ObjectLibrary.BASIC));
		
		recalculate();
		
		for(Shape shape:shapes){
			CFrame relativecframe = this.cframe.globalToLocal(shape.getCFrame());
			shape.attach(this, relativecframe);
		}
		
		boundsCache = calculateBoundingBox();
	}*/
	
	public Physical(CFrame location){
		mass = 0;
		inertia = 0;
		cframe = location;
		boundsCache = new BoundingBox(0.0, 0.0, 0.0, 0.0);
	}
	
	public void addPart(Shape s, CFrame relativePos, PhysicalProperties properties){
		parts.add(new Part(this, s, relativePos, properties));
		recalculate();
	}
	
	public void interactWith(Physical otherObj){
		if(this.boundsCache.intersects(otherObj.boundsCache)){
			for(Part cur:parts){
				for(Part other:otherObj.parts){
					cur.getIntersectionPoints(other).forEach((point) -> {
						double smallestInertia = Math.min(getPointInertia(point), otherObj.getPointInertia(point));
						handleIntersectionPoint(other, smallestInertia, point);
					});
					other.getIntersectionPoints(cur).forEach((point) -> {
						double smallestInertia = Math.min(getPointInertia(point), otherObj.getPointInertia(point));
						otherObj.handleIntersectionPoint(cur, smallestInertia, point);
					});
				}
			}
		}
	}
	
	private void handleIntersectionPoint(Part otherPart, double smallestInertia, OrientedPoint point) {
		Vec2 position = point.position;
		DepthWithDirection d = otherPart.getGlobalShape().getNormalVecAndDepthToSurface(point);
		
		NormalizedVec2 normalVec = d.direction;
		
		Debug.logVector(position, normalVec, Color.CYAN.darker());
		
		Vec2 repulsionForce = d.getVecToSurface().mul(Constants.REPULSION_FACTOR*smallestInertia);
		
		Vec2 relativeVelocity = this.getSpeedOfPoint(position).subtract(otherPart.getSpeedOfPoint(position));
		
		double normalComponent = normalVec.dot(relativeVelocity);
		double sidewaysComponent = normalVec.cross(relativeVelocity);
		
		Debug.logVector(position, Vec2.UNITX.mul(sidewaysComponent), Color.GREEN);
		Debug.logVector(position, Vec2.UNITY.mul(normalComponent), Color.RED);
		
		if(normalComponent > 0)
			normalComponent = normalComponent * otherPart.properties.stickyness;
		
		sidewaysComponent = sidewaysComponent * otherPart.properties.friction;
		
		Vec2 normalForce = normalVec.mul(-normalComponent * Constants.VELOCITY_STOP_FACTOR * smallestInertia);
		Vec2 frictionForce = normalVec.rotate90CounterClockwise().mul(-sidewaysComponent * Constants.VELOCITY_STOP_FACTOR * smallestInertia);
		
		this.actionReaction(otherPart.parent, position, repulsionForce);
		this.actionReaction(otherPart.parent, position, normalForce);
		this.actionReaction(otherPart.parent, position, frictionForce);
	}
	
	public void actionReaction(Physical other, Vec2 globalPos, Vec2 force){
		applyForce(force, globalPos);
		other.applyForce(force.neg(), globalPos);
	}
	
	public Vec2 getSpeedOfPoint(Vec2 point) {
		return velocity.add(point.subtract(getCenterOfMass()).cross(-angularVelocity));
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
	
	public double getAngularImpulse(){
		return angularVelocity * inertia;
	}
	
	/**
	 * Returns a matrix describing the given point's reaction to a force
	 * 
	 * PointAccel = M*Force
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
	
	public Vec2 getConcentratedForceInPoint(Vec2 point){
		return totalForce.add(point.subtract(getCenterOfMass()).cross(-totalMoment));
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
		
		Debug.logForce(this, cframe.globalToLocal(attachment), force);
	}
	
	/**
	 * Applies a given force to the object. Applied at the object's center of mass
	 * 
	 * @param force force vector to be applied <br><i>local to this physical</i>
	 */
	public void applyForceAtCenterOfMass(Vec2 force){
		if(anchored) return;
		totalForce = totalForce.add(force);
		
		Debug.logForce(this, Vec2.ZERO, force);
	}
	
	/**
	 * Applies a given impulse at the given position. 
	 * 
	 * Afterwards the speed of the given point will have changed by<br><br>
	 * <code>impulse/getPointInertia(attachment, impulse.normalize())</code>
	 * 
	 * @param impulse
	 * @param attachment the point at which the impulse must attach, global coordinates
	 */
	public void applyImpulse(Vec2 impulse, Vec2 attachment){
		if(anchored) return;
		
		velocity = velocity.add(impulse.div(mass));
		angularVelocity += attachment.subtract(getCenterOfMass()).cross(impulse) / inertia;
	}
	
	/**
	 * Applies the given impulse at the center of mass, accellerating the object by 
	 * <code>impulse/mass</code>
	 * @param impulse
	 */
	public void applyImpulseAtCenterOfMass(Vec2 impulse){
		if(anchored) return;
		velocity = velocity.add(impulse.div(mass));
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
	
	/**
	 * Applies a given torque impulse to this object, changing it's angular momentum by torqueImpulse/inertia
	 * @param torqueImpulse torque impulse
	 */
	public void applyTorqueImpulse(double torqueImpulse){
		totalMoment += torqueImpulse / inertia;
	}
	
	/**
	 * Updates the attributes of this physical according to the applied forces. <br>
	 * Then resets the total force and momentum. <br><br>
	 * 
	 * position += initialVel*dT + (acceleration*dT^2)/2<br>
	 * rotation += initialAngularVel*dT + (angularAcceleration*dT^2)/2<br><br>
	 * 
	 * velocity += acceleration * dT<br>
	 * angularVel += angularAcceleration * dT<br><br>
	 * 
	 * force = 0<br>
	 * moment = 0<br><br>
	 * 
	 * @param deltaT time interval to next frame
	 */
	public void update(double deltaT){
		if(anchored) return;
		
		Vec2 acceleration = getAcceleration();
		double angularAcceleration = getRotAccelertation();
		
		Vec2 movement = velocity.mul(deltaT).add(acceleration.mul(deltaT*deltaT/2));
		double rotation = angularVelocity * deltaT + angularAcceleration*deltaT*deltaT/2;
		
		velocity = velocity.add(acceleration.mul(deltaT));
		angularVelocity += angularAcceleration * deltaT;
		
		// Vec2 movement = velocity.mul(deltaT);
		// double rotation = angularVelocity * deltaT;
		
		Vec2 relCOM = cframe.localToGlobalRotation(centerOfMassRelative);
		
		RotMat2 rot = new RotMat2(rotation);
		
		move(movement.add(relCOM.subtract(rot.mul(relCOM))));
		rotate(rot);
		
		totalForce = Vec2.ZERO;
		totalMoment = 0.0;
		
		boundsCache = calculateBoundingBox();
	}
	
	private BoundingBox calculateBoundingBox(){
		BoundingBox[] boxes = new BoundingBox[parts.size()];
		for(int i = 0; i < parts.size(); i++)
			boxes[i] = parts.get(i).getBoundingBox();
		
		return BoundingBox.mergeBoxes(boxes);
	}
	
	public BoundingBox getBoundingBox(){return boundsCache;}
	
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
			weighedAverage = weighedAverage.add(p.getCenterOfMass().mul(partMass));
		}
		
		Vec2 centerOfMass = weighedAverage.div(totalMass);
		
		Vec2 COMRelative = this.cframe.globalToLocal(centerOfMass);
		
		this.centerOfMassRelative = COMRelative;
		
		/*RotMat2 rotationMat = parts.get(0).relativeCFrame.rotation;
		
		CFrame newCFrame = new CFrame(centerOfMass, rotationMat);
		
		for(Part p:parts)
			p.relativeCFrame = newCFrame.globalToLocal(this.cframe.localToGlobal(p.relativeCFrame));
		
		this.cframe = newCFrame;*/
		
		//for(Part p:parts)
		// 	p.relativeCFrame = p.relativeCFrame.add(COMRelative.neg());
		
		
		this.mass = totalMass;
	}
	
	private void recalculateInertia(){
		Vec2 objCenter = getCenterOfMass();
		double totalInertia = 0;
		
		for(Part p:parts){
			Vec2 delta = p.getCenterOfMass().subtract(objCenter);
			
			totalInertia += p.getInertia() + delta.lengthSquared() * p.getMass();
		}
		
		this.inertia = totalInertia;
	}
	
	
	
	/**
	 * Detaches the given part from this polygon, removing it from this;
	 * 
	 * @param p the part to detach
	 * @return true if the shape was detached succesfully
	 */
	public boolean detachShape(Part p){
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
	
	public double getKineticEnergy(){
		return getMass()*velocity.lengthSquared()/2 + getInertia()*angularVelocity*angularVelocity/2;
	}
	
	public double getPotentialEnergy(Vec2 gravity){
		return -getMass()*getCenterOfMass().dot(gravity);
	}
	
	public double getEnergy(Vec2 gravity){
		return getKineticEnergy()+getPotentialEnergy(gravity);
	}
	
	@Override
	public CFrame getCFrame(){return cframe;}
	
	public Vec2 getCenterOfMass(){
		return cframe.localToGlobal(centerOfMassRelative);
	}
	
	public void move(Vec2 delta){
		cframe = cframe.add(delta);
	}
	
	public void rotate(double angle){
		cframe = cframe.rotated(angle);
	}
	
	public void rotate(RotMat2 rotation){
		cframe = cframe.rotated(rotation);
	}
	
	public Vec2 getAccelerationOfPoint(Vec2 point){
		Vec2 relativeDist = point.subtract(getCenterOfMass());
		Vec2 accelerationOfCenterOfMass = getAcceleration();
		Vec2 rotAcceleration = relativeDist.rotate90CounterClockwise().mul(getRotAccelertation());
		Vec2 centripetalAcceleration = relativeDist.mul(-angularVelocity*angularVelocity);
		return Vec2.sum(accelerationOfCenterOfMass, rotAcceleration, centripetalAcceleration);
	}
	
	public Vec2 getAcceleration(){return totalForce.div(mass);}
	public double getRotAccelertation(){return totalMoment / inertia;}
	/**
	 * 
	 * @param relativePoint relative to Center Of Mass
	 * @return acceleration of the given point
	 */
	// public Vec2 getAccelerationOfPoint(Vec2 relativePoint){}
	
	public void anchor() {anchored = true;}
	public void unAnchor() {anchored = false;}
	public boolean isAnchored(){return anchored;}

	public double getMass() {return mass;}
	public double getInertia() {return inertia;}
	
	

	@Override
	public String toString() {
		return String.format("%s{\n  cframe: %s\n  mass: %.9f\n  inertia: %.9f\n  velocity: %s\n  angularVel: %s\n  anchored: %s\n  F: %s\n  M: %.9f\n}", 
				name, cframe.describe().replace("\n", "\n  "), mass, inertia, velocity, angularVelocity, anchored, totalForce, totalMoment);
	}
	
	/**
	 * Gets the desired speed towards a point, whereby slowing the object down at <code>acceleration</code>
	 * it will come to a complete stop at relativePoint
	 * 
	 * @param relativePoint the point towards which to be accelerated
	 * @param acceleration
	 * @return
	 */
	public static Vec2 getDesiredSpeedTowardsPoint(Vec2 relativePoint, double acceleration){
		return relativePoint.mul(acceleration);
	}
	
	/**
	 * Returns the force by which to pull the object towards a point with a max acceleration
	 * 
	 * @param attachPoint position in local frame, where the force must attach
	 * @param pullPoint global point towards to be pulled
	 * @param pullPointVelocity velocity of pullPoint
	 * @param acceleration maximum acceleration
	 * @return
	 */
	public Vec2 getPullForceTowardsPointDampened(Vec2 attachPoint, Vec2 pullPoint, Vec2 pullPointVelocity, double acceleration){
		/*Vec2 desiredSpeed = getDesiredSpeedTowardsPoint(relativePos, acceleration).add(pullPointVelocity);
		
		Vec2 thisPointVelocity = getSpeedOfPoint(relativePos.add(getCenterOfMass()));
		
		Vec2 delta = thisPointVelocity.subtract(desiredSpeed).maxLength(acceleration);
		
		double inertia = getPointInertia(relativePos, delta.normalize());
		
		Vec2 force = delta.mul(inertia);
		
		return force;*/
		
		Vec2 globalAttach = cframe.localToGlobal(attachPoint);
		Vec2 vecToDest = pullPoint.subtract(globalAttach);
		
		Vec2 desiredSpeed = getDesiredSpeedTowardsPoint(vecToDest, acceleration*0.03).add(pullPointVelocity);
		
		Vec2 currentSpeed = getSpeedOfPoint(globalAttach);
		
		Vec2 deltaSpeed = desiredSpeed.subtract(currentSpeed);
		
		Vec2 accelerationVec = deltaSpeed.maxLength(acceleration).subtract(getAccelerationOfPoint(globalAttach));
		
		Mat2 inertiaMat = getPointInertialMatrix(globalAttach);
		
		Vec2 force = inertiaMat.inv().mul(accelerationVec);
		
		Debug.logVector(globalAttach, desiredSpeed, Color.ORANGE);
		Debug.logVector(globalAttach, currentSpeed, Color.GREEN);
		Debug.logVector(globalAttach, force, Color.RED);
		Debug.logVector(globalAttach.add(currentSpeed), deltaSpeed, Color.BLUE);
		
		
		return force;
		
		// return pullPoint.subtract(cframe.localToGlobal(attachPoint)).mul(acceleration*mass);
	}

	public void actionRotaction(Physical other, double torque) {
		this.applyTorque(torque/2);
		other.applyTorque(-torque/2);
	}
}
