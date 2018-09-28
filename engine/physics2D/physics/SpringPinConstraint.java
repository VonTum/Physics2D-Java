package physics2D.physics;

import physics2D.math.CFrame;
import physics2D.math.Vec2;

/**
 * A PinConstraint keeps the two attachments together, but allows free rotation around the axle of the constraint
 */
public class SpringPinConstraint extends Constraint {
	
	private final double pinForce;
	
	public SpringPinConstraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2, double pinForce) {
		super(part1, part2, attach1, attach2);
		this.pinForce = pinForce;
	}
	
	@Override
	public void enact() {
		Vec2 firstPos = getGlobalAttachPos1();
		Vec2 secondPos = getGlobalAttachPos2();
		
		Vec2 delta = secondPos.subtract(firstPos);
		
		part1.actionReaction(part2, firstPos, delta.mul(pinForce));
	}
}
