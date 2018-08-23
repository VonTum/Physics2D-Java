package game.gui;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import game.Debug;
import game.input.InputHandler;
import geom.Polygon;
import geom.Shape;

import java.io.IOException;
import java.util.ArrayList;

import math.BoundingBox;
import math.CFrame;
import math.Mat2;
import math.NormalizedVec2;
import math.Vec2;
import math.Vertex2;
import math.WorldVec2;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import com.sun.org.apache.xpath.internal.operations.Or;

import physics.Constraint;
import physics.DepthWithDirection;
import physics.Physical;
import physics.World;
import util.Color;
import util.Dimentions;
import static java.lang.Math.PI;


@SuppressWarnings("unused")
public class Screen {
	private static long window;
	
	public static Camera camera = new Camera(new CFrame(0, 0, 0), 1.0);
	
	private static ArrayList<Drawable> markingsBuf = new ArrayList<>();
	private static ArrayList<Drawable> markings = new ArrayList<>();
	
	public static final boolean DRAW_BLOCK_DIRECTION = false;
	public static final boolean DRAW_BLOCK_NORMALVECS = false;
	public static final boolean DRAW_LOCAL_AXES = true;
	public static final boolean DRAW_VERTEX_ORIENTATION = false;
	public static final boolean DRAW_BLOCK_SPEEDS = false;
	public static final boolean DRAW_CENTER_OF_MASS = true;
	
	static final int DEFAULT_SIZE = 920;

	static Font font;
	
	static int 	width = DEFAULT_SIZE;
	static int height = DEFAULT_SIZE;
	
