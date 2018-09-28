package physics;

import math.CFrame;
import math.RotMat2;

public class CoilPinConstraint extends PinConstraint {
	
	public double strength;
	
	public CoilPinConstraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2, double strength) {
		super(part1, part2, attach1, attach2);
		
		this.strength = strength;
	}
	
	@Override
	public void enact(){
		RotMat2 rot1 = part1.cframe.localToGlobal(attach1).rotation;
		RotMat2 rot2 = part2.cframe.localToGlobal(attach2).rotation;
		RotMat2 deltaRot = rot2.mul(rot1.inv());
		
		double angle = deltaRot.getAngle();
		
		double torque = angle * strength;
		
		part1.actionRotaction(part2, torque);
		
		super.enact();
	}
}
