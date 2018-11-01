package physics2D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import physics2D.geom.Shape;
import physics2D.math.Vec2;
import physics2D.math.WorldVec2;
import physics2D.physics.Physical;
import physics2D.physics.World;
import game.Physics2D;
import game.gui.Screen;
import game.input.DebugInputHandler;
import game.util.Color;

public class Debug {
	
	private static final boolean PAUSE_ENABLED = "true".equalsIgnoreCase(System.getProperty("pauseEnabled"));
	
	public static boolean MARK_FORCES = false;
	public static boolean MARK_POINTS = false;
	public static boolean MARK_VECTORS = false;
	public static boolean MARK_SHAPES = false;
	
	private static final HashSet<Class<?>> loggedClasses = new HashSet<>();
	private static final HashSet<String> loggedMethods = new HashSet<>();
	
	private static final Color DEFAULT_COLOR = new Color(1.0, 0.0, 0.0, 1.0);
	private static final Color DEFAULT_FORCE_COLOR = new Color(0.0, 0.0, 1.0, 1.0);
	private static final Color DEFAULT_SHAPE_COLOR = new Color(0.0, 0.6, 0.0, 0.6);
	
	private static int INTERACTION_COUNT = 0;
	private static long BEGIN_NANOS = System.nanoTime();
	private static String DEBUG_INFO = "";
	
	private static CircularLongQueue lastNanos = new CircularLongQueue(500);
	private static CircularLongQueue lastTPS = new CircularLongQueue(40);
	
	public static int age = 0;
	private static long startTime = System.nanoTime();
	
	private static int curTPS = 0;
	private static long lastTPSStore = System.nanoTime();
	
	private static World world = null;
	
	public static boolean stop = false;
	
	private static final CommitableBuffer<LoggedObject> objectLog = new CommitableBuffer<>();
	
	private static boolean shouldLog(){
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for(StackTraceElement e:stackTrace){
			try {
				if(e.getClassName().contains("tests") || 
					loggedClasses.contains(Class.forName(e.getClassName())) || 
					loggedMethods.contains(e.getMethodName())
				) return true;
			} catch (ClassNotFoundException e1) {assert false;/*will never happen*/}
		}
		return false;
	}
	
