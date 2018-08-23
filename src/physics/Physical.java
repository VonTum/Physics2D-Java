package physics;

import game.Constants;
import game.Debug;
import game.gui.Describable;
import geom.Shape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import util.Color;
import math.CFrame;
import math.NormalizedVec2;
import math.OrientedPoint;
import math.RotMat2;
import math.Vec2;

public class Physical implements Locatable, Describable {
	public CFrame cframe;
	
	public final List<Shape> shapes;
	
	public Vec2 velocity = Vec2.ZERO;
	public double angularVelocity = 0;
	
	private Vec2 totalForce = Vec2.ZERO;
	private double totalMoment = 0;	// counterclockwise is positive
	
	private double mass;
	private double inertia;
	
	private boolean anchored = false;
	
	public String name = super.toString();
	
	public Physical(Shape... shapes){
		this.shapes = new ArrayList<>();
		for(Shape s:shapes)
			this.shapes.add(s);
		
		recalculate();
		
		for(Shape shape:shapes){
			CFrame relativecframe = this.cframe.globalToLocal(shape.getCFrame());
			shape.attach(this, relativecframe);
		}
	}
	
	public void interactWith(Physical otherObj){
		for(Shape shape:shapes){
			for(Shape otherShape:otherObj.shapes){
				shape.getIntersectionPoints(otherShape).forEach((point) -> {
					double smallestInertia = Math.min(getPointInertia(point), otherObj.getPointInertia(point));
					handleIntersectionPoint(otherShape, smallestInertia, point);
				});
				Stream<? extends OrientedPoint> pointStream = otherShape.getIntersectionPoints(shape);
				pointStream.forEach((point) -> {
					double smallestInertia = Math.min(getPointInertia(point), otherObj.getPointInertia(point));
					otherObj.handleIntersectionPoint(shape, smallestInertia, point);
				});
			}
		}
	}
	
	private void handleIntersectionPoint(Shape otherShape, double smallestInertia, OrientedPoint point) {
		Vec2 position = point.position;
		DepthWithDirection d = otherShape.getNormalVecAndDepthToSurface(point);
		
		NormalizedVec2 normalVec = d.direction;
		
		Debug.logVector(position, normalVec, Color.CYAN.darker());
		
		Vec2 repulsionForce = d.getVecToSurface().mul(Constants.REPULSION_FACTOR*smallestInertia);
		
		Vec2 relativeVelocity = this.getSpeedOfPoint(position).subtract(otherShape.getSpeedOfPoint(position));
		
		double normalComponent = normalVec.dot(relativeVelocity);
		double sidewaysComponent = normalVec.cross(relativeVelocity);
		
		Debug.logVector(position, Vec2.UNITX.mul(sidewaysComponent), Color.GREEN);
		Debug.logVector(position, Vec2.UNITY.mul(normalComponent), Color.RED);
		
		if(normalComponent > 0)
			normalComponent = normalComponent*otherShape.properties.stickyness;
		
		sidewaysComponent = sidewaysComponent * otherShape.properties.friction;
		
		Vec2 normalForce = normalVec.mul(-normalComponent * Constants.VELOCITY_STOP_FACTOR * smallestInertia);
		Vec2 frictionForce = normalVec.rotate90CounterClockwise().mul(-sidewaysComponent * Constants.VELOCITY_STOP_FACTOR * smallestInertia);
		
		this.actionReaction(otherShape.parent, position, repulsionForce);
		this.actionReaction(otherShape.parent, position, normalForce);
		this.actionReaction(otherShape.parent, position, frictionForce);
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
		double movementFactor = 1/getMass();
		double rotationFactor = Math.abs(relativePosition.cross(relativePosition.cross(direction)).dot(direction) / getInertia());
		return 1/(movementFactor + rotationFactor);
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
	public void applyForce(Vec2 force){
		if(anchored) return;
		totalForce = totalForce.add(force);
		
		Debug.logForce(this, Vec2.ZERO, force);
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
		
		Vec2 acceleration = totalForce.div(mass);
		double angularAcceleration = totalMoment / inertia;
		
		Vec2 movement = velocity.mul(deltaT).add(acceleration.mul(deltaT*deltaT/2));
		double rotation = angularVelocity * deltaT + angularAcceleration*deltaT*deltaT/2;
		
		velocity = velocity.add(acceleration.mul(deltaT));
		angularVelocity += angularAcceleration * deltaT;
		
		// Vec2 movement = velocity.mul(deltaT);
		// double rotation = angularVelocity * deltaT;
		
		move(movement);
		rotate(rotation);
		
		totalForce = Vec2.ZERO;
		totalMoment = 0.0;
	}
	
	private void recalculate(){
		recalculateCenterOfMass();
		recalculateInertia();
	}
	
	private void recalculateCenterOfMass(){
		Vec2 weighedAverage = Vec2.ZERO;
		double totalMass = 0;
		
		for(Shape shape:shapes){
			double shapeMass = shape.getMass();
			totalMass += shapeMass;
			weighedAverage = weighedAverage.add(shape.getCenterOfMass().mul(shapeMass));
		}
		
		Vec2 centerOfMass = weighedAverage.div(totalMass);
		RotMat2 rotationMat = shapes.get(0).getCFrame().rotation;
		
		this.cframe = new CFrame(centerOfMass, rotationMat);
		this.mass = totalMass;
	}
	
	private void recalculateInertia(){
		Vec2 objCenter = getCenterOfMass();
		double totalInertia = 0;
		
		for(Shape shape:shapes){
			Vec2 delta = shape.getCenterOfMass().subtract(objCenter);
			
			totalInertia += shape.getInertia() + delta.lengthSquared() * shape.getMass();
		}
		
		this.inertia = totalInertia;
	}
	
	
	
	/**
	 * Detaches the given shape from this polygon, removing it from this, and calling s.detach();
	 * 
	 * @param s the shape to detach
	 * @return true if the shape was detached succesfully
	 */
	public boolean detachShape(Shape s){
		for(Iterator<Shape> shapeIter = this.shapes.iterator(); shapeIter.hasNext(); ){
			Shape shape = shapeIter.next();
			if(shape == s){
				shapeIter.remove();
				s.detach();
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
		return cframe.position;
	}
	
	public void move(Vec2 delta){
		cframe = cframe.add(delta);
	}
	
	public void rotate(double angle){
		cframe = cframe.rotated(angle);
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
	public String toString(){
		return "Physical("+cframe+", mass="+mass+", I="+inertia+", v="+velocity+", w="+angularVelocity+", F="+totalForce+", M="+totalMoment+")";
	}

	@Override
	public String describe() {
		return String.format("%s{\n  cframe: %s\n  mass: %.9f\n  inertia: %.9f\n  velocity: %s\n  angularVel: %s\n  anchored: %s\n  F: %s\n  M: %.9f\n}", 
				name, cframe.describe().replace("\n", "\n  "), mass, inertia, velocity, angularVelocity, anchored, totalForce, totalMoment);
		
	}
}
