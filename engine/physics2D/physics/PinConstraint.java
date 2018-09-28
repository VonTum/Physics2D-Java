package physics2D.physics;

import game.util.Color;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.Debug;

public class PinConstraint extends Constraint {
	
	private static final double PIN_DELTA_STRENGTH = 3000;
	private static final double PIN_MOVEMENT_STRENGTH = 30;
	
	
	public PinConstraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2) {
		super(part1, part2, attach1, attach2);
	}
	
	@Override
	public void enact() {
		Vec2 target = part2.cframe.localToGlobal(attach2.position);
		Vec2 attachPoint = part1.cframe.localToGlobal(attach1.position);
		Vec2 delta = target.subtract(attachPoint);
		
		Debug.logPoint(attachPoint, Color.RED);
		Debug.logVector(attachPoint, delta, Color.GREEN);
		
		Vec2 deltaSpeed = part1.getSpeedOfPoint(attachPoint).subtract(part2.getSpeedOfPoint(attachPoint));
		
		Debug.logVector(attachPoint, deltaSpeed);
		
		Vec2 relSpeedForce = deltaSpeed.mul(-PIN_MOVEMENT_STRENGTH*part1.mass);
		
		if(relSpeedForce.dot(delta) > 0){
			relSpeedForce.add(delta.mul(relSpeedForce.dot(delta)));
		}
		
		Vec2 deltaForce = delta.mul(part1.getMass() * PIN_DELTA_STRENGTH);
		
		part1.actionReaction(part2, attachPoint, deltaForce);
		part1.actionReaction(part2, attachPoint, relSpeedForce);
	}
}
