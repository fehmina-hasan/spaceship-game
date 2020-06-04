package prog04;

import processing.core.PApplet;
import processing.core.PGraphics;

//NEW
//	The Face class is now derived from GraphicObject
/** Graphic class to draw a face.
 * 
 * @author jyh
 *
 */
public class Spaceship extends GraphicObject implements ApplicationConstants, AnimatedObject {
	
	public static final int WHOLE_SHIP = 0;
	public static final int LEFT_WING = 1;
	public static final int RIGHT_WING = 2;
	public static final int SHIP_FRONT = 3;
	public static final int BACK_ENGINE = 4;
	public static final int BOTH_WINGS = 5;
	public static final int PARTS = 5;
	public static final int []PART_COLORS =  {	
			0xFFFF0000,	//	WHOLE SHIP     red color
			//
			0xFF00FF00,	//	LEFT_WING      green color
			0xFF00FF00,	//	RIGHT_WING     green color
			0xFF42C5F5,	//	SHIP FRONT     blue color
			0xFF0000FF, //  BACK ENGINE    blue color
			0x0FFFFFFF,	//	BOTH_WINGS     invisible 
		};
	
	public static final float SHIP_DIAMETER = 5;
	//Spaceship Variables
	public static final float ENGINE_V_OFFSET = 0.0f*SHIP_DIAMETER;
	public static final float ENGINE_H_OFFSET = 0.4f*SHIP_DIAMETER;
	
	private static final float LEFT_WING_X = 0;
	private static final float LEFT_WING_Y = 3;
	private static final float RIGHT_WING_X = 0;
	private static final float RIGHT_WING_Y = 3;
	
	private static final float LEFT_GUN_X = LEFT_WING_X -1;   //LEFT_WING_X - 1,LEFT_WING_Y
	private static final float LEFT_GUN_Y = LEFT_WING_Y;
	private static final float RIGHT_GUN_X = RIGHT_WING_X + 1;   //RIGHT_WING_X + 1,RIGHT_WING_Y
	private static final float RIGHT_GUN_Y = RIGHT_WING_Y;
	
	
	
	//Engine Metrics
	private static final float LEFT_ENGINE_X = -ENGINE_H_OFFSET;
	private static final float LEFT_ENGINE_Y = ENGINE_V_OFFSET;
	private static final float RIGHT_ENGINE_X = ENGINE_H_OFFSET;
	private static final float RIGHT_ENGINE_Y = ENGINE_V_OFFSET;

	private static final float ANGLE_INCR = 0.03f;
	private static final float ACCEL = 0.03f;  //0.20f
	
	private static final float V_MISSILE = WORLD_WIDTH/20;
	private static final float GUN_TIP = 2f;
	
