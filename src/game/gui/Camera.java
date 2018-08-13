package game.gui;

import math.CFrame;
import math.Vec2;

public class Camera{
	public CFrame cframe;
	public double zoomFactor;
	public boolean attachedToObject = false;
	
	public Camera(CFrame startCFrame, double startZoom){
		this.cframe = startCFrame;
		this.zoomFactor = startZoom;
	}
	
	public Vec2 toCameraSpace(Vec2 worldSpace){
		return cframe.globalToLocal(worldSpace).mul(zoomFactor);
	}
	
	public Vec2 fromCameraSpace(Vec2 camSpace){
		return cframe.localToGlobal(camSpace.mul(1/zoomFactor));
	}
	
	/**
	 * Zooms the camera around zoomCenter
	 * @param zoomAmount amount to zoom
	 * @param zoomCenter The point around which to zoom in Screen coordinates, this will stay stationary in world coordinates
	 */
	public void zoom(double zoomAmount, Vec2 zoomCenter){
		if(attachedToObject){
			zoomFactor *= zoomAmount;
		}else{
			Vec2 worldPosStart = fromCameraSpace(zoomCenter);
			zoomFactor *= zoomAmount;
			Vec2 zoomCenterMoved = worldPosStart.subtract(fromCameraSpace(zoomCenter));
			cframe.move(zoomCenterMoved);
		}
	}
	
	public void move(Vec2 delta){
		if(attachedToObject)
			cframe = cframe.copy();
		cframe.move(delta);
	}
	
	public void attachToObject(CFrame cframe){
		attachedToObject = true;
		this.cframe = cframe;
	}
}