	public static void logForce(Physical subject, Vec2 relativeAttachment, Vec2 force){
		logForce(subject, relativeAttachment, force, DEFAULT_FORCE_COLOR);
	}
	public static void logForce(Physical subject, Vec2 relativeAttachment, Vec2 force, Color color) {
		/*if(age >= 4900 && force.lengthSquared()/subject.mass >= 205.0){
			System.out.println("Large force: " + force + " causing accel: " + force.div(subject.mass));
			Screen.markVector(subject.getCFrame().localToGlobal(relativeAttachment), force, Color.RED);
			Screen.markPoint(subject.getCFrame().localToGlobal(relativeAttachment), Color.YELLOW);
			subject.parts.get(0).properties = subject.parts.get(0).properties.withColor(Color.YELLOW);
			stop = true;
		}*/
		if(MARK_FORCES) Screen.markVector(subject.getCFrame().localToGlobal(relativeAttachment), force, color);
	}
	public static void logPoint(Vec2 point){
		logPoint(point, DEFAULT_COLOR);
	}
	public static void logPoint(Vec2 point, Color color){
		if(MARK_POINTS) Screen.markPoint(point, color);
	}
	public static void logVector(WorldVec2 vec){
		logVector(vec.origin, vec.vector);
	}
	public static void logVector(WorldVec2 vec, Color c){
		logVector(vec.origin, vec.vector, c);
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
	public static void logPolygon(Vec2... polygon){
		logPolygon(DEFAULT_SHAPE_COLOR, polygon);
	}
	public static void logPolygon(Color color, Vec2... polygon){
		if(MARK_SHAPES) Screen.markPolygon(polygon, color);
	}
	public static void logPolygon(Color fillColor, Color edgeColor, Vec2... polygon){
		if(MARK_SHAPES) Screen.markPolygon(polygon, fillColor, edgeColor);
	}
	public static void logShape(Shape s){
		logShape(s, DEFAULT_SHAPE_COLOR);
	}
	public static void logShape(Shape s, Color color){
		logShape(s, color, Color.BLACK);
	}
	public static void logShape(Shape s, Color fillColor, Color edgeColor){
		Screen.markShape(s, fillColor, edgeColor);
	}
	public static int getDrawCount(){
		return Screen.getMarkingsCount();
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
		
		objectLog.commit();
		Screen.commitDrawings();
		
		
	}
	
	public static synchronized String getDebugInfo(){
		return DEBUG_INFO;
	}
	
	public static synchronized List<LoggedObject> getObjectLog(){
		return objectLog.getCommittedData();
	}
	
	/**
	 * Sets up the debug screen, giving it an empty world.
	 */
	public static void setupDebugScreen(){
		World emptyWorld = new World(Vec2.ZERO);
		DebugInputHandler debugInputHandler = new DebugInputHandler(emptyWorld);
		try{
			Screen.init(debugInputHandler);
			Screen.setWorld(emptyWorld);
			world = emptyWorld;
		}catch(IOException ex){
			throw new RuntimeException(ex);
		}
	}
	
	public static Vec2 getMouseWorldPos(){
		return Screen.mouseToWorldCoords(Screen.getMousePos());
	}
	
	private static boolean PAUSED = false;
	/**
	 * Pauses the current execution and adds drawings
	 */
	public static void pause(){
		Screen.addDrawings();
		p();
	}
	/**
	 * Pauses the current execution and adds drawings
	 * Prints <code>message</code>
	 */
	public static void pause(String message){
		System.out.println(message);
		pause();
	}
	/**
	 * Pauses the current execution and commits drawings
	 */
	public static void pauseAndCommit(){
		Screen.commitDrawings();
		p();
	}
	private static void p(){
		if(!PAUSE_ENABLED) return;
		PAUSED = true;
		while(!Screen.shouldClose() && PAUSED){
			Screen.refresh();
		}
	}
	public static void unpause() {
		PAUSED = false;
	}
	
	/**
	 * halt further code to inspect screen
	 * 
	 * Should be called after setupDebugScreen
	 */
	public static void halt() {
		while(!Screen.shouldClose())
			Screen.refresh();
		destroyDebugScreen();
	}
	
	/**
	 * Closes the current debug screen
	 */
	public static void destroyDebugScreen(){
		Screen.close();
	}
	
	/**
	 * Halt further code to inspect screen
	 * Will update screen every frame
	 * 
	 * @param r Runnable to run every refresh
	 */
	public static void haltWithTickAction(Runnable r){
		if(PAUSE_ENABLED)
		while(!Screen.shouldClose()){
			r.run();
			Debug.endTick();
			Screen.refresh();
		}
	}
	
	public static void setWorld(World w){world = w;}
	
	private static class CircularLongQueue {
		
		private int curPos = 0;
		private final long[] queue;
		public CircularLongQueue(int size){
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
	
	public static class CommitableBuffer<T> implements Iterable<T>{
		private ArrayList<T> bufList = new ArrayList<T>();
		private ArrayList<T> finalList = new ArrayList<T>();
		
		public synchronized void add(T elem){bufList.add(elem);}
		public synchronized void commit(){
			ArrayList<T> tmp = finalList;
			finalList = bufList;
			tmp.clear();
			bufList = tmp;
		}
		
		@Override
		public synchronized Iterator<T> iterator() {
			return getCommittedData().iterator();
		}
		
		
		
		public synchronized List<T> getCommittedData() {
			return new ArrayList<T>(finalList);
		}
	}
	
	private static class LoggedObject {
		public final Physical subject;
		public final String type, group;
		public final Vec2 gPosition;
		public final Object data;
		
		public LoggedObject(Physical subject, String type, String group, Vec2 gPosition, Object data){
			this.subject = subject;
			this.type = type;
			this.group = group;
			this.gPosition = gPosition;
			this.data = data;
		}
	}

}
