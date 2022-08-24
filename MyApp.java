

// /usr/lib/jvm/jdk-18/bin/javac -cp MyApp.java My3D.java
// /usr/lib/jvm/jdk-18/bin/java  -cp MyApp


import java.lang.Math;
import java.awt.*;
import java.awt.Dimension;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;




public class MyApp extends JFrame implements KeyListener {

   final static Color bg = Color.white;
   final static Color fg = Color.black;
   final static Color side1_color = Color.red;
   final static Color side2_color = Color.pink;
   final static Color side3_color = Color.yellow;
   final static Color side4_color = Color.blue;
   final static Color side5_color = Color.green;
   final static Color side6_color = Color.black;

   //java-swing user-coords is X is positive to right, Y is positive down, 0,0 is top-left
   
   //OpenGL camera-space, origin is (0,0,0), X is positive to right, Y is positive up, focal-length == e == 1 == (1/tan(alpha/2)), alpha==90degrees, camera/eye-ball at (0,0,-1)
   //clockwise , surface visible
   //counter-clockwise, surface hidden (not-visible)
   
   My3D Center_Of_Cube_Point = new My3D(0,0,-2.5);

   //front start
   My3D side1_vertex1 = new My3D(-0.5,  0.5, -2);
   My3D side1_vertex2 = new My3D(-0.5, -0.5, -2);
   My3D side1_vertex3 = new My3D( 0.5, -0.5, -2);
   My3D side1_vertex4 = new My3D( 0.5,  0.5, -2);
   My3D side1_normal_vector = CalculateCrossProduct(side1_vertex1, side1_vertex2, side1_vertex3);

   //top start 
   My3D side2_vertex1 = new My3D(-0.5, 0.5, -2);
   My3D side2_vertex2 = new My3D( 0.5, 0.5, -2);
   My3D side2_vertex3 = new My3D( 0.5, 0.5, -3);
   My3D side2_vertex4 = new My3D(-0.5, 0.5, -3);
   My3D side2_normal_vector = CalculateCrossProduct(side2_vertex1, side2_vertex2, side2_vertex3);
   
   //bottom start
   My3D side3_vertex1 = new My3D(-0.5, -0.5, -2);
   My3D side3_vertex2 = new My3D(-0.5, -0.5, -3);
   My3D side3_vertex3 = new My3D( 0.5, -0.5, -3);
   My3D side3_vertex4 = new My3D( 0.5, -0.5, -2);
   My3D side3_normal_vector = CalculateCrossProduct(side3_vertex1, side3_vertex2, side3_vertex3);

   //right start
   My3D side4_vertex1 = new My3D(0.5,  -0.5, -2);
   My3D side4_vertex2 = new My3D(0.5,  -0.5, -3);
   My3D side4_vertex3 = new My3D(0.5,   0.5, -3);
   My3D side4_vertex4 = new My3D(0.5,   0.5, -2);
   My3D side4_normal_vector = CalculateCrossProduct(side4_vertex1, side4_vertex2, side4_vertex3);

   //left start
   My3D side5_vertex1 = new My3D(-0.5, -0.5, -2);
   My3D side5_vertex2 = new My3D(-0.5,  0.5, -2);
   My3D side5_vertex3 = new My3D(-0.5,  0.5, -3);
   My3D side5_vertex4 = new My3D(-0.5, -0.5, -3);
   My3D side5_normal_vector = CalculateCrossProduct(side5_vertex1, side5_vertex2, side5_vertex3);

   //back start
   My3D side6_vertex1 = new My3D(-0.5,  0.5, -3);
   My3D side6_vertex2 = new My3D( 0.5,  0.5, -3);
   My3D side6_vertex3 = new My3D( 0.5, -0.5, -3);
   My3D side6_vertex4 = new My3D(-0.5, -0.5, -3);
   My3D side6_normal_vector = CalculateCrossProduct(side6_vertex1, side6_vertex2, side6_vertex3);





   public MyApp(String name) {
        super(name);
   }
   
   
      