	/** Spaceship()
	 * 
	 * Create the relative bounding boxes for all parts of the spaceship
	 * call the absolute bounding box function as well
	 */
	public Spaceship() {

		//	Our random initialization is now taken care of in the parent class's constructor.
		super();
		spin_ = 0;
		
		//	Create the relative bounding boxes
		relativeBox_ = new BoundingBox[PARTS+1];
		relativeBox_[LEFT_WING] = new BoundingBox(LEFT_ENGINE_X - SHIP_DIAMETER + 2, 	//	xmin
												 LEFT_ENGINE_X + SHIP_DIAMETER-3,		//	xmax
												 LEFT_ENGINE_Y - SHIP_DIAMETER/5, 		//	ymin
												 LEFT_ENGINE_Y + SHIP_DIAMETER-2, 		//	ymax
												 PART_COLORS[LEFT_WING]);
		
		relativeBox_[RIGHT_WING] = new BoundingBox(RIGHT_ENGINE_X - SHIP_DIAMETER + 3, 	//	xmin
												  RIGHT_ENGINE_X + SHIP_DIAMETER - 2,	//	xmax
												  RIGHT_ENGINE_Y - SHIP_DIAMETER/5, 	//	ymin
												  RIGHT_ENGINE_Y + SHIP_DIAMETER-2, 	//	ymax
												  PART_COLORS[RIGHT_WING]);
		
		relativeBox_[SHIP_FRONT] = new BoundingBox(LEFT_ENGINE_X - SHIP_DIAMETER/5 +1, 	//	xmin left
												   LEFT_ENGINE_X + SHIP_DIAMETER-1,		//	xmax right
												   LEFT_ENGINE_Y-SHIP_DIAMETER +6, 		//	ymin bottom
												  +SHIP_DIAMETER, 						//	ymax top
												  PART_COLORS[SHIP_FRONT]);
			
		relativeBox_[BACK_ENGINE] = new BoundingBox(LEFT_ENGINE_X - SHIP_DIAMETER/5, 	//	xmin left
												    RIGHT_ENGINE_X + SHIP_DIAMETER/5,	//	xmax right
												    -SHIP_DIAMETER +3, 					//	ymin bottom
												    RIGHT_ENGINE_Y + SHIP_DIAMETER/5 -2, 	//	ymax top
												  PART_COLORS[BACK_ENGINE]);
		
		relativeBox_[WHOLE_SHIP] = new BoundingBox( LEFT_ENGINE_X - SHIP_DIAMETER +2, 	//	xmin
													RIGHT_ENGINE_X + SHIP_DIAMETER - 2,	//	xmax
												  	-SHIP_DIAMETER +3, 					//	ymin 
												  	LEFT_ENGINE_Y + SHIP_DIAMETER , 	//	ymax 
												  	PART_COLORS[WHOLE_SHIP]);
		
		//	create the absolute boxes
		absoluteBox_ = new BoundingBox[PARTS+1];
		

		for (int k=0; k<= PARTS; k++)
		{
			absoluteBox_[k] = new BoundingBox(PART_COLORS[k]);
		}
		updateAbsoluteBoxes_();
	}

	
	/**	draw(PGraphics g, BoundingBoxMode boxMode)
	 * 
	 * @param app		reference to the sketch
	 * @param theMode	should the object be drawn with a bounding box?
	 * 
	 * Used to display or remove the bounding boxes from the screen
	 */
	public void draw(PGraphics g, BoundingBoxMode boxMode)
	{
		//NEW
		//	Invokes method declared in the parent class, that draws the object
		draw_(g);
		
		//	Then draw the boxes, if needed

		if (boxMode == BoundingBoxMode.RELATIVE_BOX)
		{
			// we use this object's instance variable to access the application's instance methods and variables
			g.pushMatrix();

			g.translate(x_,  y_);
			g.rotate(angle_);		

			for (BoundingBox box : relativeBox_)
				box.draw(g);
			
			g.popMatrix();	
		}
		
		else if (boxMode == BoundingBoxMode.ABSOLUTE_BOX)
		{
			for (BoundingBox box : absoluteBox_)
				if (box != null)
					box.draw(g);
			
		}
	}


	/** drawAllQuadrants(PGraphics g, BoundingBoxMode boxMode)
	 * 
	 * @param g		the graphic object
	 * @param theMode	should the object be drawn with a bounding box?
	 * 
	 */
	public void drawAllQuadrants(PGraphics g, BoundingBoxMode boxMode)
	{
		draw(g, boxMode);
		
		if (shouldGetDrawnInQuadrant_[NORTH_WEST])
		{
			g.pushMatrix();
			g.translate(XMIN-XMAX, YMIN-YMAX);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[NORTH])
		{
			g.pushMatrix();
			g.translate(0, YMIN-YMAX);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[NORTH_EAST])
		{
			g.pushMatrix();
			g.translate(XMAX-XMIN, YMIN-YMAX);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[EAST])
		{
			g.pushMatrix();
			g.translate(XMAX-XMIN, 0);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[SOUTH_EAST])
		{
			g.pushMatrix();
			g.translate(XMAX-XMIN, YMAX-YMIN);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[SOUTH])
		{
			g.pushMatrix();
			g.translate(0, YMAX-YMIN);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[SOUTH_WEST])
		{
			g.pushMatrix();
			g.translate(XMIN-XMAX, YMAX-YMIN);
			draw(g, boxMode);
			g.popMatrix();
		}
		if (shouldGetDrawnInQuadrant_[WEST])
		{
			g.pushMatrix();
			g.translate(XMIN-XMAX, 0);
			draw(g, boxMode);
			g.popMatrix();
		}
	}

