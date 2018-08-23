package physics;

import game.Constants;
import game.Debug;
import geom.Shape;

import java.util.ArrayList;

import util.Color;
import math.Vec2;


public class World {
	public Vec2 gravity = new Vec2(0, -2);
	public final double AIRFRICTIONFACTOR = 0.01;
	public final double AIRROTFRICTIONFACTOR = 0.001;
	
	private static final double MAGNET_MOVEMENT_CANCEL_FACTOR = Constants.VELOCITY_STOP_FACTOR;
	private static final double MAGNET_STRENGTH = 1000.0;
	
	public final ArrayList<Physical> physicals = new ArrayList<>();
	public final ArrayList<Constraint> constraints = new ArrayList<>();
	
	private Vec2 magnetTarget = null;
	private Physical magnetSubject = null;
	private Vec2 magnetAttachPoint = null;
	private final Object magnetLock = new Object();
	
	public World(Vec2 gravity){
		this.gravity = gravity;
	}
	
	public void addObject(Physical p){
		physicals.add(p);
	}
	
	public void addConstraint(Constraint c){
		constraints.add(c);
	}
	
	public void tick(double deltaT) {
		for(Physical p:physicals){
			p.update(deltaT);
		}
		
		for(Physical p:physicals){
			// gravity
			p.applyForce(gravity.mul(p.getMass()));
			
			// air friction
			// p.applyForce(p.velocity.mul(-AIRFRICTIONFACTOR));
			// p.applyTorque(-p.angularVelocity * AIRROTFRICTIONFACTOR);
		}
		
		for(int i = 0; i < physicals.size(); i++){
			for(int j = i+1; j < physicals.size(); j++){
				physicals.get(i).interactWith(physicals.get(j));
				Debug.logInteraction(null, null);
			}
		}
		
		executeConstraints();
	}
	
	private void executeConstraints(){
		for(Constraint c:constraints){
			c.enact();
		}
		synchronized (magnetLock) {
			if(magnetSubject != null){
				Vec2 attachPoint = magnetSubject.cframe.localToGlobal(magnetAttachPoint);
				Vec2 delta = magnetTarget.subtract(attachPoint);
				
				Debug.logPoint(attachPoint, Color.RED);
				Debug.logVector(attachPoint, delta, Color.GREEN);
				
				Vec2 relSpeedForce = magnetSubject.getSpeedOfPoint(attachPoint).mul(-MAGNET_MOVEMENT_CANCEL_FACTOR*magnetSubject.getMass());
				
				if(relSpeedForce.dot(delta) > 0){
					relSpeedForce.add(delta.mul(relSpeedForce.dot(delta)));
				}
				
				Vec2 deltaForce = delta.mul(magnetSubject.getMass() * MAGNET_STRENGTH);
				
				magnetSubject.applyForce(deltaForce, attachPoint);
				magnetSubject.applyForce(relSpeedForce, attachPoint);
			}
		}
	}
	
	/**
	 * Returns the first physical found containing the given coordinate
	 * If none found then <code>null</code>
	 * 
	 * @param worldPos
	 * @return
	 */
	public Physical getObjectAt(Vec2 worldPos){
		for(Physical p:physicals)
			for(Shape subShape:p.shapes)
				if(subShape.containsPoint(worldPos))
					return p;
		
		return null;
	}
	
	public void grabBlock(Vec2 mousePos){
		synchronized (magnetLock) {
			System.out.println("Block tried to grab at " + mousePos);
			for(Physical b:physicals){
				for(Shape subShape:b.shapes)
					if(subShape.containsPoint(mousePos)){
						System.out.println("Block " + b + " grabbed!");
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
