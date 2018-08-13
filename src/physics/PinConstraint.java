package physics;

import game.Debug;
import util.Color;
import math.CFrame;
import math.Vec2;

/**
 * A PinConstraint keeps the two attachments together, but allows free rotation around the axle of the constraint
 */
public class PinConstraint extends Constraint {
	
	private final double pinForce;
	
	public PinConstraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2, double pinForce) {
		super(part1, part2, attach1, attach2);
		this.pinForce = pinForce;
	}
	
	@Override
	public void enact() {
		Vec2 firstPos = getGlobalAttachPos1();
		Vec2 secondPos = getGlobalAttachPos2();
		
		Vec2 avgPos = firstPos.mul(part2.getMass()).add(secondPos.mul(part1.getMass())).div(part1.getMass()+part2.getMass());
		
		Vec2 delta1 = avgPos.subtract(firstPos);
		Vec2 delta2 = avgPos.subtract(secondPos);
		
		// part1.move(delta1);
		// part2.move(delta2);
		
		// <COPY>
		
		Vec2 attachPoint = getGlobalAttachPos1();
		Vec2 otherAttach = getGlobalAttachPos2();
		Vec2 delta = otherAttach.subtract(attachPoint); // vector from attach1 to attach2
		
		Debug.logPoint(attachPoint, Color.RED);
		Debug.logVector(attachPoint, delta, Color.GREEN);
		
		// speed of part2 relative to part1
		Vec2 relSpeed = part2.getSpeedOfPoint(otherAttach).subtract(part1.getSpeedOfPoint(attachPoint)); 
		
		double escapeSpeedFactor = relSpeed.dot(delta);
		
		Vec2 slowDownForce = Vec2.ZERO;
		if(escapeSpeedFactor > 0){
			slowDownForce = delta.mul(escapeSpeedFactor/delta.lengthSquared());
			Debug.logVector(attachPoint, slowDownForce, Color.MAROON);
		}
		
		part1.actionReaction(part2, avgPos, slowDownForce);
		
		/*if(relSpeedForce.dot(delta) > 0){
			relSpeedForce.add(delta.mul(relSpeedForce.dot(delta)));
		}*/
		
		double minMass = Math.min(part1.getMass(), part2.getMass());
		
		Vec2 deltaForce = delta.mul(minMass * pinForce);
		
		
		part1.actionReaction(part2, avgPos, deltaForce);
		
		/*magnetSubject.applyForce(deltaForce, attachPoint);
		magnetSubject.applyForce(relSpeedForce, attachPoint);//*/
		
		// </COPY>
		
		// Vec2 pointForceOf1 = part1.getConcentratedForceInPoint(avgPos);
		// Vec2 pointForceOf2 = part2.getConcentratedForceInPoint(avgPos);
		
		// part1.actionReaction(part2, avgPos, force);
	}
}