	/**	renders the Face object
	 * 
	 * @param app		reference to the sketch
	 * @param theMode	should the object be drawn with a bounding box?
	 */
	public void draw(PGraphics g, BoundingBoxMode boxMode, int quad)
	{
		if (quad >= NORTH && quad <= NORTH_EAST && shouldGetDrawnInQuadrant_[quad])
		{
			//	Invokes method declared in the parent class, that draws the object
			draw_(g);

			//	Then draw the boxes, if needed

			if (boxMode == BoundingBoxMode.RELATIVE_BOX)
			{
				// we use this object's instance variable to access the application's instance methods and variables
				g.pushMatrix();

				g.translate(x_,  y_);
				g.rotate(angle_);		

				for (BoundingBox box : relativeBox_)
					box.draw(g);

				g.popMatrix();	
			}

			else if (boxMode == BoundingBoxMode.ABSOLUTE_BOX)
			{
				for (BoundingBox box : absoluteBox_)
					if (box != null)
						box.draw(g);

			}
		}
	}

	/**	draw_(PGraphics g)
	 * 
	 * @param app		reference to the sketch
	 * 
	 * This function is used to model and draw the Spaceship using different shapes and translating them
	 * to get the desired Spaceship dimensions.
	 */
	public void draw_(PGraphics g)
	{
		// we use this object's instance variable to access the application's instance methods and variables
		g.pushMatrix();
		
		g.fill(255);
		g.stroke(50);
		g.fill(40);
		g.fill(color_);
		//g.noStroke();
		g.triangle(LEFT_WING_X, LEFT_WING_Y+2, LEFT_WING_X-2, LEFT_WING_Y-2, RIGHT_WING_X+2,  RIGHT_WING_Y-2);  //SHIP FRONT  
		g.triangle(LEFT_WING_X, LEFT_WING_Y+2, LEFT_WING_X-2, LEFT_WING_Y-2, RIGHT_WING_X+2,  RIGHT_WING_Y-2);  //SHIP FRONT  
		g.triangle(RIGHT_WING_X, RIGHT_WING_Y, RIGHT_WING_X, -1, 5, -1);  //RIGHT WING
		g.triangle(LEFT_WING_X, LEFT_WING_Y, LEFT_WING_X, -1, LEFT_WING_X -5, -1); // LEFT WING
		g.rect(RIGHT_ENGINE_X - SHIP_DIAMETER, RIGHT_ENGINE_Y - SHIP_DIAMETER +3 , 120*PIXEL_TO_WORLD, 20*PIXEL_TO_WORLD); //BACK ENGINE
		
		//	draw the left aim shoot
		g.pushMatrix();
		g.fill(color_);
		g.translate(LEFT_ENGINE_X , LEFT_ENGINE_Y );
		g.stroke(30);
		g.strokeWeight(7*PIXEL_TO_WORLD);
		g.line(LEFT_WING_X - 1,LEFT_WING_X,LEFT_WING_X - 1,LEFT_WING_Y);
		g.popMatrix();  

		//	draw the right aim shoot
		g.pushMatrix();
		g.fill(color_);
		g.translate(RIGHT_ENGINE_X , RIGHT_ENGINE_Y );
		g.stroke(30);
		g.strokeWeight(7*PIXEL_TO_WORLD);
		g.line(RIGHT_WING_X + 1,RIGHT_WING_X,RIGHT_WING_X + 1,RIGHT_WING_Y);
		g.popMatrix();  


		g.popMatrix();	
	}