   /****************************************************************************************************
   A perspective projection that maps x and y coordinates
   to the correct place on the projection plane while
   maintaining depth information is achieved by mapping the view frustrum to a cube.
   The cube called "homogenous clip space" is centered at the originin OpenGL
   and extends from -1 to +1 on each of the x, y, and z axes.
   
   The mapping to homogenous clip space is performed using a 4x4 projection
   matrix that, among other actions, places the negative z-coordinate of a
   camera-space point into the w-coordinate of the transformed point.
   Subsequent division by the w-cooridnate produces a 3-dimensional point that lies in clip space.
   
   Let P = <Px, Py, Pz, 1> be a homogenous point in camera-space that lies inside the view frustrum.
   The OpenGL function glFrustrum() takes as parameters the left edge x=l,
   the right edge x=r, the bottom edge y=b, and the top edge y=t of the rectangle
   carved out of the near plane by the four side planes of the vuew frustrum.
   The near plane lies at z=-n, so we can calculate the projected X and y coordinates
   of the point P on the near plane using the equations
   
   x = - (n/Pz)(Px)  and   y = - (n/Pz)(Py)
   
   Any point in lying in the view frustrum satisfies l <= x <= r
   and b <= y <= t on the near plane.  We want to map these ranges to the [-1,1] range
   needed to fit the view frustrum into homogenous clip space.....
   
   Mapping the projected z-coordinate to the range -1 to 1 involves somewhat
   more complex computation. Since the point P lies inside the view frustrum,
   its z-coordinate must satisfy -f <= z <= -n, where n and f are the distances
   from the camera to the near and far planes, respectively.
   We wish to find a function that maps -n --> -1 and -f --> 1.
   (Note that such a mapping reflects the z-axis; therefore
   homogenous clip space is left-handed.)
   
         |  2n/(r-l)      0         (r+l)/(r-l)        0      |
         |                                                    |
         |  0            2n/(t-b)   (t+b)/(t-b)        0      |
    P' = |                                                    | P
         |  0             0         -(f+n)/(f-n)   -2nf/(f-n) | 
		 |                                                    |
		 |  0             0            -1              0      |
		
		
	P' = <-x'Pz, -y'Pz, -z'Pz, -Pz>
	
	divide that by the w-coordinate which is -Pz and you get
	
	<x', y', z'>
		
	This matrix is the OpenGL perspective projection matrix
	generated by the glFrustrum() function.  Camera-space points
	are transformed by this matrix into homogenous clip coordinates
	in such a way that the w-coordinate holds the negation of the original camera-space Z-coordinate.
	
	x' = (2n/(r-l))(-Px/Pz) - ((r+l)/(r-l))
	y' = (2n/(t-b))(-Py/Pz) - ((t+b)/(t-b))
	***************************************************************************************************/
	
	
	
	
	

    /**************************************************************************
	Orthographic Projection
	
	      | 2/(r-l)         0             0              (r+l)/(r-l) |
	      |                                                          |
	      | 0             2/(t-b)         0              (t+b)/(t-b) |
	P' =  |                                                          |  P
	      | 0               0            -2/(f-n)        (f+n)/(f-n) |
		  |                                                          |
		  | 0               0             0               1          |
		  
	this matrix is the OpenGL orthographic projection matrix generated by the 
	glOrtho() function.  Note that the w-coordinate remains 1 after the transformation,
	and thus no perspective projection takes place.
	
	(so if (right) r == +1  and  (left) l == -1  for x-axis-planes
	 and if  (top) t == +1 and (bottom) b == -1  for y-axis planes
	 and if (near) n and (far) f for z-axis planes
	 then the matrix becomes  |  1     0         0             0  |
	                          |  0     1         0             0  |
							  |  0     0    (negative number)  0  |
							  |  0     0         0             1  |
	 
	 which means the x-coord and y-coord do not change
	**************************************************************************/
	
	
   



   //Math.sin and Math.cos take angle in radians
   /***************************************
     cos   -sin   0
     sin   cos    0
     0     0      1
   ****************************************/
   public void Rotate_Around_Z_Axis(My3D vertex, double angle_in_radians) {

        double XX = (Math.cos(angle_in_radians) * vertex.x) - (Math.sin(angle_in_radians) * vertex.y);
        double YY = (Math.sin(angle_in_radians) * vertex.x) + (Math.cos(angle_in_radians) * vertex.y);

        vertex.x = XX;
        vertex.y = YY;
        //vertex.z  is unchanged
   }

