package prog04;
import java.util.ArrayList;

//
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/*--------------------------------------------------------------------------+
|	This version puts together all the things we have seen so far:			|
|		• GraphicObject class hierarchy										|
|		• an interface type that specifies functionality (here animation)	|
|		• Use of PImage as graphic content for objects.						|
|		• An example of keyframed animation;								|
|		• Use of back buffer rendering for double buffering and also		|
|			to produce PImage content for rendering.						|
|																			|
|	Keyboard commands:														|
|		• ' ' pauses/resumes animation										|
|		• 'b' set the animation mode to "BOX_WORLD"							|
|		• 'c' set the animation mode to "CYLINDER_WORLD"					|
|		• 'f' displays the objects' reference frames						|
|		• 'a' displays the objects' absolute bounding boxes					|
|		• 'r' displays the objects' relative bounding boxes					|
|		• 'n' disables display of the objects' bounding boxes				|
|																			|
|	Points worthy of notice:												|
|		1.  I use two enumerated types to store "mode" information: one	to	|
|			store the type of bounding box to use (BoxRenderingMode has		|
|			three possible values: absolute, relative, and none), and one 	|
|			to choose the animation mode (AnimationMode; currently only two	|
|			modes are implemented: BOX and CYLINDER).						|
|		2.	Support for bounding boxes has been moved to the GraphicObject	|
|			class.  Every GraphicObject subclass must implement the method	|
|			updateAbsoluteBoxes_().											|
|		3.	As discussed in class, I implemented an abstract parent class	|
|			KeyframeInterpolator, of which LinearKeyframeInterpolator,		|
|			CubicSplineKeyframeInterpolator, etc. would be subclasses that	|
|			implement a particular kind of interpolation.					|
|		4.	A KeyframeInterpolator is created with a list of key-frames to	|
|			work with, and will return the interpolated state vector for a	|
|			given time t.  This class should be enhanced by adding the		|
|			possibility for an animation to repeat a given number of times	|
|			or forever, to chain animations, etc.							|
|		5. 	Right now, only KeyframedFace uses a KeyframeInterpolator, but	|
|			really, any animated GraphicObject should be able to take a		|
|			KeyframeInterpolator as argument of its constructor (next 		|
|			revised version).												|
|																			|
|	Jean-Yves Hervé, Nov. 2012 (version for design grad students).			|
|					 Revised Nov. 2019 for CSC406.							| 
+--------------------------------------------------------------------------*/

public class EarShooterMainClass extends PApplet implements ApplicationConstants 
{
	//-----------------------------
	//	graphical objects
	//-----------------------------
	ArrayList<GraphicObject> objectList_;
	

	ArrayList<Missile> missileList_;
	//-----------------------------
	//	Various status variables
	//-----------------------------
	/**	Desired rendering frame rate
	 * 
	 */
	static final float RENDERING_FRAME_RATE = 60;
	
	/**	Ratio of animation frames over rendering frames 
	 * 
	 */
	static final int ANIMATION_RENDERING_FRAME_RATIO = 5;
	
	/**	computed animation frame rate
	 * 
	 */
	static final float ANIMATION_FRAME_RATE = RENDERING_FRAME_RATE * ANIMATION_RENDERING_FRAME_RATIO;
	
	
	/**	Variable keeping track of the last time the update method was invoked.
	 * 	The different between current time and last time is sent to the update
	 * 	method of each object. 
	 */
	private int lastUpdateTime_;
	
	/**	A counter for animation frames
	 * 
	 */
	private int frameCount_;


	/**	Whether to draw the reference frame of each object (for debugging
	 * purposes.
	 */
	private boolean drawRefFrame_ = false;

	/**	Used to pause the animation
	 */
	boolean animate_ = true;
	
	/**
	 * 	How we handle edges of the window
	 */
	private AnimationMode animationMode_ = AnimationMode.BOX_WORLD;
	
	/**	The off-screen buffer used for double buffering (and display content
	 * for some rectangles.
	 */
	private PGraphics offScreenBuffer_;

	/**	Previous value of the off-screen buffer (after the last call to draw()
	 */
	private PGraphics lastBuffer_;

	/** Toggles on-off double buffering.
	 */
	private boolean doDoubleBuffer_ = false;
	
	/** PImage for background
	 */
	private PImage bg;
	
	/** object spaceship
	 */
	private Spaceship spaceship_;