	/**
	 * 	Computes the new dimensions of the object's absolute bounding boxes, for
	 * the object's current position and orientation.
	 * Since my space ship contains triangular shapes I used the formulas in the sakai forum
	 * to help get the bounding boxes
	 */
	protected void updateAbsoluteBoxes_() {
		float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
		
		//SHIP FRONT
		final float SF_TOP_X = LEFT_WING_X,
					SF_TOP_Y = LEFT_WING_Y+2,
					SF_BOTTOM_LEFT_X = LEFT_WING_X-2,       
				    SF_BOTTOM_LEFT_Y =  LEFT_WING_Y-2,       
				    SF_BOTTOM_RIGHT_X = RIGHT_WING_X+2,
					SF_BOTTOM_RIGHT_Y =  RIGHT_WING_X+1;
					
		float rLFX = x_ + cA*SF_BOTTOM_LEFT_X - sA*SF_BOTTOM_LEFT_Y;
		float rLFY = y_ + sA*SF_BOTTOM_LEFT_X + cA*SF_BOTTOM_LEFT_Y;
		float rRFX = x_ + cA*SF_BOTTOM_RIGHT_X - sA*SF_BOTTOM_RIGHT_Y;
		float rRFY = y_ + sA*SF_BOTTOM_RIGHT_X + cA*SF_BOTTOM_RIGHT_Y;
		float rSFX = x_ + cA*SF_TOP_X - sA*SF_TOP_Y;
		float rSFY = y_ + sA*SF_TOP_X + cA*SF_TOP_Y;

		absoluteBox_[SHIP_FRONT].updatePosition(Math.min(rSFX, Math.min(rLFX, rRFX)),	//	xmin
				Math.max(rSFX, Math.max(rLFX, rRFX)),	//	xmax
				Math.min(rSFY, Math.min(rLFY, rRFY)),	//	xmin
				Math.max(rSFY, Math.max(rLFY, rRFY)));	//	ymax

		
		
		//BACK ENGINE
		final float ENG_UL_X = RIGHT_ENGINE_X - SHIP_DIAMETER, 
				ENG_UL_Y = RIGHT_ENGINE_Y - SHIP_DIAMETER +4 , 
				ENG_W = 120 * PIXEL_TO_WORLD, 
				ENG_H = 20 * PIXEL_TO_WORLD,
				ENG_HW = ENG_W/2, 
				ENG_HH = ENG_H/2,
				ENG_C_X = ENG_UL_X + ENG_W/2,
				ENG_C_Y = ENG_UL_Y - ENG_H/2;
		float rEngX = x_ + cA*ENG_C_X - sA*ENG_C_Y;
		float rEngY = y_ + sA*ENG_C_X + cA*ENG_C_Y;
		
		float 	Bdx = Math.max(Math.abs(cA*ENG_HW - sA*ENG_HH),
				Math.abs(cA*ENG_HW + sA*ENG_HH)),
				Bdy = Math.max(Math.abs(sA*ENG_HW + cA*ENG_HH),
		  		Math.abs(sA*ENG_HW - cA*ENG_HH));

		
		absoluteBox_[BACK_ENGINE].updatePosition(rEngX - Bdx,	//	xmin
													rEngX + Bdx,	//	xmax
													rEngY - Bdy,	//	ymin
													rEngY + Bdy);	//	ymax


		//LEFT WING
		final float LW_TOP_X = LEFT_WING_X,
					LW_TOP_Y = LEFT_WING_Y,
					LW_BOTTOM_LEFT_X = LEFT_WING_X -5,       
					LW_BOTTOM_LEFT_Y =  -1,       
					LW_BOTTOM_RIGHT_X = LEFT_WING_X,
					LW_BOTTOM_RIGHT_Y =  -1;

		float rLLWX = x_ + cA*LW_BOTTOM_LEFT_X - sA*LW_BOTTOM_LEFT_Y;
		float rLLWY = y_ + sA*LW_BOTTOM_LEFT_X + cA*LW_BOTTOM_LEFT_Y;
		float rRLWX = x_ + cA*LW_BOTTOM_RIGHT_X - sA*LW_BOTTOM_RIGHT_Y;
		float rRLWY = y_ + sA*LW_BOTTOM_RIGHT_X + cA*LW_BOTTOM_RIGHT_Y;
		float rLWX = x_ + cA*LW_TOP_X - sA*LW_TOP_Y;
		float rLWY = y_ + sA*LW_TOP_X + cA*LW_TOP_Y;
		
		
		absoluteBox_[LEFT_WING].updatePosition(Math.min(rLWX, Math.min(rLLWX, rRLWX)),	//	xmin
				Math.max(rLWX, Math.max(rLLWX, rRLWX)),	//	xmax
				Math.min(rLWY, Math.min(rLLWY, rRLWY)),	//	xmin
				Math.max(rLWY, Math.max(rLLWY, rRLWY)));	//	ymax

	
        //RIGHT WING
		final float RW_TOP_X = RIGHT_WING_X,
					RW_TOP_Y = RIGHT_WING_Y,
					RW_BOTTOM_LEFT_X = RIGHT_WING_X,       
					RW_BOTTOM_LEFT_Y =  -1,       
					RW_BOTTOM_RIGHT_X = RIGHT_WING_X+5,
					RW_BOTTOM_RIGHT_Y =  -1;
		
		float rLRWX = x_ + cA*RW_BOTTOM_LEFT_X - sA*RW_BOTTOM_LEFT_Y;
		float rLRWY = y_ + sA*RW_BOTTOM_LEFT_X + cA*RW_BOTTOM_LEFT_Y;
		
		float rRRWX = x_ + cA*RW_BOTTOM_RIGHT_X - sA*RW_BOTTOM_RIGHT_Y;
		float rRRWY = y_ + sA*RW_BOTTOM_RIGHT_X + cA*RW_BOTTOM_RIGHT_Y;
		float rRWX = x_ + cA*RW_TOP_X - sA*RW_TOP_Y;
		float rRWY = y_ + sA*RW_TOP_X + cA*RW_TOP_Y;

		absoluteBox_[RIGHT_WING].updatePosition(Math.min(rRWX, Math.min(rLRWX, rRRWX)),	//	xmin
				Math.max(rRWX, Math.max(rLRWX, rRRWX)),	//	xmax
				Math.min(rRWY, Math.min(rLRWY, rRRWY)),	//	xmin
				Math.max(rRWY, Math.max(rLRWY, rRRWY)));	//	ymax
		
		
		

		//RIGHT WING + LEFT WING COMBINED
		final float TOP_X = RIGHT_WING_X,
					TOP_Y = RIGHT_WING_Y,
					BOTTOM_LEFT_X = LEFT_WING_X -5,       
					BOTTOM_LEFT_Y =  -1,       
					BOTTOM_RIGHT_X = RIGHT_WING_X+5,
					BOTTOM_RIGHT_Y =  -1
					;
		
		float rLX = x_ + cA*BOTTOM_LEFT_X - sA*BOTTOM_LEFT_Y;
		float rLY = y_ + sA*BOTTOM_LEFT_X + cA*BOTTOM_LEFT_Y;
		float rRX = x_ + cA*BOTTOM_RIGHT_X - sA*BOTTOM_RIGHT_Y;
		float rRY = y_ + sA*BOTTOM_RIGHT_X + cA*BOTTOM_RIGHT_Y;
		float rX = x_ + cA*TOP_X - sA*TOP_Y;
		float rY = y_ + sA*TOP_X + cA*TOP_Y;

		absoluteBox_[BOTH_WINGS].updatePosition(Math.min(rX, Math.min(rLX, rRX)),	//	xmin
				Math.max(rX, Math.max(rLX, rRX)),	//	xmax
				Math.min(rY, Math.min(rLY, rRY)),	//	xmin
				Math.max(rY, Math.max(rLY, rRY)));	//	ymax
		
		
		
		
		absoluteBox_[WHOLE_SHIP].updatePosition(PApplet.min(//absoluteBox_[LEFT_WING].getXmin(),
														 absoluteBox_[BOTH_WINGS].getXmin(),
														 absoluteBox_[BACK_ENGINE].getXmin(),
														 absoluteBox_[SHIP_FRONT].getXmin()),// xmin
				
												PApplet.max(//absoluteBox_[LEFT_WING].getXmax(),
															absoluteBox_[BOTH_WINGS].getXmax(),
															absoluteBox_[BACK_ENGINE].getXmax(),
															absoluteBox_[SHIP_FRONT].getXmax()),	// xmax
												
												PApplet.min(//absoluteBox_[LEFT_WING].getYmin(),
															absoluteBox_[BOTH_WINGS].getYmin(),
															absoluteBox_[BACK_ENGINE].getYmin(),
															absoluteBox_[SHIP_FRONT].getYmin()),	// ymin
												
												PApplet.max(//absoluteBox_[LEFT_WING].getYmax(),
														    absoluteBox_[BOTH_WINGS].getYmax(),
															absoluteBox_[BACK_ENGINE].getYmax(),
															absoluteBox_[SHIP_FRONT].getYmax()));	// xmax;
		
	}