   /***************************************
      1     0     0
      0     cos   -sin
      0     sin   cos
   ***************************************/
   public void Rotate_Around_X_Axis(My3D vertex, double angle_in_radians) {

        double YY = (Math.cos(angle_in_radians) * vertex.y) - (Math.sin(angle_in_radians) * vertex.z);
        double ZZ = (Math.sin(angle_in_radians) * vertex.y) + (Math.cos(angle_in_radians) * vertex.z);

        //vertex.x  is unchanged
        vertex.y = YY;
        vertex.z = ZZ;
   }

   /****************************************
      cos    0     sin
      0      1     0
      -sin   0     cos
   ******************************************/
   public void Rotate_Around_Y_Axis(My3D vertex, double angle_in_radians) {

        double XX = (Math.cos(angle_in_radians) * vertex.x) + (Math.sin(angle_in_radians) * vertex.z);
        double ZZ = (Math.cos(angle_in_radians) * vertex.z) - (Math.sin(angle_in_radians) * vertex.x);

        vertex.x  = XX;
        //vertex.y is unchanged
        vertex.z  = ZZ;
   }


   private My3D CalculateCrossProduct(My3D origin, My3D P, My3D Q) {

        //Z positive means visible
        //Z negative means hidden because behind stuff
        //Z 0 means shows as a line or dot

        My3D P_vector = new My3D(P.x - origin.x, P.y - origin.y, P.z - origin.z);
        My3D Q_vector = new My3D(Q.x - origin.x, Q.y - origin.y, Q.z - origin.z);

        double answer_X = (P_vector.y * Q_vector.z) - (P_vector.z * Q_vector.y);
        double answer_Y = (P_vector.z * Q_vector.x) - (P_vector.x * Q_vector.z);
        double answer_Z = (P_vector.x * Q_vector.y) - (P_vector.y * Q_vector.x);

        My3D answer = new My3D(answer_X, answer_Y, answer_Z);
        return answer;
   }

   private double CalculateDotProduct(My3D vec1, My3D vec2) {

        //P dot Q > 0 means P and Q are on the same side of the plane
        //P dot Q < 0 means P and Q are on opposite sides of the plane
        //P dot Q == 0 means P and Q are perpendicular
        //So you take the normal vector of the side do dot product of it with unit-Z vector to see if it
        //is visible to "camera" or not visible

        double answer = (vec1.x * vec2.x) + (vec1.y * vec2.y) + (vec1.z * vec2.z);
        return answer;
   }


   private int  Convert_To_UserCoord_X(double dd) {
	    //for now origin of our java-swing-"viewing plane" is 300,300 in java-swing-user-coords
		//for java-swing-user-coords and camera-space X is positive to the right
	    int answer = (int) ((dd * 100) + 300);
        return answer;		
   }
   
