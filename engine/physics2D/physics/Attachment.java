package physics2D.physics;

import physics2D.geom.Shape;
import physics2D.math.CFrame;

public final class Attachment {
	public final Shape shape;
	public final CFrame relativeCFrame;
	
	public Attachment(Shape shape, CFrame relativeCFrame){
		this.shape = shape;
		this.relativeCFrame = relativeCFrame;
	}
}
