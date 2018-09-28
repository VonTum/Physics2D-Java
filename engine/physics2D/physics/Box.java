package physics2D.physics;

import physics2D.geom.Rectangle;
import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class Box extends Physical {
	
	public Box(Vec2 position, RotMat2 rotation, double width, double height, PhysicalProperties properties) {
		this(new CFrame(position, rotation), width, height, properties);
	}
	
	public Box(CFrame cframe, double width, double height, PhysicalProperties properties){
		super(cframe);
		addPart(new Rectangle(width, height), CFrame.IDENTITY, properties);
	}
}
