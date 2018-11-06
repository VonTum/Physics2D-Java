package physics2D.physics;

import game.util.Color;
import physics2D.Debug;
import physics2D.geom.Shape;
import physics2D.math.CFrame;
import physics2D.math.Mat2;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class Physical extends RigidBody {
	
	public CFrame cframe;
	
	/** velocity of center of mass */
	public Vec2 velocity = Vec2.ZERO;
	/** angular velocity of center of mass */
	public double angularVelocity = 0;
	
	
	public String name = super.toString();
	
	public Physical(CFrame location){
		cframe = location;
	}
	
	/**
	 * Construct a physical with the given properties for all parts
	 * @param properties
	 * @param shapes
	 */
	public Physical(PhysicalProperties properties, Shape... shapes){
		Vec2 com = Vec2.ZERO;
		double totalArea = 0;
		for(Shape s:shapes){
			double area = s.getArea();
			com = com.add(s.getCenterOfMass().mul(area));
			totalArea += area;
		}
		com = com.div(totalArea);
		cframe = new CFrame(com);
		
		for(Shape s:shapes){
			parts.add(new Part(this, s, CFrame.IDENTITY, properties));
		}
		recalculate();
	}
	
	@Override
	public Vec2 getSpeedOfPoint(Vec2 point) {
		return velocity.add(point.subtract(getCenterOfMass()).cross(-angularVelocity));
	}
	
	public Vec2 getSpeedOfRelPoint(Vec2 relPoint){
		return velocity.add(relPoint.cross(-angularVelocity));
	}
	
	/**
	 * Returns the angular impulse of this object, eg a measure of how much rotational oempf this object has
	 * @return The angular impulse, <code>angularVelocity * inertia</code>
	 */
	public double getAngularImpulse(){
		return angularVelocity * inertia;
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
	 * Applies a given torque impulse to this object, changing it's angular momentum by torqueImpulse/inertia
	 * @param torqueImpulse torque impulse
	 */
	public void applyTorqueImpulse(double torqueImpulse){
		totalMoment += torqueImpulse / inertia;
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
	
	@Override
	public CFrame getCFrame(){return cframe;}
	
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
	@Override
	public void update(double deltaT){
		if(anchored) return;
		
		Vec2 acceleration = getAcceleration();
		double angularAcceleration = getRotAccelertation();
		
		Vec2 movement = velocity.mul(deltaT).add(acceleration.mul(deltaT*deltaT/2));
		double rotation = angularVelocity * deltaT + angularAcceleration*deltaT*deltaT/2;
		
		velocity = velocity.add(acceleration.mul(deltaT));
		angularVelocity += angularAcceleration * deltaT;
		
		Vec2 relCOM = cframe.localToGlobalRotation(centerOfMassRelative);
		
		RotMat2 rot = new RotMat2(rotation);
		
		move(movement.add(relCOM.subtract(rot.mul(relCOM))));
		rotate(rot);
		
		super.update(deltaT);
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
	
	public void move(Vec2 delta){
		cframe = cframe.add(delta);
	}
	
	public void rotate(double angle){
		cframe = cframe.rotated(angle);
	}
	
	public void rotate(RotMat2 rotation){
		cframe = cframe.rotated(rotation);
	}
	
	/**
	 * Returns the acceleration of a point, were it to lie on this object. 
	 * @param point <i>global</i>
	 * @return The acceleration of this point
	 */
	public Vec2 getAccelerationOfPoint(Vec2 point){
		Vec2 relativeDist = point.subtract(getCenterOfMass());
		Vec2 accelerationOfCenterOfMass = getAcceleration();
		Vec2 rotAcceleration = relativeDist.rotate90CounterClockwise().mul(getRotAccelertation());
		Vec2 centripetalAcceleration = relativeDist.mul(-angularVelocity*angularVelocity);
		return Vec2.sum(accelerationOfCenterOfMass, rotAcceleration, centripetalAcceleration);
	}
	
	public Vec2 getAcceleration(){return totalForce.div(mass);}
	public double getRotAccelertation(){return totalMoment / inertia;}
	
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
}
