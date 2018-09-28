package physics2D.physics;

import physics2D.math.CFrame;
import physics2D.math.Vec2;

public abstract class Constraint {
	public final Physical part1, part2;
	public final CFrame attach1, attach2;
	
	public Constraint(Physical part1, Physical part2, CFrame attach1, CFrame attach2){
		this.part1 = part1;
		this.part2 = part2;
		this.attach1 = attach1;
		this.attach2 = attach2;
	}
	
	public abstract void enact();
	
	public CFrame getGlobalAttach1(){
		return part1.getCFrame().localToGlobal(attach1);
	}
	
	public CFrame getGlobalAttach2(){
		return part2.getCFrame().localToGlobal(attach2);
	}
	
	public Vec2 getGlobalAttachPos1(){
		return part1.getCFrame().localToGlobal(attach1.position);
	}
	
	public Vec2 getGlobalAttachPos2(){
		return part2.getCFrame().localToGlobal(attach2.position);
	}
}
