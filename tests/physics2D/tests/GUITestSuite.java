package physics2D.tests;

import org.junit.After;
import org.junit.Before;

import physics2D.Debug;

public abstract class GUITestSuite {
	@Before
	public void setupDebugScreen(){
		if(Boolean.getBoolean("debugEnabled"))
			Debug.setupDebugScreen();
	}
	
	@After
	public void destroyDebugScreen(){
		if(Boolean.getBoolean("debugEnabled")){
			Debug.endTick();
			if(Debug.getDrawCount() > 0)
				Debug.halt();
			else
				Debug.destroyDebugScreen();
		}
	}
}
