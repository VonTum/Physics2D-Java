package physics2D.physics;

import physics2D.math.CFrame;

public class SlideConstraint extends Constraint {

	

	public SlideConstraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2) {
		super(part1, part2, attach1, attach2);
	}
	
	@Override
	public void enact() {
		
		double combinedInertia = (1/part1.inertia + 1/part2.inertia);
		
		
		double deltaWI = part2.getAngularImpulse() - part1.getAngularImpulse();
		
		System.out.println(deltaWI+ ":"+ combinedInertia);
		
		part1.applyTorqueImpulse(deltaWI / combinedInertia / 2);
		part2.applyTorqueImpulse(-deltaWI / combinedInertia / 2);
		
		double deltaA = part2.getRotAccelertation() - part1.getRotAccelertation();
		
		System.out.println(deltaA + ":"+ combinedInertia);
		
		part1.applyTorque(deltaA / combinedInertia);
		part2.applyTorque(-deltaA / combinedInertia);
	}

}
