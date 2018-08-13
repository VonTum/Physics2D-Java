package game;

import physics.Locatable;
import util.Color;
import game.gui.Screen;
import math.Vec2;

public class Debug {
	
	private static final boolean MARK_FORCES = false;
	private static final boolean MARK_POINTS = false;
	private static final boolean MARK_VECTORS = true;
	
	private static final Color DEFAULT_COLOR = new Color(1.0, 0.0, 0.0, 1.0);
	private static final Color DEFAULT_FORCE_COLOR = new Color(0.0, 0.0, 1.0, 1.0);
	
	public static void logForce(Locatable subject, Vec2 relativeAttachment, Vec2 force){
		logForce(subject, relativeAttachment, force, DEFAULT_FORCE_COLOR);
	}
	public static void logForce(Locatable subject, Vec2 relativeAttachment, Vec2 force, Color color){
		if(MARK_FORCES) Screen.markVector(subject.getCFrame().localToGlobal(relativeAttachment), force, color);
	}
	public static void logPoint(Vec2 point){
		logPoint(point, DEFAULT_COLOR);
	}
	public static void logPoint(Vec2 point, Color color){
		if(MARK_POINTS) Screen.markPoint(point, color);
	}
	public static void logVector(Vec2 origin, Vec2 vector){
		logVector(origin, vector, DEFAULT_COLOR);
	}
	public static void logVector(Vec2 origin, Vec2 vector, Color color){
		if(MARK_VECTORS) Screen.markVector(origin, vector, color);
	}
	public static void commit(){
		Screen.commitDrawings();
	}
}
