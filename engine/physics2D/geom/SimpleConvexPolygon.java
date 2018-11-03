package physics2D.geom;

import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class SimpleConvexPolygon implements ConvexPolygon {
	public final Vec2[] corners;
	public SimpleConvexPolygon(Vec2[] polygon) {
		this.corners = polygon;
	}
	@Override
	public Vec2[] getCorners() {
		return corners;
	}
	
	@Override
	public SimpleConvexPolygon leftSlice(Vec2 origin, Vec2 direction){
		return SimpleConvexPolygon.leftSlice(getCorners(), origin, direction);
	}
	
	public static SimpleConvexPolygon leftSlice(Vec2[] corners, Vec2 origin, Vec2 direction){
		return new SimpleConvexPolygon(ConvexPolygon.leftSlice(corners, origin, direction));
	}
	
	@Override
	public SimpleConvexPolygon scale(double factor){
		return new SimpleConvexPolygon(Polygon.scaled(getCorners(), factor));
	}
	
	@Override
	public SimpleConvexPolygon transformToCFrame(CFrame frame){
		return new SimpleConvexPolygon(Polygon.transformToCFrame(getCorners(), frame));
	}
	
	@Override
	public SimpleConvexPolygon translate(Vec2 offset){
		return new SimpleConvexPolygon(Polygon.translate(getCorners(), offset));
	}
	
	@Override
	public SimpleConvexPolygon rotate(RotMat2 rotation){
		return new SimpleConvexPolygon(Polygon.rotate(getCorners(), rotation));
	}
	
	@Override
	public SimpleConvexPolygon rotate(double angle){
		return rotate(RotMat2.rotTransform(angle));
	}
}
