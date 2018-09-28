package game;

import geom.Rectangle;
import geom.Shape;
import math.CFrame;
import math.Mat2;
import math.RotMat2;
import math.Vec2;
import physics.Physical;
import physics.PhysicalProperties;
import util.Color;

public class ObjectLibrary {
	
	public static final PhysicalProperties BASIC = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
	
	public static Physical createFloor(CFrame cframe, PhysicalProperties properties){
		Shape floorShape = new Rectangle(20.0, 0.2);
		Physical floor = new Physical(cframe);
		floor.addPart(floorShape, CFrame.IDENTITY, properties);
		floor.anchor();
		return floor;
	}
	
	public static Physical createHammer(CFrame cframe, PhysicalProperties headProperties, PhysicalProperties armProperties){
		Shape heavyCube = new Rectangle(0.1, 0.1);
		Shape veryLightArm = new Rectangle(0.2, 0.03);
		Physical hammer = new Physical(cframe);
		hammer.addPart(heavyCube, new CFrame(0.0, 0.0, Math.PI/4), headProperties);
		hammer.addPart(veryLightArm, new CFrame(-0.3, 0.0, 0.0), armProperties);
		return hammer;
	}
	
	public static Physical createHammer(CFrame cframe){
		return createHammer(cframe, new PhysicalProperties(1000.0, 0.05, 0.0, Color.DARK_GREY.alpha(0.6)), new PhysicalProperties(10.0));
	}
	
	public static Physical createBowl(CFrame cframe, PhysicalProperties properties){
		RotMat2 rotMat = new RotMat2(0.7);
		Shape bowlLeft = new Rectangle(0.2, 0.05);
		Shape bowlBottom = new Rectangle(0.2, 0.05);
		Shape bowlRight = new Rectangle(0.2, 0.05);
		
		Physical bowl = new Physical(cframe);
		
		bowl.addPart(bowlLeft, new CFrame(rotMat.inv().mul(new Vec2(0, -0.4)), -0.7), properties);
		bowl.addPart(bowlBottom, new CFrame(new Vec2(0, -0.4), 0.0), properties);
		bowl.addPart(bowlRight, new CFrame(rotMat.mul(new Vec2(0, -0.4)), 0.7), properties);
		
		// bowl.cframe = cframe.localToGlobal(bowl.cframe);
		return bowl;
	}
}
