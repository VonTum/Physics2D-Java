package physics2D.physics;

import game.util.Color;

import java.util.ArrayList;

import physics2D.Debug;
import physics2D.math.Constants;
import physics2D.math.Vec2;


public class World {
	public Vec2 gravity = new Vec2(0, -2);
	public final double AIRFRICTIONFACTOR = 0.01;
	public final double AIRROTFRICTIONFACTOR = 0.001;
	
	public final ArrayList<Physical> physicals = new ArrayList<>();
	public final ArrayList<Constraint> constraints = new ArrayList<>();
	
	private Vec2 magnetTarget = null;
	private Physical magnetSubject = null;
	private Vec2 magnetAttachPoint = null;
	private final Object magnetLock = new Object();
	
	public World(Vec2 gravity){
		this.gravity = gravity;
	}
	
	public synchronized void addObject(Physical... objects){
		for(Physical p:objects)
			physicals.add(p);
	}
	
	public synchronized boolean removeObject(Part part) {
		for(int i = 0; i < physicals.size(); i++){
			Physical p = physicals.get(i);
			if(p.parts.remove(part)){
				if(p.parts.size() == 0){
					physicals.remove(i);
					
					for(int j = 0; j < constraints.size(); j++){
						Constraint c = constraints.get(j);
						if(c.part1 == p || c.part2 == p){
							constraints.remove(j);
						}
					}
				}else{
					p.recalculate();
				}
				return true;
			}
		}
		return false;
	}
	
	public synchronized void addConstraint(Constraint... consts){
		for(Constraint c:consts)
			constraints.add(c);
	}
	
	public synchronized void tick(double deltaT) {
		updatePhysicals(deltaT);
		applyExternalForces(deltaT);
		computeInteractions(deltaT);
		executeConstraints(deltaT);
	}
	
	private void updatePhysicals(double deltaT) {
		for(Physical p:physicals){
			p.update(deltaT);
		}
	}
	
	private void applyExternalForces(double deltaT) {
		for(Physical p:physicals){
			// gravity
			p.applyForceAtCenterOfMass(gravity.mul(p.getMass()));
		}
	}
	
	private void computeInteractions(double deltaT) {
		for(int i = 0; i < physicals.size(); i++){
			for(int j = i+1; j < physicals.size(); j++){
				physicals.get(i).interactWith(physicals.get(j));
				Debug.logInteraction(physicals.get(i), physicals.get(j));
			}
		}
	}
	
	private void executeConstraints(double deltaT){
		synchronized (magnetLock) {
			if(magnetSubject != null){
				Vec2 attachPoint = magnetSubject.cframe.localToGlobal(magnetAttachPoint);
				Vec2 delta = magnetTarget.subtract(attachPoint);
				
				Debug.logPoint(attachPoint, Color.RED);
				Debug.logVector(attachPoint, delta, Color.GREEN);
				
				double rotTorque = -magnetSubject.angularVelocity * Constants.MAGNET_ROTATION_CANCEL_FACTOR * magnetSubject.inertia;
				
				magnetSubject.applyTorque(rotTorque);
				
				Vec2 relSpeedForce = magnetSubject.getSpeedOfPoint(attachPoint).mul(-Constants.MAGNET_MOVEMENT_CANCEL_FACTOR);
				
				if(relSpeedForce.dot(delta) > 0){
					relSpeedForce.add(delta.mul(relSpeedForce.dot(delta)));
				}
				
				Vec2 deltaForce = delta.mul(Constants.MAGNET_STRENGTH);
				Vec2 relAttach = attachPoint.subtract(magnetSubject.getCenterOfMass());
				
				if(!deltaForce.equals(Vec2.ZERO)) magnetSubject.applyForce(deltaForce.mul(magnetSubject.getPointInertia(relAttach, deltaForce)), attachPoint);
				if(!relSpeedForce.equals(Vec2.ZERO)) magnetSubject.applyForce(relSpeedForce.mul(magnetSubject.getPointInertia(relAttach, relSpeedForce)), attachPoint);
				
				
				
				/*double accel = 100;
				Vec2 force = magnetSubject.getPullForceTowardsPointDampened(magnetAttachPoint, magnetTarget, Vec2.ZERO, accel);
				
				System.out.println(force);
				
				magnetSubject.applyForce(force, magnetSubject.cframe.localToGlobal(magnetAttachPoint));*/
			}
		}
		
		for(Constraint c:constraints){
			c.enact();
		}
	}
	
	/**
	 * Returns the first part found containing the given coordinate
	 * If none found then <code>null</code>
	 * 
	 * @param worldPos
	 * @return the selected part
	 */
	public Part getPartAt(Vec2 worldPos){
		for(Physical p:physicals)
			for(Part subPart:p.parts)
				if(subPart.containsPoint(worldPos))
					return subPart;
		
		return null;
	}
	
	public void grabBlock(Vec2 mousePos){
		synchronized (magnetLock) {
			for(Physical b:physicals){
				for(Part subPart:b.parts)
					if(subPart.containsPoint(mousePos)){
						magnetSubject = b;
						magnetAttachPoint = b.cframe.globalToLocal(mousePos);
						magnetTarget = mousePos;
					}
			}
		}
	}
	
	public void dragBlock(Vec2 newMousePos){
		synchronized (magnetLock) {
			magnetTarget = newMousePos;
		}
	}
	
	public void dropBlock(){
		synchronized (magnetLock) {
			magnetSubject = null;
			magnetAttachPoint = null;
		}
	}
}
