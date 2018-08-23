package game;

import java.io.IOException;

import physics.Locatable;
import physics.Physical;
import physics.World;
import util.Color;
import game.gui.Screen;
import game.input.StandardInputHandler;
import math.Vec2;

public class Debug {
	
	private static final boolean MARK_FORCES = false;
	private static final boolean MARK_POINTS = false;
	private static final boolean MARK_VECTORS = false;
	
	private static final Color DEFAULT_COLOR = new Color(1.0, 0.0, 0.0, 1.0);
	private static final Color DEFAULT_FORCE_COLOR = new Color(0.0, 0.0, 1.0, 1.0);
	
	private static int INTERACTION_COUNT = 0;
	private static long BEGIN_NANOS = System.nanoTime();
	private static String DEBUG_INFO = "";
	
	private static CircularQueue lastNanos = new CircularQueue(500);
	private static CircularQueue lastTPS = new CircularQueue(40);
	
	public static int age = 0;
	private static long startTime = System.nanoTime();
	
	private static int curTPS = 0;
	private static long lastTPSStore = System.nanoTime();
	
	private static World world = null;
	
	public static void setWorld(World w){
		world = w;
	}
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
	public static void logInteraction(Physical first, Physical second){
		INTERACTION_COUNT++;
	}
	public static synchronized void startTick(){
		age++;
		BEGIN_NANOS = System.nanoTime();
	}
	public static synchronized void endTick(){
		long curTime = System.nanoTime();
		long TICK_TIME_NANOS = curTime - BEGIN_NANOS;
		
		
		curTPS++;
		
		if(curTime > lastTPSStore + 100000000){ // 10^8 = 0.1sec
			lastTPS.put(curTPS);
			curTPS = 0;
			lastTPSStore = curTime;
		}
		
		lastNanos.put(TICK_TIME_NANOS);
		
		double avgTick = lastNanos.sum() / Math.min(500.0, age);
		
		double TPS = lastTPS.sum() / Math.min(4.0, Math.floor((curTime-startTime)/1E9));
		
		DEBUG_INFO = 	"Tick: "+String.format("%.6fms", avgTick*0.000001) + 
						"\nTPS: " + String.format("%.2f", TPS) + 
						"\nTPSTarget: " + String.format("%.2f", Physics2D.SIMULATION_SPEED*Physics2D.SIMULATION_REPEATS_PER_TICK*100) + 
						"\nInteractions: " + INTERACTION_COUNT + 
						"\nObjects: " + world.physicals.size() + 
						"\nAge: " + age + " ticks";
		
		INTERACTION_COUNT = 0;
		
		Screen.commitDrawings();
	}
	
	public static synchronized String getDebugInfo(){
		return DEBUG_INFO;
	}
	
	/**
	 * Sets up the debug screen, giving it an empty world.
	 */
	public static void setupDebugScreen(){
		World emptyWorld = new World(Vec2.ZERO);
		try{
			Screen.init(new StandardInputHandler(emptyWorld));
			Screen.setWorld(emptyWorld);
		}catch(IOException ex){
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * halt further code to inspect screen
	 * 
	 * Should be called after setupDebugScreen
	 */
	public static void halt() {
		while(!Screen.shouldClose())
			Screen.refresh();
	}
	
	private static final class CircularQueue {
		
		private int curPos = 0;
		private final long[] queue;
		public CircularQueue(int size){
			queue = new long[size];
		}
		
		public void put(long data){
			queue[curPos] = data;
			curPos = (curPos+1)%queue.length;
		}
		
		public long sum(){
			long sum = 0;
			for(long d:queue)
				sum += d;
			
			return sum;
		}
	}
}
