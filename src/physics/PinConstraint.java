package physics;

import game.Constants;
import game.Debug;
import util.Color;
import math.CFrame;
import math.Vec2;
import math.Mat2;

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
	
	// @Override
	public void enactOld2() {
		Vec2 rA = attach1.position;
		Vec2 rB = attach2.position;
		
		Vec2 point1 = part1.cframe.localToGlobal(rA);
		Vec2 point2 = part2.cframe.localToGlobal(rB);
		Vec2 avgPoint = point1.mul(part1.mass).add(point2.mul(part2.mass)).div(part1.mass+part2.mass);
		
		Mat2 accelFactor = Mat2.IDENTITY.mul(1/part1.mass+1/part2.mass);
		Mat2 rotAFactor = new Mat2(-rA.y*rA.y, rA.x*rA.y, rA.x*rA.y, -rA.x*rA.x).mul(-1/part1.inertia);
		Mat2 rotBFactor = new Mat2(-rB.y*rB.y, rB.x*rB.y, rB.x*rB.y, -rB.x*rB.x).mul(-1/part2.inertia);
		
		Mat2 forceToAccelMatrix = accelFactor.add(rotAFactor).add(rotBFactor);
		
		Mat2 accelToForceMatrix = forceToAccelMatrix.inv();
		
		Vec2 dap = part2.getAccelerationOfPoint(point2).subtract(part1.getAccelerationOfPoint(point1));
		
		Vec2 force = accelToForceMatrix.mul(dap);
		
		System.out.println(force);
		
		part1.applyForce(force, point1);
		part2.applyForce(force.neg(), point2);
		
		
		
		game.Debug.logPoint(avgPoint, util.Color.RED);
		
		if(game.Debug.age >= 500)
			System.out.println();
		
		/*Vec2 deltaV = part2.getSpeedOfPoint(point2).subtract(part1.getSpeedOfPoint(point1));
		double inertiaA = part1.getPointInertia(point1, deltaV);
		double inertiaB = part2.getPointInertia(point2, deltaV);
		double minInertia = Math.min(inertiaA, inertiaB);
		Vec2 impulse = deltaV.mul(minInertia);
		
		part1.applyImpulse(impulse, avgPoint);
		part2.applyImpulse(impulse.neg(), avgPoint);*/
		
		// part1.move(avgPoint.subtract(part1.cframe.position));
		// part2.move(avgPoint.subtract(part2.cframe.position));
		
		/*Vec2 delta = point2.subtract(point1);
		Vec2 deltaForce = delta.mul(20000/(1/part1.mass+1/part2.mass));
		
		part1.applyForce(deltaForce, point1);
		part2.applyForce(deltaForce.neg(), point2);*/
		
		
		Vec2 accOfPoint1 = part1.getAccelerationOfPoint(point1);
		Vec2 accOfPoint2 = part2.getAccelerationOfPoint(point2);
		
		
		System.out.println(accOfPoint1 + " <=> " + accOfPoint2);
	}
	
	public void enactOld() {
		double invM = 1/part1.mass + 1/part2.mass;
		Vec2 rAG = part1.cframe.localToGlobal(attach1).position;
		Vec2 rBG = part2.cframe.localToGlobal(attach2).position;
		
		Vec2 target = rAG.mul(part1.mass).add(rBG.mul(part2.mass)).div(part1.mass+part2.mass);
		
		Vec2 rA = rAG.subtract(part1.getCenterOfMass());
		Vec2 rB = rBG.subtract(part2.getCenterOfMass());
		
		Vec2 da = part2.getAccelerationOfPoint(rBG).subtract(part1.getAccelerationOfPoint(rAG));
		
		double Ia = part1.inertia;
		double Ib = part2.inertia;
		
		double AxB = rA.cross(rB);
		
		double tmp = 	invM*invM
						- invM*(rA.lengthSquared()/Ia+rB.lengthSquared()/Ib)
						+ AxB*AxB;
		
		double left = rA.x*rA.y/Ia+rB.x*rB.y/Ib;
		double rightX = invM - rA.x*rA.x/Ia-rB.x*rB.x/Ib;
		double rightY = invM - rA.y*rA.y/Ia-rB.y*rB.y/Ib;
		
		double Fx = (-da.y*left+da.x*rightY)/tmp;
		double Fy = (-da.x*left+da.y*rightX)/tmp;
		
		part2.actionReaction(part1, target, new Vec2(Fx, Fy));
		
		// TODO FIX, DIS BROKEN
	}

}
