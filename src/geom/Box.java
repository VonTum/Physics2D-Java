package geom;

import physics.Physical;
import physics.PhysicalProperties;
import math.CFrame;
import math.RotMat2;
import math.Vec2;

public class Box extends Physical {
	
	public Box(Vec2 position, RotMat2 rotation, double width, double height, PhysicalProperties properties) {
		this(new CFrame(position, rotation), width, height, properties);
	}
	
	public Box(CFrame cframe, double width, double height, PhysicalProperties properties){
		super(new Rectangle(properties, cframe, width, height));
	}
	
}
