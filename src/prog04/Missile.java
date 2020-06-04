package prog04;

import processing.core.*; 

/**	The Missile class is rewritten as a subclass of GraphicObject
 * 
 * @author jyh
 *
 */
public class Missile extends GraphicObject 
{
	private static final float MISSILE_W = 0.5f;
	private static final float MISSILE_H = 0.7f;
	
	/**	
	 * 	The image to display as a "fill" for this object
	 */
	PImage image_;
	
	/**
	 * 	Horizontal scale to apply to the image
	 */
	float scaleX_;
	
	/**
	 * 	Vertical scale to apply to the image
	 */
	float scaleY_;

	/**	Constructor. Creates a random object at set location 
	 * 
	 * @param x			x coordinate of the object's center (in world coordinates)
	 * @param y			y coordinate of the object's center (in world coordinates)
	 */
	public Missile(float x, float y, float angle, float vx, float vy)
	{

		super(x, y, angle, MISSILE_W, MISSILE_H, 0xFF32CD32, vx, vy, 0);

		setupDefaultBoundingBoxes_();
	}

	/**	Rendering code specific to Missiles
	 * 
	 * @param g	The Processing application in which the action takes place
	 */
	protected void draw_(PGraphics g)
	{
		g.pushMatrix();
		g.translate(-width_/2,  -height_/2);
		if (image_ == null){
			g.fill(color_);
//			g.strokeWeight(3*DRAW_IN_PIXELS_SCALE);
//			g.stroke(255);
			g.rect(0,0,width_/2, height_);
		}
		else
		{
			g.scale(scaleX_, scaleY_);
			g.image(image_, 0, 0,  image_.width, image_.height);
		}
		g.popMatrix();
	}

	/**	updateAbsoluteBoxes_()
	 * 	Used to update the absolute boxes for the missiles
	 */
	protected void updateAbsoluteBoxes_()
	{
		//	could definitely be optimized
		float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
		float hwidth = width_/2, hheight = height_/2;
		float dx = Math.max(Math.abs(cA*hwidth - sA*hheight),
							Math.abs(cA*hwidth + sA*hheight)),
			  dy = Math.max(Math.abs(sA*hwidth + cA*hheight),
					  		Math.abs(sA*hwidth - cA*hheight));
		
		float	[]cornerX = {	x_ - dx,	//	upper left
								x_ + dx,	//	upper right
								x_ + dx,	//	lower right
								x_ - dx};	//	lower left
		
		float	[]cornerY = {	y_ - dy,	//	upper left
								y_ + dy,	//	upper right
								y_ + dy,	//	lower right
								y_ - dy};	//	lower left
				
		absoluteBox_[0].updatePosition(	PApplet.min(cornerX),	//	xmin
										PApplet.max(cornerX),	//	xmax
										PApplet.min(cornerY),	//	ymin
										PApplet.max(cornerY));	//	ymax
		
	}
	
	
	/** isInside(float x, float y)
	 * 
	 * @param x	get the x boundary of the object
	 * @param y	get the y boundary of the object
	 */
	public boolean isInside(float x, float y){
		//	Convert x and y into this object's reference frame coordinates
		double cosAngle = Math.cos(angle_), sinAngle = Math.sin(angle_);
		double xb = cosAngle*(x - x_) + sinAngle*(y - y_);
		double yb = cosAngle*(y - y_) + sinAngle*(x_ - x);
		return ((PApplet.abs((float) xb) <= width_/2) && (PApplet.abs((float) yb) <= height_/2));
	}
	
}