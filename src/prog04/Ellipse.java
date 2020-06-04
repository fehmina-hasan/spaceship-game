package prog04;

import processing.core.*; 

/**	The Ellipse class rewritten as a subclass of GraphicObject
 * 
 * @author jyh
 *
 */
public class Ellipse extends GraphicObject 
{

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
	
	
	/**	Constructor. Initializes all instance variables to the values set by the arguments
	 * 
	 * @param x			x coordinate of the object's center (in world coordinates)
	 * @param y			y coordinate of the object's center (in world coordinates)
	 * @param angle		orientation of the object (in rad)
	 * @param width		width of the object (in world units)
	 * @param height	height of the object (in world units)
	 * @param color		The color is stored as a single int in hex format
	 */
	public Ellipse(float x, float y, float angle, float width, float height, int color)
	{
		super(x, y, angle, width, height, color);

		setupDefaultBoundingBoxes_();
	}
	
	/**	Constructor. Creates a random object at set location 
	 * 
	 * @param x			x coordinate of the object's center (in world coordinates)
	 * @param y			y coordinate of the object's center (in world coordinates)
	 */
	public Ellipse(float x, float y)
	{
		super(x, y);

		setupDefaultBoundingBoxes_();
	}
	
	/**	Default constructor. Initializes all instance variables with random values.
	 */
	public Ellipse()
	{
		super();

		setupDefaultBoundingBoxes_();
		
	}
	
	/**	Initializes all instance variables with random values, except for the filling image
	 * @param image		The image to draw inside this box
	 * @param scale		scale to apply to the image
	 */
	public Ellipse(PImage image, float scale)
	{
		super();

		width_ = image.width*scale;
		height_ = image.height*scale;
		image_ = image;
		scaleX_ = scaleY_ = scale;

		setupDefaultBoundingBoxes_();
	}

	

	/**	Rendering code specific to ellipses
	 * 
	 * @param g	The Processing application in which the action takes place
	 */
	protected void draw_(PGraphics g)
	{
		
		g.pushMatrix();
		g.translate(-width_/2,  -height_/2);
//g.strokeWeight(2*DRAW_IN_PIXELS_SCALE);
//g.noFill();
//g.stroke(0, 255,0);
//g.rect(0, 0, width_, height_);
//g.noStroke();

		if (image_ == null)
			g.ellipse(0,  0,  width_-2, height_-3);
		else
		{
			g.scale(scaleX_, scaleY_);
			g.image(image_, 0, 0,  image_.width, image_.height);
//g.strokeWeight(2*DRAW_IN_PIXELS_SCALE);
//g.stroke(255,0,0);
//g.noFill();
//g.rect(0, 0, image_.width, image_.height);
//g.noStroke();
		}

		g.popMatrix();
	}


	protected void updateAbsoluteBoxes_()
	{
		//	could definitely be optimized
		float cA = PApplet.cos(angle_), sA = PApplet.sin(angle_);
		float hwidth = width_/2, hheight = height_/2;
		float []cornerX;
		float []cornerY;
		
		//----------------------------------------------
		//	General case first
		//----------------------------------------------
		if (Math.abs(cA) > 1E-4 && Math.abs(sA)> 1E-4)
		{
			//	parametric equation of the ellipse is {w/2 cos(t), h/2 sin(t), 0≤t≤2π

			//	Compute the values of t that give us horizontal and vertical tangents
			float tV = (float) Math.atan(-(height_*sA)/(width_*cA));
			float tH = (float) Math.atan((height_*cA)/(width_*sA));
			
			//float dxH = (float) (cA*hwidth*Math.cos(tH) - sA*hheight*Math.sin(tH));
			float dyH = (float) (sA*hwidth*Math.cos(tH) + cA*hheight*Math.sin(tH));
			float dxV = (float) (cA*hwidth*Math.cos(tV) - sA*hheight*Math.sin(tV));
			//float dyV = (float) (sA*hwidth*Math.cos(tV) + cA*hheight*Math.sin(tV));
			
			float	[]tempCX = {	x_ - Math.abs(dxV),		//	upper left
									x_ + Math.abs(dxV),		//	upper right
									x_ + Math.abs(dxV),		//	lower right
									x_ - Math.abs(dxV)};	//	lower left

			float	[]tempCY = {	y_ + Math.abs(dyH),	//	upper left
									y_ + Math.abs(dyH),	//	upper right
									y_ - Math.abs(dyH),	//	lower right
									y_ - Math.abs(dyH)};	//	lower left
			cornerX = tempCX; cornerY = tempCY;
		}
		//	case of ellipse rotated by ± π/2
		else if (Math.abs(cA) <= 1E-4)
		{
			float	[]tempCX = {	x_ - hheight,	//	upper left
									x_ + hheight,	//	upper right
									x_ + hheight,	//	lower right
									x_ - hheight};	//	lower left

			float	[]tempCY = {	y_ + hwidth,	//	upper left
									y_ + hwidth,	//	upper right
									y_ - hwidth,	//	lower right
									y_ - hwidth};	//	lower left
			cornerX = tempCX; cornerY = tempCY;
		}
		//	case of horizontal ellipse
		else //	Math.abs(sA) ≤ 1E-4)
		{
			float	[]tempCX = {	x_ - hwidth,	//	upper left
									x_ + hwidth,	//	upper right
									x_ + hwidth,	//	lower right
									x_ - hwidth};	//	lower left

			float	[]tempCY = {	y_ + hheight,	//	upper left
									y_ + hheight,	//	upper right
									y_ - hheight,	//	lower right
									y_ - hheight};	//	lower left
			cornerX = tempCX; cornerY = tempCY;
		}
				
		absoluteBox_[0].updatePosition(	PApplet.min(cornerX),	//	xmin
										PApplet.max(cornerX),	//	xmax
										PApplet.min(cornerY),	//	ymin
										PApplet.max(cornerY));	//	ymax
		
	}
	
	
	public boolean isInside(float x, float y)
	{
		//	Convert x and y into this object's reference frame coordinates
		double cosAngle = Math.cos(angle_), sinAngle = Math.sin(angle_);
		double xb = cosAngle*(x - x_) + sinAngle*(y - y_);
		double yb = cosAngle*(y - y_) + sinAngle*(x_ - x);

		float dx = 2*((float)xb)/width_, dy = 2*((float) yb)/height_;
		return dx*dx + dy*dy <= 1.0f;
	}
}