import java.awt.*;

import javax.swing.JFrame;


//import com.jogamp.opengl.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL.*;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import java.net.*;
import java.io.*;
import java.nio.FloatBuffer;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


public class Demo implements GLEventListener {

    /**
     * ScreenSaverOGL - this is a simple screen saver that uses JOGL
     * Eric McCreath 2009,2011,2017,2019
     *
     * You need to include the jogl jar files (gluegen2-rt.jar and jogl2.jar). In
     * eclipse use "add external jars" in Project->Properties->Java Build Path->Libraries
     * otherwise make certain they are in the class path.  In the current linux
     * computers there files are in the /usr/share/java directory.
     *
     * If you are executing from the command line then something like:
     *   javac -cp .:/usr/share/java/jogl2.jar:/usr/share/java/gluegen2-rt.jar ScreenSaverOGL.java
     *   java -cp .:/usr/share/java/jogl2.jar:/usr/share/java/gluegen2-rt.jar ScreenSaverOGL
     * should work.
     *
     * You may also need set up the LD_LIBRARY_PATH environment variable. It should point to a
     * directory that contains: libgluegen-rt.so, libjogl_cg.so, libjogl_awt.so,
     * and libjogl.so. In eclipse this can be done in the "Run Configurations.."
     * by adding an environment variable.  In the current linux set up th LD_LIBRARY_PATH
     * does not need to change.
     *
     * I found java 11 to work rather than java 8.
     *
     */

    JFrame jf;
    //GLCanvas canvas;
    //GLCapabilities caps;
    GLJPanel gljpanel;
    Dimension dim = new Dimension(1500, 1000);
    FPSAnimator animator;

    float xpos;
    float xvel;

    URL url = getClass().getResource("/chessimage.png");
    Texture cgtexture;


    // set up the OpenGL Panel within a JFrame
    public Demo() {
        jf = new JFrame();
        gljpanel = new GLJPanel();
        gljpanel.addGLEventListener(this);
        gljpanel.requestFocusInWindow();
        jf.getContentPane().add(gljpanel);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        jf.setPreferredSize(dim);
        jf.pack();
        animator = new FPSAnimator(gljpanel, 20);
        xpos = 100.0f;
        xvel = 1.0f;



        animator.start();

    }

    public static void main(String[] args) {
        new Demo();
    }


    public void display(GLAutoDrawable dr) {
        GL2 gl = (GL2) dr.getGL();
        GLU glu = new GLU();
        GLUT glut = new GLUT();


        // clear the screen
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);


        // Render size and the pixel size.
        int pixel_size = 10;
        int render_size = 30;

        gl.glPushMatrix();


        try {
            cgtexture = TextureIO.newTexture(new File("src/ck_1.png"), true);
            cgtexture.enable(gl);
        } catch (IOException e) {
            System.out.println(e);
        }


        //gl.glTranslatef(400, 300, 0);
        //gl.glTranslated(dim.width/2, dim.height/2, 0);

        //gl.glTranslatef(-200, -200, 0);
        //gl.glScalef(8, 8, 0);
        //texture = t.getTextureObject(gl);

        gl.glTranslated(10,dim.height/2 - cgtexture.getHeight()/2,0);

        int image_x = 10;
        int image_y = dim.height/2 - cgtexture.getHeight()/2;

        drawSquare(gl);

        gl.glTranslated(dim.width/4,0,0);

        int dif = render_size/pixel_size;

        int max_width = cgtexture.getWidth()/pixel_size;
        int max_height = cgtexture.getHeight()/pixel_size;

        Pixel[][] pixel_array = new Pixel[max_width][max_height];

        draw_from_image(gl, pixel_array, pixel_size, max_width, max_height, image_x, image_y, pixel_size/2, pixel_size/2);


        gl.glTranslated(dim.width/4,0,0);

        draw_from_sample(gl,pixel_array,dif,pixel_size);

        gl.glTranslated(dim.width/4,0,0);

        draw_from_image(gl, pixel_array, render_size,
                cgtexture.getWidth()/render_size, cgtexture.getHeight()/render_size,
                image_x, image_y, pixel_size/2, pixel_size/2);

        gl.glPopMatrix();
        gl.glFlush();