	/**	Performs a hierarchical search to determine whether the point received
	 * as parameter is inside the Spaceship.
	 * 
	 * @param x		x coordinate of a point in the world reference frame
	 * @param y		y coordinate of a point in the world reference frame
	 * @return	true if the point at (x, y) lies inside this spaceship object.
	 */
	public boolean isInside(float x, float y) {
		boolean inside = false;
		//	If the point is inside the global absolute bounding box
		if (absoluteBox_[WHOLE_SHIP].isInside(x, y)) {
			//	test if the point is inside one of the sub-boxes
			//	Remember that Java (like C, C++, Python) uses lazy evaluation,
			//	so as soon as one test returns true, the evaluation ends.
			inside = absoluteBox_[LEFT_WING].isInside(x, y) ||
					 absoluteBox_[RIGHT_WING].isInside(x, y) ||
					 absoluteBox_[BACK_ENGINE].isInside(x, y) ||
					 absoluteBox_[SHIP_FRONT].isInside(x, y);
		}
		
		return inside;
	}
	
	/**	spinLeft()
	 * used to change the angle counterclockwise
	 */
	public void spinLeft() {
		angle_ += ANGLE_INCR;
	}
	
	/**	spinRight()
	 * used to change the angle clockwise
	 */
	public void spinRIGHT() {
		angle_ -= ANGLE_INCR;
	}
	
