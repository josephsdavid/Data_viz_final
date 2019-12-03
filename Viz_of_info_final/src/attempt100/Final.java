package attempt100;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import peasy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Final extends PApplet {




PeasyCam cam;
// flexing polymorphism
localEffects[] e = new localEffects[2];

String path = "tcclean.csv";

public void setup() {
  // peasycam is absolutely amazing
  cam = new PeasyCam(this, width/2, height/2,0,1000);
  cam.setMinimumDistance(500);
  cam.setMaximumDistance(5000);
  
  // lame 
  e[0] = new effectPlot(path);
  e[1] = new effectAxes(path);
}



public void draw() {
  lights();
  // to undo any stupid HSB i did
  colorMode(RGB,255);
  
  ambientLight(0,0,0);

  background(255);
  translate(width/4, height/4);
  rotateX(-.5f);
  for (int i = 0; i < e.length; i++) {
    e[i].display();
  }

}



class effect {
	// building off the midterm, a named series
  String name;
  float[] series;
  int number;
  effect ( String n, float[] s) {
    name = n;
    series = s;
    
    // this is not relevant to what you are grading, was used for failed experiments
        FloatList uniq =  new FloatList();
    uniq.append(s[0]);
    for (int i = 0; i < s.length; i++) {
      if (s[i] != uniq.get(uniq.size()-1)) 
      {
        uniq.append(s[i]);
      }

    }
    // i used this in previous iterations, it is not actually that useful in its current form
    number = uniq.size();
    
  }
  // This is relevant, just returning a regularized series for multiplication by a scale
  public float[] regularize() {
    FloatList res =  new FloatList();
    float mini = min(series);
    float scaler = max(series) - mini;
    for (int i = 0; i < series.length; i ++) {
      float x = (series[i] - mini)/scaler; 
      res.append(x);
    }
    return(res.array());
  }

  // not relevant to grading, never used
  public float range() {
    return(max(series) - min(series)); 
  }
}

class localEffects {
	  effect ale, tonnage, cabins;

	  localEffects(String path) {
	    // read in the csv
	    Table df = loadTable(path, "header, csv");
	    // hardcode in the effects with the dumb floatlist trick I learned in the midterm
	    FloatList al = new FloatList();
	    FloatList ton = new FloatList();
	    FloatList cab = new FloatList();
	    for (TableRow row : df.rows()) {
	      al.append(row.getFloat(".ale"));
	      ton.append(row.getFloat("Tonnage"));
	      cab.append(row.getFloat("cabins"));
	    }
	    // construct the arrays objects
	    ale = new effect("ale", al.array());
	    tonnage = new effect("tonnage", ton.array());
	    cabins = new effect("cabins", cab.array());
	  }


	  // inspired by
	  //https://github.com/OliverColeman/hivis/tree/master/examples/demos
	  // installing hvis feels lazy but i really like the way
	  // they approached a 3d plot
	  // Mine is more of a surface plot, because of the regularize method!
	  public void display() {
	    // this gets that nice heatmappy colors
	    colorMode(HSB, 255);
	    pushMatrix();
	    // stretch the matrix to where i want it
	    scale(width/3 , height/3);
	    // not sure why this is there but it finally worked when i did this
	    translate(0, 0);
	    // it can be the length of any of the effects, as they are all the same
	    for (int i = 0; i < tonnage.series.length; i ++) {
	      // x is tonnage of the ship
	      float x = tonnage.regularize()[i];
	      // y is the number of cabins
	      float y = cabins.regularize()[i];
	      // z is the accumulated local effect. It is generally small except in one area
	      float z = ale.regularize()[i] * 200;
	      // color by effect strength, hot colors are strong interactive effect
	      strokeWeight(1);
	      stroke((z),255,255);
	      pushMatrix();
	      // I love this
	      //translate(x,y,z);
	      // was my original attempt
	      noSmooth();
	      // points get scaled into happy large dots!
	      point(x,y,z);
	      rotateX(0.5f);
	      popMatrix();
	    }  popMatrix();
	  }
	}


// draws the coordinate system and sweet legend
class effectAxes extends localEffects {
	// normal font and big font
	// for best results install iosevka
  private PFont f, b;
  effectAxes (String path) {
	  // contains local effects info
	  super(path);
    f = createFont("Arial", 16);
    b = createFont("Arial", 32);
  }
  
  // draw the axis for tonnage
  public void tonaxes() {
        stroke(255);
    strokeWeight(4);
    pushMatrix();
    translate(width/3 + 20,400);
    line(0, 0, 0, -400, 0, 0);
    textFont(f);
    fill(0);
     text(min(tonnage.series),-400,0, 100);
     text(max(tonnage.series), 0, 0, 100);
     text((max(tonnage.series)+min(tonnage.series))/2, -200, 0, 100);
     textFont(b);
     text(tonnage.name, -250, 100, 100); 
    popMatrix();
  }
  
  // draw the axis for cabins
  public void cabAxes() {
     stroke(255);
     fill(255);
    strokeWeight(4);
    pushMatrix();

    line(-40, 400, 0, -40, -100, 0);
    textFont(f);
    fill(0);
     text(min(cabins.series),-80,350, 100);
     text(max(cabins.series), -80, 0, 100);
     text((max(cabins.series)+min(tonnage.series))/2, -80, 200, 100);
     textFont(b);
     text(cabins.name, -200, 100, 100); 
    popMatrix();
    
  }
  
  // draw the lovely color legend
  public void ALEaxes() {
        colorMode(HSB, 255);
  for (int i = 0; i<tonnage.series.length; i++) {
          float z = ale.regularize()[i] * 200;
                strokeWeight(30);
      stroke((z),255,255);
      point(z+100, -200, 100);
    }
    textFont(f);
    fill(0);
    text(max(ale.series), max(ale.regularize())*200 + 100, -200, 150);
    text(min(ale.series), min(ale.regularize())*200 + 50, -200, 150);
    textFont(b);
    text("Accumulated Local Effect", 0, -250, 150); 
  }
  
  public void display() {
    tonaxes();
    cabAxes();
    ALEaxes();
    
  }
}


// here we draw a nice little plane on top of the local effects
class effectPlot extends localEffects {
  effectPlot (String path) {
    super(path);
  }

  public void xyPlane() {
    pushMatrix();
    //Make this a hl-mesh maybe (that would be sweet)

    translate(width/6, height/6, -30);
    stroke(255);
    box(width, height, -20);
    translate(0, 0, -0.1f);

    popMatrix();
  }

    public void display() {
      super.display();
      colorMode(RGB, 255);
      fill(200);
      xyPlane();
     // annotateAxes();
    }
  }

  public void settings() {  size(1000, 1000, P3D); }
  static public void main(String[] passedArgs) {
		String[] processingArgs = {"Final"};
		Final fin = new Final();
		PApplet.runSketch(processingArgs, fin);
  }
}