        xpos += xvel;
        if (xpos > 360) xpos = 0.0f;
    }

    private void draw_from_image(GL2 gl, Pixel[][] pixel_array, int pixel_size, int max_width,
                                 int max_height, int image_x, int image_y,
                                 int sample_x, int sample_y)
    {
        gl.glPushMatrix();
        for (int i = 0; i < max_width; i ++)
        {
            gl.glPushMatrix();
            for (int j = 0; j < max_height; j ++)
            {
                //float r  = gl.glReadPixels(i + image_x,j + image_y,0,0,0,0);
                //gl.glReadP
                FloatBuffer buffer = FloatBuffer.allocate(4);
                gl.glReadPixels(image_x + (i * pixel_size) + sample_x, image_y + (j*pixel_size) + sample_y, 1, 1, GL.GL_RGBA, GL.GL_FLOAT, buffer);

                float[] pixels = new float[3];
                pixels = buffer.array();
                float r = pixels[0];
                float g = pixels[1];
                float b = pixels[2];

                pixel_array[i][j] = new Pixel(r,g,b);

                drawPixel(gl,r,g,b,pixel_size);
                gl.glTranslated(0,pixel_size,0);
            }
            gl.glPopMatrix();
            gl.glTranslated(pixel_size,0,0);
        }
        gl.glPopMatrix();
    }

    private void draw_from_sample(GL2 gl, Pixel[][] pixel_array, int dif, int pixel_size)
    {
        gl.glPushMatrix();
        for (int i = 0; i < pixel_array.length -(dif/2); i += dif)
        {
            gl.glPushMatrix();
            for (int j = 0; j < pixel_array[i].length -(dif/2); j += dif)
            {
                float r = 0;
                for (int k = 0; k < dif; k++)
                {
                    for(int l = 0; l < dif; l++)
                    {
                        r += pixel_array[i+k][j+l].r;
                    }
                }
                r = r/(dif * dif);
                float g = 0;
                for (int k = 0; k < dif; k++)
                {
                    for(int l = 0; l < dif; l++)
                    {
                        g += pixel_array[i+k][j+l].g;
                    }
                }
                g = g/(dif * dif);
                float b = 0;
                for (int k = 0; k < dif; k++)
                {
                    for(int l = 0; l < dif; l++)
                    {
                        b += pixel_array[i+k][j+l].b;
                    }
                }
                b = b/(dif * dif);

                drawPixel(gl,r,g,b,pixel_size * dif);
                gl.glTranslated(0,pixel_size*dif,0);
            }

            gl.glPopMatrix();
            gl.glTranslated(pixel_size*dif,0,0);
        }
        gl.glPopMatrix();
    }


    private void drawSquare(GL2 gl) {
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_POLYGON);

        gl.glEnable(GL2.GL_TEXTURE_2D);

        cgtexture.bind(gl);

        double width = cgtexture.getWidth();
        double height = cgtexture.getHeight();

        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2d(0.0, 0.0);

        //gl.glTexCoord2d(0.0, 796.0);
        gl.glTexCoord2d(0.0, 1);
        gl.glVertex2d(0.0, height);

        //gl.glTexCoord2d(788.0, 796.0);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(width, height);

        //gl.glTexCoord2d(788.0, 0.0);
        gl.glTexCoord2d(1, 0);
        gl.glVertex2d(width, 0.0);

        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2d(0.0, 0.0);


        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glEnd();
        gl.glPopMatrix();
    }

    private void drawPixel(GL2 gl, float r, float g, float b, int size)
    {
        gl.glColor3f(r,g,b);
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_POLYGON);

        gl.glVertex2d(0.0, 0.0);
        gl.glVertex2d(0.0, size);
        gl.glVertex2d(size, size);
        gl.glVertex2d(size, 0.0);
        gl.glVertex2d(0.0, 0.0);

        gl.glEnd();
        gl.glPopMatrix();
    }


    public void displayChanged(GLAutoDrawable dr, boolean arg1, boolean arg2) {
    }


    // init - set up the opengl context
    public void init(GLAutoDrawable dr) {
        GL2 gl = dr.getGL().getGL2();
        GLU glu = new GLU();
        GLUT glut = new GLUT();
        // make the clear colour black
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        // set the projection matrix to a simple Orthogonal 2D mapping
        gl.glMatrixMode(GL2.GL_PROJECTION);
        glu.gluOrtho2D(0.0, dim.getWidth(), 0.0, dim.getHeight());

        // any transformation that we do from hear on in will be on the model-view matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public void reshape(GLAutoDrawable dr, int arg1, int arg2, int arg3,
                        int arg4) {
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {

    }
}

class Pixel
{
    float r,g,b;
    Pixel(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}