	/**	applyThrust()
	 * used to provide acceleration
	 */
	public void applyThrust() {
		vx_ += -ACCEL * Math.sin(angle_);
		vy_ += ACCEL  * Math.cos(angle_);
	
	}
	
	/**	reverse_applyThrust()
	 * used to provide de-acceleration
	 */
	public void reverse_applyThrust() {
		vx_ -= -ACCEL * Math.sin(angle_);
		vy_ -= ACCEL  * Math.cos(angle_);
	}
	
	/**	public Missile[] shoot()
	 * used to shoot from the guns in the spaceship in the direction it is going
	 */
	public Missile[] shoot(){
		float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
		Missile []miss = {
		//new Missile(),
		new Missile(x_ + cA*LEFT_GUN_X - sA*(LEFT_GUN_Y+ GUN_TIP),     //+ GUN_TIP
					y_ + sA*LEFT_GUN_X + cA*(LEFT_GUN_Y+ GUN_TIP),     //+ GUN_TIP
					angle_,
					vx_ - V_MISSILE * sA,
					vy_ + V_MISSILE * cA), 

		new Missile(x_ + cA*RIGHT_GUN_X - sA*(RIGHT_GUN_Y+ GUN_TIP ),   //+ GUN_TIP
					y_ + sA*RIGHT_GUN_X + cA*(RIGHT_GUN_Y+ GUN_TIP ),   //+ GUN_TIP
					angle_,
					vx_ - V_MISSILE * sA,
					vy_ + V_MISSILE * cA)

		};

		return miss;
		}
	
	
	
}