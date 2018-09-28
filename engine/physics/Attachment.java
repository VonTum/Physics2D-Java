package physics;

import geom.Shape;
import math.CFrame;

public final class Attachment {
	public final Shape shape;
	public final CFrame relativeCFrame;
	
	public Attachment(Shape shape, CFrame relativeCFrame){
		this.shape = shape;
		this.relativeCFrame = relativeCFrame;
	}
}