   private int  Convert_To_UserCoord_Y(double dd) {
	    //for now origin of our java-swing-"viewing plane" is 300,300 in java-swing-user-coords
		//but camera-space Y is positive going up
		//but java-swing-user-coords Y is positive going down
		
		int answer = (int) (300 - (dd * 100));
		return answer;
   }


   
   private void DrawObject(Graphics2D g2)
   {
			//Since we want to draw the cube as if it was rotating around its own center 
			//as opposed to rotating around the origin-camera-space even though
			//all the math in this app is cube rotating around origin-camera-space
			//we technically have to translate the cube-vertexes by its center-in-camera-space
			//convert to user-java-coordinate-space
			//draw cube
	        if (side1_normal_vector.z > 0) {
				int [] side1_x = { Convert_To_UserCoord_X(side1_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side1_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side1_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side1_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side1_y = { Convert_To_UserCoord_Y(side1_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side1_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side1_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side1_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side1_x, side1_y, 4);
				g2.setColor(side1_color);
				g2.fillPolygon(side1_x, side1_y, 4);
			}
		
			if (side2_normal_vector.z > 0) {
				int [] side2_x = { Convert_To_UserCoord_X(side2_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side2_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side2_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side2_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side2_y = { Convert_To_UserCoord_Y(side2_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side2_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side2_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side2_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side2_x, side2_y, 4);
				g2.setColor(side2_color);
				g2.fillPolygon(side2_x, side2_y, 4);
			}
		
			if (side3_normal_vector.z > 0) {
				int [] side3_x = { Convert_To_UserCoord_X(side3_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side3_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side3_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side3_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side3_y = { Convert_To_UserCoord_Y(side3_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side3_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side3_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side3_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side3_x, side3_y, 4);
				g2.setColor(side3_color);
				g2.fillPolygon(side3_x, side3_y, 4);
			}
		
			if (side4_normal_vector.z > 0) {
				int [] side4_x = { Convert_To_UserCoord_X(side4_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side4_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side4_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side4_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side4_y = { Convert_To_UserCoord_Y(side4_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side4_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side4_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side4_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side4_x, side4_y, 4);
				g2.setColor(side4_color);
				g2.fillPolygon(side4_x, side4_y, 4);
			}
		
			if (side5_normal_vector.z > 0) {
				int [] side5_x = { Convert_To_UserCoord_X(side5_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side5_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side5_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side5_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side5_y = { Convert_To_UserCoord_Y(side5_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side5_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side5_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side5_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side5_x, side5_y, 4);
				g2.setColor(side5_color);
				g2.fillPolygon(side5_x, side5_y, 4);
			}
		
			if (side6_normal_vector.z > 0) {
				int [] side6_x = { Convert_To_UserCoord_X(side6_vertex1.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side6_vertex2.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side6_vertex3.x - Center_Of_Cube_Point.x), Convert_To_UserCoord_X(side6_vertex4.x - Center_Of_Cube_Point.x) };
				int [] side6_y = { Convert_To_UserCoord_Y(side6_vertex1.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side6_vertex2.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side6_vertex3.y - Center_Of_Cube_Point.y), Convert_To_UserCoord_Y(side6_vertex4.y - Center_Of_Cube_Point.y) };
				g2.setPaint(fg);
				g2.drawPolygon(side6_x, side6_y, 4);
				g2.setColor(side6_color);
				g2.fillPolygon(side6_x, side6_y, 4);
			}
   }//DrawObject


   public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
		Dimension d = getSize();
		g2.clearRect(0,0,d.width,d.height);
		DrawObject(g2);
    }

    public static void main(String s[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        MyApp frame = new MyApp("Please press x or y or for rotation, up-arrow or down-arrow for scaling");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        frame.pack();
        frame.setBackground(bg);
        frame.setForeground(fg);
        frame.setSize(new Dimension(600,600));
        frame.addKeyListener(frame);
        frame.setVisible(true);
    }



    public void keyTyped(KeyEvent e) {
        displayInfo(e, "KEY TYPED: ");
    }
     
    public void keyPressed(KeyEvent e) {
        displayInfo(e, "KEY PRESSED: ");
    }
     
    public void keyReleased(KeyEvent e) {
        displayInfo(e, "KEY RELEASED: ");

        HandleKeyPress(e);
    }
	
	private void RotateCube_X_Axis() {
		  double angle_in_radians = Math.toRadians(15);

		  //Rotate center of cube in camera-space
		  Rotate_Around_X_Axis(Center_Of_Cube_Point, angle_in_radians);


          My3D side1_norm_pt = new My3D(side1_normal_vector.x + side1_vertex1.x,
                                        side1_normal_vector.y + side1_vertex1.y,
                                        side1_normal_vector.z + side1_vertex1.z);
          Rotate_Around_X_Axis(side1_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side1_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side1_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side1_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side1_vertex4, angle_in_radians);
          side1_normal_vector.x = side1_norm_pt.x - side1_vertex1.x;
          side1_normal_vector.y = side1_norm_pt.y - side1_vertex1.y;
          side1_normal_vector.z = side1_norm_pt.z - side1_vertex1.z;
      

          My3D side2_norm_pt = new My3D(side2_normal_vector.x + side2_vertex1.x,
                                        side2_normal_vector.y + side2_vertex1.y,
                                        side2_normal_vector.z + side2_vertex1.z);
          Rotate_Around_X_Axis(side2_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side2_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side2_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side2_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side2_vertex4, angle_in_radians);
          side2_normal_vector.x = side2_norm_pt.x - side2_vertex1.x;
          side2_normal_vector.y = side2_norm_pt.y - side2_vertex1.y;
          side2_normal_vector.z = side2_norm_pt.z - side2_vertex1.z;
      

          My3D side3_norm_pt = new My3D(side3_normal_vector.x + side3_vertex1.x,
                                        side3_normal_vector.y + side3_vertex1.y,
                                        side3_normal_vector.z + side3_vertex1.z);
          Rotate_Around_X_Axis(side3_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side3_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side3_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side3_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side3_vertex4, angle_in_radians);
          side3_normal_vector.x = side3_norm_pt.x - side3_vertex1.x;
          side3_normal_vector.y = side3_norm_pt.y - side3_vertex1.y;
          side3_normal_vector.z = side3_norm_pt.z - side3_vertex1.z;
      

          My3D side4_norm_pt = new My3D(side4_normal_vector.x + side4_vertex1.x,
                                        side4_normal_vector.y + side4_vertex1.y,
                                        side4_normal_vector.z + side4_vertex1.z);
          Rotate_Around_X_Axis(side4_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side4_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side4_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side4_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side4_vertex4, angle_in_radians);
          side4_normal_vector.x = side4_norm_pt.x - side4_vertex1.x;
          side4_normal_vector.y = side4_norm_pt.y - side4_vertex1.y;
          side4_normal_vector.z = side4_norm_pt.z - side4_vertex1.z;
      

          My3D side5_norm_pt = new My3D(side5_normal_vector.x + side5_vertex1.x,
                                        side5_normal_vector.y + side5_vertex1.y,
                                        side5_normal_vector.z + side5_vertex1.z);
          Rotate_Around_X_Axis(side5_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side5_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side5_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side5_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side5_vertex4, angle_in_radians);
          side5_normal_vector.x = side5_norm_pt.x - side5_vertex1.x;
          side5_normal_vector.y = side5_norm_pt.y - side5_vertex1.y;
          side5_normal_vector.z = side5_norm_pt.z - side5_vertex1.z;
      

          My3D side6_norm_pt = new My3D(side6_normal_vector.x + side6_vertex1.x,
                                        side6_normal_vector.y + side6_vertex1.y,
                                        side6_normal_vector.z + side6_vertex1.z);
          Rotate_Around_X_Axis(side6_norm_pt, angle_in_radians);
          Rotate_Around_X_Axis(side6_vertex1, angle_in_radians);
          Rotate_Around_X_Axis(side6_vertex2, angle_in_radians);
          Rotate_Around_X_Axis(side6_vertex3, angle_in_radians);
          Rotate_Around_X_Axis(side6_vertex4, angle_in_radians);
          side6_normal_vector.x = side6_norm_pt.x - side6_vertex1.x;
          side6_normal_vector.y = side6_norm_pt.y - side6_vertex1.y;
          side6_normal_vector.z = side6_norm_pt.z - side6_vertex1.z;
	}//RotateCube_X_Axis
	
	
	private void RotateCube_Y_Axis() {
		  double angle_in_radians = Math.toRadians(15);
		  
		  //Rotate center of cube in camera-space
		  Rotate_Around_Y_Axis(Center_Of_Cube_Point, angle_in_radians);


          My3D side1_norm_pt = new My3D(side1_normal_vector.x + side1_vertex1.x,
                                        side1_normal_vector.y + side1_vertex1.y,
                                        side1_normal_vector.z + side1_vertex1.z);
          Rotate_Around_Y_Axis(side1_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side1_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side1_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side1_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side1_vertex4, angle_in_radians);
          side1_normal_vector.x = side1_norm_pt.x - side1_vertex1.x;
          side1_normal_vector.y = side1_norm_pt.y - side1_vertex1.y;
          side1_normal_vector.z = side1_norm_pt.z - side1_vertex1.z;
      

          My3D side2_norm_pt = new My3D(side2_normal_vector.x + side2_vertex1.x,
                                        side2_normal_vector.y + side2_vertex1.y,
                                        side2_normal_vector.z + side2_vertex1.z);
          Rotate_Around_Y_Axis(side2_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side2_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side2_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side2_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side2_vertex4, angle_in_radians);
          side2_normal_vector.x = side2_norm_pt.x - side2_vertex1.x;
          side2_normal_vector.y = side2_norm_pt.y - side2_vertex1.y;
          side2_normal_vector.z = side2_norm_pt.z - side2_vertex1.z;
      

          My3D side3_norm_pt = new My3D(side3_normal_vector.x + side3_vertex1.x,
                                        side3_normal_vector.y + side3_vertex1.y,
                                        side3_normal_vector.z + side3_vertex1.z);
          Rotate_Around_Y_Axis(side3_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side3_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side3_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side3_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side3_vertex4, angle_in_radians);
          side3_normal_vector.x = side3_norm_pt.x - side3_vertex1.x;
          side3_normal_vector.y = side3_norm_pt.y - side3_vertex1.y;
          side3_normal_vector.z = side3_norm_pt.z - side3_vertex1.z;
      

          My3D side4_norm_pt = new My3D(side4_normal_vector.x + side4_vertex1.x,
                                        side4_normal_vector.y + side4_vertex1.y,
                                        side4_normal_vector.z + side4_vertex1.z);
          Rotate_Around_Y_Axis(side4_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side4_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side4_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side4_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side4_vertex4, angle_in_radians);
          side4_normal_vector.x = side4_norm_pt.x - side4_vertex1.x;
          side4_normal_vector.y = side4_norm_pt.y - side4_vertex1.y;
          side4_normal_vector.z = side4_norm_pt.z - side4_vertex1.z;
      

          My3D side5_norm_pt = new My3D(side5_normal_vector.x + side5_vertex1.x,
                                        side5_normal_vector.y + side5_vertex1.y,
                                        side5_normal_vector.z + side5_vertex1.z);
          Rotate_Around_Y_Axis(side5_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side5_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side5_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side5_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side5_vertex4, angle_in_radians);
          side5_normal_vector.x = side5_norm_pt.x - side5_vertex1.x;
          side5_normal_vector.y = side5_norm_pt.y - side5_vertex1.y;
          side5_normal_vector.z = side5_norm_pt.z - side5_vertex1.z;
      

          My3D side6_norm_pt = new My3D(side6_normal_vector.x + side6_vertex1.x,
                                        side6_normal_vector.y + side6_vertex1.y,
                                        side6_normal_vector.z + side6_vertex1.z);
          Rotate_Around_Y_Axis(side6_norm_pt, angle_in_radians);
          Rotate_Around_Y_Axis(side6_vertex1, angle_in_radians);
          Rotate_Around_Y_Axis(side6_vertex2, angle_in_radians);
          Rotate_Around_Y_Axis(side6_vertex3, angle_in_radians);
          Rotate_Around_Y_Axis(side6_vertex4, angle_in_radians);
          side6_normal_vector.x = side6_norm_pt.x - side6_vertex1.x;
          side6_normal_vector.y = side6_norm_pt.y - side6_vertex1.y;
          side6_normal_vector.z = side6_norm_pt.z - side6_vertex1.z;
		  
	}//RotateCube_Y_Axis
	
	private void RotateCube_Z_Axis() {
		double angle_in_radians = Math.toRadians(15);
		
		  //Rotate center of cube in camera-space
		  Rotate_Around_Z_Axis(Center_Of_Cube_Point, angle_in_radians);

          My3D side1_norm_pt = new My3D(side1_normal_vector.x + side1_vertex1.x,
                                        side1_normal_vector.y + side1_vertex1.y,
                                        side1_normal_vector.z + side1_vertex1.z);
          Rotate_Around_Z_Axis(side1_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side1_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side1_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side1_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side1_vertex4, angle_in_radians);
          side1_normal_vector.x = side1_norm_pt.x - side1_vertex1.x;
          side1_normal_vector.y = side1_norm_pt.y - side1_vertex1.y;
          side1_normal_vector.z = side1_norm_pt.z - side1_vertex1.z;
      

          My3D side2_norm_pt = new My3D(side2_normal_vector.x + side2_vertex1.x,
                                        side2_normal_vector.y + side2_vertex1.y,
                                        side2_normal_vector.z + side2_vertex1.z);
          Rotate_Around_Z_Axis(side2_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side2_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side2_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side2_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side2_vertex4, angle_in_radians);
          side2_normal_vector.x = side2_norm_pt.x - side2_vertex1.x;
          side2_normal_vector.y = side2_norm_pt.y - side2_vertex1.y;
          side2_normal_vector.z = side2_norm_pt.z - side2_vertex1.z;
      

          My3D side3_norm_pt = new My3D(side3_normal_vector.x + side3_vertex1.x,
                                        side3_normal_vector.y + side3_vertex1.y,
                                        side3_normal_vector.z + side3_vertex1.z);
          Rotate_Around_Z_Axis(side3_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side3_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side3_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side3_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side3_vertex4, angle_in_radians);
          side3_normal_vector.x = side3_norm_pt.x - side3_vertex1.x;
          side3_normal_vector.y = side3_norm_pt.y - side3_vertex1.y;
          side3_normal_vector.z = side3_norm_pt.z - side3_vertex1.z;
      

          My3D side4_norm_pt = new My3D(side4_normal_vector.x + side4_vertex1.x,
                                        side4_normal_vector.y + side4_vertex1.y,
                                        side4_normal_vector.z + side4_vertex1.z);
          Rotate_Around_Z_Axis(side4_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side4_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side4_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side4_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side4_vertex4, angle_in_radians);
          side4_normal_vector.x = side4_norm_pt.x - side4_vertex1.x;
          side4_normal_vector.y = side4_norm_pt.y - side4_vertex1.y;
          side4_normal_vector.z = side4_norm_pt.z - side4_vertex1.z;
      

          My3D side5_norm_pt = new My3D(side5_normal_vector.x + side5_vertex1.x,
                                        side5_normal_vector.y + side5_vertex1.y,
                                        side5_normal_vector.z + side5_vertex1.z);
          Rotate_Around_Z_Axis(side5_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side5_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side5_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side5_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side5_vertex4, angle_in_radians);
          side5_normal_vector.x = side5_norm_pt.x - side5_vertex1.x;
          side5_normal_vector.y = side5_norm_pt.y - side5_vertex1.y;
          side5_normal_vector.z = side5_norm_pt.z - side5_vertex1.z;
      

          My3D side6_norm_pt = new My3D(side6_normal_vector.x + side6_vertex1.x,
                                        side6_normal_vector.y + side6_vertex1.y,
                                        side6_normal_vector.z + side6_vertex1.z);
          Rotate_Around_Z_Axis(side6_norm_pt, angle_in_radians);
          Rotate_Around_Z_Axis(side6_vertex1, angle_in_radians);
          Rotate_Around_Z_Axis(side6_vertex2, angle_in_radians);
          Rotate_Around_Z_Axis(side6_vertex3, angle_in_radians);
          Rotate_Around_Z_Axis(side6_vertex4, angle_in_radians);
          side6_normal_vector.x = side6_norm_pt.x - side6_vertex1.x;
          side6_normal_vector.y = side6_norm_pt.y - side6_vertex1.y;
          side6_normal_vector.z = side6_norm_pt.z - side6_vertex1.z;
	}//RotateCube_Z_Axis
	
	
	private void UniformScale(boolean is_uniform_scale_up) {
		
		double CurrentScale = 0.9;
		
		if (is_uniform_scale_up)
			CurrentScale = 1.1;
		
		Center_Of_Cube_Point.x *= CurrentScale;
		Center_Of_Cube_Point.y *= CurrentScale;
		Center_Of_Cube_Point.z *= CurrentScale;
		
		side1_vertex1.x *= CurrentScale;
		side1_vertex1.y *= CurrentScale;
		side1_vertex1.z *= CurrentScale;
		side1_vertex2.x *= CurrentScale;
		side1_vertex2.y *= CurrentScale;
		side1_vertex2.z *= CurrentScale;
		side1_vertex3.x *= CurrentScale;
		side1_vertex3.y *= CurrentScale;
		side1_vertex3.z *= CurrentScale;
		side1_vertex4.x *= CurrentScale;
		side1_vertex4.y *= CurrentScale;
		side1_vertex4.z *= CurrentScale;
		
		
		side2_vertex1.x *= CurrentScale;
		side2_vertex1.y *= CurrentScale;
		side2_vertex1.z *= CurrentScale;
		side2_vertex2.x *= CurrentScale;
		side2_vertex2.y *= CurrentScale;
		side2_vertex2.z *= CurrentScale;
		side2_vertex3.x *= CurrentScale;
		side2_vertex3.y *= CurrentScale;
		side2_vertex3.z *= CurrentScale;
		side2_vertex4.x *= CurrentScale;
		side2_vertex4.y *= CurrentScale;
		side2_vertex4.z *= CurrentScale;
		
		
		side3_vertex1.x *= CurrentScale;
		side3_vertex1.y *= CurrentScale;
		side3_vertex1.z *= CurrentScale;
		side3_vertex2.x *= CurrentScale;
		side3_vertex2.y *= CurrentScale;
		side3_vertex2.z *= CurrentScale;
		side3_vertex3.x *= CurrentScale;
		side3_vertex3.y *= CurrentScale;
		side3_vertex3.z *= CurrentScale;
		side3_vertex4.x *= CurrentScale;
		side3_vertex4.y *= CurrentScale;
		side3_vertex4.z *= CurrentScale;
		
		
		
		side4_vertex1.x *= CurrentScale;
		side4_vertex1.y *= CurrentScale;
		side4_vertex1.z *= CurrentScale;
		side4_vertex2.x *= CurrentScale;
		side4_vertex2.y *= CurrentScale;
		side4_vertex2.z *= CurrentScale;
		side4_vertex3.x *= CurrentScale;
		side4_vertex3.y *= CurrentScale;
		side4_vertex3.z *= CurrentScale;
		side4_vertex4.x *= CurrentScale;
		side4_vertex4.y *= CurrentScale;
		side4_vertex4.z *= CurrentScale;
		
		
		
		side5_vertex1.x *= CurrentScale;
		side5_vertex1.y *= CurrentScale;
		side5_vertex1.z *= CurrentScale;
		side5_vertex2.x *= CurrentScale;
		side5_vertex2.y *= CurrentScale;
		side5_vertex2.z *= CurrentScale;
		side5_vertex3.x *= CurrentScale;
		side5_vertex3.y *= CurrentScale;
		side5_vertex3.z *= CurrentScale;
		side5_vertex4.x *= CurrentScale;
		side5_vertex4.y *= CurrentScale;
		side5_vertex4.z *= CurrentScale;
		
		
		side6_vertex1.x *= CurrentScale;
		side6_vertex1.y *= CurrentScale;
		side6_vertex1.z *= CurrentScale;
		side6_vertex2.x *= CurrentScale;
		side6_vertex2.y *= CurrentScale;
		side6_vertex2.z *= CurrentScale;
		side6_vertex3.x *= CurrentScale;
		side6_vertex3.y *= CurrentScale;
		side6_vertex3.z *= CurrentScale;
		side6_vertex4.x *= CurrentScale;
		side6_vertex4.y *= CurrentScale;
		side6_vertex4.z *= CurrentScale;	
	}//Uniform Scale
	
	

    private void HandleKeyPress(KeyEvent e) {
       int keyCode = e.getKeyCode();
       if (keyCode == 65) { //a
          System.out.println("a pressed");
          Dimension d = getSize();
          System.out.println("width  = " + d.width);
          System.out.println("height = " + d.height);

          side1_normal_vector.DebugPrint("side1_normal_vector = ");
          side2_normal_vector.DebugPrint("side2_normal_vector = ");
          side3_normal_vector.DebugPrint("side3_normal_vector = ");
          side4_normal_vector.DebugPrint("side4_normal_vector = ");
          side5_normal_vector.DebugPrint("side5_normal_vector = ");
          side6_normal_vector.DebugPrint("side6_normal_vector = ");
		  
		  side1_vertex1.DebugPrint("side1_vertex1 = ");
		  side1_vertex2.DebugPrint("side1_vertex2 = ");
		  side1_vertex3.DebugPrint("side1_vertex3 = ");
		  side1_vertex4.DebugPrint("side1_vertex4 = ");
       }
	   else if (keyCode == 38) { //up-arrow
	      UniformScale(true);
		  this.repaint();
	   }
	   else if (keyCode == 40) { //down-arrow
	      UniformScale(false);
		  this.repaint();
	   }
       else if (keyCode == 88) { //x
	      RotateCube_X_Axis();
		  this.repaint();
       }//x
	   else if (keyCode == 89) { //y
	      RotateCube_Y_Axis();
		  this.repaint();
       }//y
	   else if (keyCode == 90) { //z
	      RotateCube_Z_Axis();
		  this.repaint();
       }//z
    }//HandleKeyPress
	
     
    private void displayInfo(KeyEvent e, String keyStatus) {
         
        //You should only rely on the key char if the event
        //is a key typed event.

        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }
         
        int modifiersEx = e.getModifiersEx();
        String modString = "extended modifiers = " + modifiersEx;
        String tmpString = KeyEvent.getModifiersExText(modifiersEx);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no extended modifiers)";
        }
         
        String actionString = "action key? ";
        if (e.isActionKey()) {
            actionString += "YES";
        } else {
            actionString += "NO";
        }
         
        String locationString = "key location: ";
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
            locationString += "standard";
        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
            locationString += "left";
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            locationString += "right";
        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
            locationString += "numpad";
        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
            locationString += "unknown";
        }
         
        System.out.println(keyStatus);
        System.out.println(keyString);
        System.out.println(modString);
        System.out.println(actionString);
        System.out.println(locationString);
        System.out.println();
        System.out.println();
    }
}