	public void settings() 
	{
		//  dimension of window in pixels
		size(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	public void setup() 
	{    
		if (BAIL_OUT_IF_ASPECT_RATIOS_DONT_MATCH)
		{
			if (Math.abs(WORLD_HEIGHT - PIXEL_TO_WORLD*WINDOW_HEIGHT) > 1.0E5)
			{
				System.out.println("World and Window aspect ratios don't match");
				System.exit(1);
			}
		}
		
		frameRate(ANIMATION_FRAME_RATE);
		frameCount_ = 0;

		//	Loading data files should be done in the setup method
		PImage []image = {	loadImage("astroid4.png"),
							loadImage("astroid8.png"),
							loadImage("astroid5.png")
							};
		
		image[0].resize(width/5, height/3);
		bg = loadImage("space2.jpg");
		image(bg, 0, 0);
		bg.resize(width, height);
		image(bg, 0, 0);

		objectList_ = new ArrayList<GraphicObject>();
		
		missileList_ = new ArrayList<Missile>();
		

		spaceship_ = new Spaceship();
			objectList_.add(spaceship_);
				
		//	Add a few astroids 
		for (int k=0; k<8; k++) {
			objectList_.add(new Astroid(image[(int)random(image.length)],	//	random image
									random(0.02f) + 0.01f));			//	random scale [0.01 - 0.03]
		}

		//	I allocate my off-screen buffers at the same dimensions as the window
		offScreenBuffer_ = createGraphics(width, height);
		lastBuffer_ = createGraphics(width, height);
		//	Fill the last buffer with something (all white).
		lastBuffer_.beginDraw();
		lastBuffer_.fill(255);
		lastBuffer_.rect(0, 0, width, height);
		lastBuffer_.endDraw();
	}


	/** Processing sketch rendering callback function
	 * 
	 */
	public void draw(){
		//================================================================
		//	Only render one frame out of ANIMATION_RENDERING_FRAME_RATIO
		//================================================================
		if (frameCount_ % ANIMATION_RENDERING_FRAME_RATIO == 0) {
			PGraphics gc;
			if (doDoubleBuffer_) {
				//	I say that the drawing will take place inside of my off-screen buffer
				gc = offScreenBuffer_;
				offScreenBuffer_.beginDraw();
			}
			else
				//	Otherwise, the "graphic context" is that of this PApplet
				gc = this.g;

			gc.background(bg);

			// define world reference frame:  
			//    Origin at windows's center and 
			//    y pointing up
			//    scaled in world units
			gc.translate(WORLD_X, WORLD_Y); 
			gc.scale(DRAW_IN_WORLD_UNITS_SCALE, -DRAW_IN_WORLD_UNITS_SCALE);

			if (drawRefFrame_)
				GraphicObject.drawReferenceFrame(gc);

			if ((animationMode_ == AnimationMode.BOX_WORLD) || (animationMode_ == AnimationMode.WINDOW_WORLD)) {
				
				for (GraphicObject obj : objectList_)
					obj.draw(gc);
				
				for (Missile obj : missileList_)
					obj.draw(gc);
			}
			else {
				for (GraphicObject obj : objectList_)
					obj.drawAllQuadrants(gc);
				
				for (Missile obj : missileList_)
					obj.drawAllQuadrants(gc);
			}
			
			if (doDoubleBuffer_){
				offScreenBuffer_.endDraw();

				image(offScreenBuffer_, 0, 0);				
	
				//	For some reason this doesn't work and I don't understand why.
				lastBuffer_.beginDraw();
				lastBuffer_.image(offScreenBuffer_, 0, 0);
				lastBuffer_.endDraw();

				int []pixelLB = lastBuffer_.pixels;
				int []pixelOSB = offScreenBuffer_.pixels;

				for (int i=0, k=height-1; i<height; i++,k--)
					for (int j=0; j<width; j++)
						pixelLB[k*width + j] = pixelOSB[i*width + j];
				
				lastBuffer_.updatePixels();
			}
		}
			
		//  and then update their state
			if (animate_) {
				update();
			}
			
			
			if (keyPressed) {
				if (key == CODED) {
					switch (keyCode) {
					case LEFT:
						spaceship_.spinLeft();
						break;
						
					case RIGHT:
						spaceship_.spinRIGHT();
						break;
						
					case UP:
						spaceship_.applyThrust();
						break;
					//EXTRA CREDIT	
					case DOWN:
						spaceship_.reverse_applyThrust();
						break;
					}
				}
				else switch(key) {

					case 'w':
						spaceship_.applyThrust();
						break;

					case 'a':
						spaceship_.spinLeft();
						break;

					case 'd':
						spaceship_.spinRIGHT();
						break;
				}	
			}	

		frameCount_++;
	}
	
	public void update() {

		int time = millis();

		if (animate_)
		{
		//USED TO MOVE OBJECT RANDOMLY
			//  update the state of the objects ---> physics
			float dt = (time - lastUpdateTime_)*0.001f;
			
			for (GraphicObject obj : objectList_){
				if (obj instanceof AnimatedObject)
					obj.update(dt);	
			}
			for (Missile obj : missileList_)
				obj.update(dt);
		}

		lastUpdateTime_ = time;
	}
	

	/** keyReleased() 
	 * Used to interact with the game by pressing certain keys
	 */
	public void keyReleased() 
	{
		switch(key) {
			case ' ':
				Missile []shots = spaceship_.shoot();
				missileList_.add(shots[0]);
				missileList_.add(shots[1]);
				break;
			
			case 'p':
				animate_ = !animate_;
				if (animate_)
					lastUpdateTime_ = millis();
				break;
				
			case 'n':
				GraphicObject.setBoundingBoxMode(BoundingBoxMode.NO_BOX);
				break;
	
			case 'r':
				GraphicObject.setBoundingBoxMode(BoundingBoxMode.RELATIVE_BOX);
				break;
	
			case 'e':
				GraphicObject.setBoundingBoxMode(BoundingBoxMode.ABSOLUTE_BOX);
				break;
	
			case 'f':
				drawRefFrame_ = !drawRefFrame_;
				GraphicObject.setDrafReferenceFrame(drawRefFrame_);
				break;
	
			case 'b':
				GraphicObject.setAnimationMode(AnimationMode.BOX_WORLD);
				animationMode_ = AnimationMode.BOX_WORLD;
				break;
	
			case 'c':
				GraphicObject.setAnimationMode(AnimationMode.CYLINDER_WORLD);
				animationMode_ = AnimationMode.CYLINDER_WORLD;
				break;

			case 't':
				GraphicObject.setAnimationMode(AnimationMode.TORUS_WORLD);
						animationMode_ = AnimationMode.TORUS_WORLD;
				break;

			case 'd':
				doDoubleBuffer_ = !doDoubleBuffer_;
				break;
				
			case 'y':
				GraphicObject.setAnimationMode(AnimationMode.WINDOW_WORLD);
				animationMode_ = AnimationMode.WINDOW_WORLD;
				break;
				
			default:
				break;
		}
	}


	public static void main(String[] argv)
	{
		PApplet.main("prog04.EarShooterMainClass");
	}

}