	public static void init(InputHandler handler) throws IOException{
		if(!glfwInit()){
			System.out.println("GLFW Failed to initialize");
			System.exit(1);
		}
		
		GLFWErrorCallback.createPrint(System.err).set();
		
		// long monitor = GLFW.glfwGetMonitors().address(0); // get secondary monitor
		
		window = glfwCreateWindow(DEFAULT_SIZE, DEFAULT_SIZE, "Test Prog", 0, 0);
		
		// TODO debug, remove
		// GLFW.glfwSetWindowPos(window, -1000, 50);
		
		glfwShowWindow(window);
		
		glfwMakeContextCurrent(window);
		
		GL.createCapabilities();
		
		
		
		/*GL11.glLoadIdentity();
		GL11.glTranslatef(0.5f, 0, 0);
		GL11.glRotatef(30f, 0, 0, 1);*/
		
		
		// glEnable(GL_POLYGON_SMOOTH);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
		// glEnable(GL_MULTISAMPLE);
		
		font = new Font(Screen.class.getResource("/ascii.png"));
		
		glfwSetWindowSizeCallback(window, (win, newWidth, newHeight) -> {
			glViewport(0, 0, newWidth, newHeight);
			width = newWidth;
			height = newHeight;
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(-(1f*newWidth)/newHeight, (1f*newWidth)/newHeight, -1.0f, 1.0f, 0.0f, 1.0f);
		});
		
		GLFW.glfwSetMouseButtonCallback(window, (win, button, action, modifiers) -> {
			if(action == 1)
				handler.mouseDown(getMousePos(), button, modifiers);
			else
				handler.mouseUp(getMousePos(), button, modifiers);
		});
		
		GLFW.glfwSetCursorPosCallback(window, (win, x, y) -> {
			handler.mouseMove(new Vec2(x, y));
		});
		
		GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			switch(action){
			case GLFW.GLFW_REPEAT:
				handler.keyRepeat(key, scancode, mods);
				break;
			case GLFW.GLFW_PRESS:
				handler.keyDown(key, scancode, mods);
				break;
			case GLFW.GLFW_RELEASE:
				handler.keyUp(key, scancode, mods);
				break;
			}
		});
		
		GLFW.glfwSetScrollCallback(window, (win, xscroll, yscroll) -> {
			handler.scroll(xscroll, yscroll);
		});
		
		glClearColor(0.7f,  0.8f, 1.0f, 0.0f);
		glLineWidth(2.0f);
	}
	
	public static Dimentions getWindowSize(){
		int[] x = new int[1], y = new int[1];
		GLFW.glfwGetWindowSize(window, x, y);
		return new Dimentions(x[0], y[0]);
	}
	
	public static Vec2 getMousePos(){
		double[] x = new double[1], y = new double[1];
		GLFW.glfwGetCursorPos(window, x, y);
		return new Vec2(x[0], y[0]);
	}
	
	private static World w;
	public static void setWorld(World w){
		Screen.w = w;
	}
	
	public static void refresh(){
		glfwPollEvents();
		glClear(GL_COLOR_BUFFER_BIT);
		
		Vec2 mousePos = getMousePos();
		Vec2 worldMousePos = mouseToWorldCoords(mousePos);
		
		Physical selectedObject = w.getObjectAt(worldMousePos);
		
		for(Physical obj: w.physicals){
			for(Shape shape:obj.shapes){
				if(obj == selectedObject)
					drawShape(shape, shape.properties.color.darker(0.8));
				else
					drawShape(shape, shape.properties.color);
				
				if(DRAW_BLOCK_NORMALVECS) drawShapeNormalVecs(shape);
				
			}
			
			if(DRAW_BLOCK_DIRECTION){
				color(0.7, 0.3, 0.3, 1.0);
				drawVector(obj.cframe.position, obj.cframe.rotation.mul(Vec2.UNITX));
			}
			color(0.2, 0.2, 0.2, 1.0);
			for(Shape shape:obj.shapes)
				drawLine(obj.cframe.position, shape.getCFrame().position);
			
			if(DRAW_BLOCK_SPEEDS) drawBlockSpeeds(obj);
			if(DRAW_CENTER_OF_MASS) drawCenterOfMass(obj);
		}
		
		for(Constraint c: w.constraints){
			color(Color.GREY.alpha(0.7));
			drawLine(c.part1.getCenterOfMass(), c.getGlobalAttachPos1());
			drawLine(c.part2.getCenterOfMass(), c.getGlobalAttachPos2());
			color(Color.RED.alpha(0.7));
			drawVector(c.getGlobalAttachPos1(), c.getGlobalAttach1().rotation.getOrientation().mul(0.03));
			drawVector(c.getGlobalAttachPos2(), c.getGlobalAttach2().rotation.getOrientation().mul(0.03));
			color(Color.BLUE.alpha(0.7));
			drawPoint(c.getGlobalAttachPos1(), 0.01);
			color(Color.WHITE.alpha(0.7));
			drawPoint(c.getGlobalAttachPos2(), 0.005);
		}
		
		applyMarkings();
		
		
		/*for(Physical obj:w.physicals){
			Font.Text text = font.new Text("Hello", 36f, 1f);
			annotate(text, obj.getCenterOfMass());
		}*/
		
		color(Color.BLACK);
		if(selectedObject != null){
			font.drawString(selectedObject.describe(), 36f, 1f, 0.999f * (float) getLeftBorderX(), 0.999f);
		}
		
		Font.Text text = font.createText(Debug.getDebugInfo(), 36f, 1f);
		double textHeight = text.getTextDimentions().height;
		text.draw(0.999f * (float) getLeftBorderX(), -0.999f+(float)textHeight);
		
		Font.Text mouseCoordsText = font.createText(String.format("%.6f\n%.6f", worldMousePos.x, worldMousePos.y), 36f, 0.999f);
		Dimentions textDims = mouseCoordsText.getTextDimentions();
		mouseCoordsText.drawRightAligned(0.999f * (float) getRightBorderX(), -0.999f + (float) textDims.height);
		
		
		
		glfwSwapBuffers(window);
	}
	
	private static void drawShape(Shape shape, Color c){
		drawPolygon(shape.getDrawingVertexes(), c);
	}
	
	private static void drawPolygon(Vec2[] vertexes, Color c){
		color(c);
		glBegin(GL11.GL_POLYGON);
		for(Vec2 vertex:vertexes)
			vertex(vertex);
		glEnd();
		color(0.0, 0.0, 0.0, 1.0);
		glBegin(GL11.GL_LINE_LOOP);
		for(Vec2 vertex:vertexes)
			vertex(vertex);
		glEnd();
	}
	
	private static void annotate(Font.Text annotation, Vec2 annotationPosition){
		double padding = 0.005;
		color(Color.GREY.alpha(0.5));
		float fontSize = annotation.getActualFontSize();
		drawRectangle(annotationPosition.add(fontSize/2-padding, -fontSize/2-padding), annotation.getTextDimentions().expand(padding*2));
		
		color(Color.GREEN);
		
		annotation.draw(camera.toCameraSpace(annotationPosition).add(fontSize/2, fontSize/2));
	}
	
	private static void applyMarkings(){
		// mark markings
		synchronized (markings) {
			for(Drawable d:markings){
				d.drawAndColor();
			}
		}
	}
	
	private static void drawCenterOfMass(Physical physical){
		Vec2 centerOfMass = physical.getCenterOfMass();
		
		double pointRadius = 0.01/camera.zoomFactor;
		
		color(Color.BLACK);
		drawCirclePart(centerOfMass, Vec2.UNITX.mul(pointRadius), Math.PI/2, Math.PI/4+0.000000001);
		drawCirclePart(centerOfMass, Vec2.UNITNEGX.mul(pointRadius), Math.PI/2, Math.PI/4+0.000000001);
		color(Color.YELLOW);
		drawCirclePart(centerOfMass, Vec2.UNITY.mul(pointRadius), Math.PI/2, Math.PI/4+0.000000001);
		drawCirclePart(centerOfMass, Vec2.UNITNEGY.mul(pointRadius), Math.PI/2, Math.PI/4+0.000000001);
	}
	
	private static void drawShapeNormalVecs(Shape shape){
		color(0.3, 0.3, 0.3, 0.3);
		
		for(Vec2 origin:shape){
			DepthWithDirection d = shape.getNormalVecAndDepthToSurface(origin, Vec2.UNITY);
			
			drawVector(origin, d.getVecToSurface());
		}
		
		if(shape instanceof Polygon){
			color(0.7, 0.7, 0.3, 0.8);
			for(Vertex2 v:((Polygon) shape).getVertexes()){
				drawVector(v.position, v.normalVec);
			}
		}
	}
	
	private static void drawBlockSpeeds(Physical physical){
		for(Shape s:physical.shapes){
			color(0.3, 0.7, 0.3, 0.8);
			for(Vec2 origin:s){
				drawVector(origin, physical.getSpeedOfPoint(origin));
			}
		}
	}
	
	private static void drawLine(Vec2 start, Vec2 end){
		glBegin(GL11.GL_LINES);
		vertex(start);
		vertex(end);
		glEnd();
	}
	
	private static void drawRectangle(Vec2 start, Vec2 diagonal){
		glBegin(GL11.GL_QUADS);
		vertex(start);
		vertex(start.add(diagonal.x, 0));
		vertex(start.add(diagonal));
		vertex(start.add(0, diagonal.y));
		glEnd();
	}
	
	private static void drawRectangle(Vec2 bottomLeft, Dimentions size){
		glBegin(GL11.GL_QUADS);
		vertex(bottomLeft);
		vertex(bottomLeft.add(size.width, 0));
		vertex(bottomLeft.add(size.width, size.height));
		vertex(bottomLeft.add(0, size.height));
		glEnd();
	}
	
	private static void drawVector(Vec2 origin, Vec2 vector){
		
		vector = vector.maxLength(0.3);
		
		Vec2 end = origin.add(vector);
		glBegin(GL11.GL_LINES);
		vertex(origin);
		vertex(end);
		
		Mat2 rotMat = Mat2.rotTransform(vector.getTheta());
		Vec2 leftPart = new Vec2(-0.2, 0.1).mul(vector.length()).maxLength(.02);
		Vec2 rightPart = new Vec2(-0.2, -0.1).mul(vector.length()).maxLength(.02);
		
		Vec2 leftVec = end.add(rotMat.mul(leftPart));
		Vec2 rightVec = end.add(rotMat.mul(rightPart));
		
		vertex(end);
		vertex(leftVec);
		
		vertex(end);
		vertex(rightVec);
		
		glEnd();
	}
	
	private static void drawTriangle(Vec2 v1, Vec2 v2, Vec2 v3){
		glBegin(GL11.GL_LINE_LOOP);
		vertex(v1);
		vertex(v2);
		vertex(v3);
		glEnd();
	}
	
	private static void drawCirclePart(Vec2 origin, Vec2 relativeStart, double angle, double maxAngleInterval){
		if(angle > 8*PI)
			angle = 8*PI;
		if(angle < -8*PI)
			angle = -8*PI;
		
		int triangleCount = (int) Math.ceil(Math.abs(angle / maxAngleInterval));
		
		double actualAngle = angle / triangleCount;
		
		Mat2 rotMat = Mat2.rotTransform(actualAngle);
		
		glBegin(GL_TRIANGLE_FAN);
		vertex(origin);
		
		Vec2 curPoint = relativeStart;
		
		for(int i = 0; i < triangleCount; i++){
			vertex(origin.add(curPoint));
			curPoint = rotMat.mul(curPoint);
		}
		vertex(origin.add(curPoint));
		
		
		
		glEnd();
	}
	
	private static void drawCircle(Vec2 origin, double radius, double maxAngleInterval){
		drawCirclePart(origin, new Vec2(radius, 0), 2*PI, maxAngleInterval);
	}
	
	private static void drawPoint(Vec2 origin){
		drawPoint(origin, 0.01/camera.zoomFactor);
	}
	
	private static void drawPoint(Vec2 origin, double radius){
		drawCircle(origin, radius, Math.PI/4+0.000001);
	}
	
	private static void vertex(Vec2 vec){
		Vec2 camVec = camera.toCameraSpace(vec);
		glVertex2d(camVec.x, camVec.y);
	}
	
	private static void vertex(double x, double y){
		vertex(new Vec2(x, y));
	}
	
	static void color(Color c){
		color(c.r, c.g, c.b, c.a);
	}
	
	private static void color(double r, double g, double b, double a){
		glColor4d(r, g, b, a);
	}
	
	public static void markPoint(Vec2 point, Color color){
		markingsBuf.add(new MarkedPoint(point, color));
	}
	
	public static void markVector(Vec2 origin, Vec2 vector, Color color){
		markingsBuf.add(new MarkedVector(origin, vector, color));
	}
	
	public static void markPolygon(Vec2[] polygon, Color color){
		markingsBuf.add(new MarkedPolygon(polygon, color));
	}
	
	/**
	 * Commits all drawings to the next frame
	 * 
	 * draw buffer is cleared
	 */
	public static void commitDrawings(){
		synchronized (markings) {
			markings = markingsBuf;
			markingsBuf = new ArrayList<>();
		}
	}
	
	public static Vec2 mouseToScreenCoords(Vec2 mouseCoords){
		
		Dimentions screenSize = getWindowSize();
		
		Vec2 translatedCoords = mouseCoords.subtract(new Vec2(screenSize.width/2, screenSize.height/2));
		
		Vec2 screenCoords = translatedCoords.div(screenSize.height/2).mulXY(1, -1); // invert y axis
		
		return screenCoords;
		//return new Vec2(mouseCoords.x * 2.0 / screenSize.width - 1.0, 1.0 - mouseCoords.y * 2.0 / screenSize.height);
	}
	
	public static Vec2 mouseToWorldCoords(Vec2 mouseCoords){
		return camera.fromCameraSpace(mouseToScreenCoords(mouseCoords));
	}
	
	public static double getTopBorderY(){return 1.0;}
	public static double getBottomBorderY(){return -1.0;}
	public static double getLeftBorderX(){return -(double) width/height;}
	public static double getRightBorderX(){return (double) width/height;}
	
	public static boolean shouldClose(){return glfwWindowShouldClose(window);}
	public static void terminate(){glfwTerminate();}
	
	private static final class MarkedPoint extends Drawable {
		public final Vec2 point;
		
		public MarkedPoint(Vec2 point, Color color){
			super(color);
			this.point = point;
		}
		
		@Override
		public void draw(){
			drawPoint(point);
		}
	}
	
	private static final class MarkedVector extends Drawable {
		public final Vec2 point;
		public final Vec2 vector;
		
		public MarkedVector(Vec2 point, Vec2 vector, Color color){
			super(color);
			this.point = point;
			this.vector = vector;
		}
		
		@Override
		public void draw(){
			drawVector(point, vector);
		}
	}
	
	private static final class MarkedPolygon extends Drawable {
		public final Vec2[] polygon;
		
		public MarkedPolygon(Vec2[] polygon, Color fillColor) {
			super(fillColor);
			this.polygon = polygon;
		}
		
		@Override
		public void draw(){
			drawPolygon(polygon, color);
		}
	}
}
