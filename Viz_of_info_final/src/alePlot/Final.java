package alePlot;

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
// TODO:
// Axes: Units
// color scheme: Ibarra appropriate, one color
public class Final extends PApplet {
	
	boolean instruct = true;
	PFont pf;




	PeasyCam cam;
	// flexing polymorphism
	localEffects[] e = new localEffects[2];
	
	// change to your font but honestly if you dont use iosevka what are you doing
	String font = "Iosevka";
	
	// stored in src
	String path = "tcclean.csv";

	public void setup() {
		// peasycam is absolutely amazing
		pf = createFont(font, 24);
		cam = new PeasyCam(this, width/2, height/2,0,1000);
		cam.setMinimumDistance(500);
		cam.setMaximumDistance(5000);

		// lame 
		e[0] = new effectPlot(path);
		e[1] = new effectAxes(path);
	}
	
	public void mousePressed() {
		instruct = false;
		
		
	}



	public void draw() {
		//lights();
		// to undo any stupid HSB i did
	if (instruct){
		background(255);
		pushMatrix();
		translate(width/2, height/4);
		textFont(pf);
		fill(0);
		textAlign(CENTER);
		text("This is a local effects plot,it shows the average change in prediction \n"
				+ "of a model based on the value of a feature. For more information, \n"
				+ "please visit https://christophm.github.io/interpretable-ml-book/ale.html \n"
				+ "In this visualization, cabins(#) represents the number of cabins on a cruise ship \n"
				+ "and Tonnage(T) represents the weight in tons of the cruise ship. Accumulated Local \n"
				+ "Effects(Δŷ) ('ALE') represents how many more crew members a random forest would predict \n"
				+ "be on that cruise ship, given the product of cabins and tonnage. If an ALE plot is more \n"
				+ "or less flat, that means there is no interaction between the two features, while \n"
				+ "if it is not flat, that indicates interactive effects. This is part of my project \n"
				+ "on Interpretable Machine Learning.\n"
				+ "CLICK TO CONTINUE", 0,0);
		popMatrix();
		textAlign(LEFT);
		
		
	} else {
		colorMode(RGB,255);
		directionalLight(255, 255, 255, 0,0,-1);
		pointLight(100, 100, 100, width ,height,0);


		background(255);
		translate(width/4+100, height/4+100);

		//rotateX(-.5f);
		// polymorphism town (I think)
		for (int i = 0; i < e.length; i++) {
			e[i].display();
		}
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
			// i used this in previous iterations, before i realized i didnt have to do bilinear interpolation
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
			// construct the namedArray effect objects
			ale = new effect("Accumulated Local Effect (Δŷ)", al.array());
			tonnage = new effect("Tonnage (T)", ton.array());
			cabins = new effect("Cabins (#)", cab.array());
		}
		
	


		// inspired by
		//https://github.com/OliverColeman/hivis/tree/master/examples/demos
		// installing hvis feels lazy but i really like the way
		// they approached a 3d plot, especially with the scaling
		public void display() {
			// this gets that nice heatmappy look
			//colorMode(HSB, 255);
			pushMatrix();
			// stretch the matrix to where i want it
			scale(width/3 , height/3);
			// it can be the length of any of the effects, as they are all the same
			for (int i = 0; i < tonnage.series.length; i ++) {
				// x is tonnage of the ship
				float x = tonnage.regularize()[i];
				// y is the number of cabins
				float y = cabins.regularize()[i];
				// z is the accumulated local effect. We multiply by 200 because scaling z is hard
				// for me to wrap my brain around.
				float z = ale.regularize()[i] * 200;
				// color by effect strength, hot colors are strong interactive effect
				noStroke();
				//float inter = map(i/4, z, tonnage.number, 0, 10);
				fill(lerpColor(color(0,0,175), color(255,0,0),z/200));
				pushMatrix();
				// I love this
				translate(x,y,z*2);
				// Dont forget scaling!!!
				box(0.19f, 0.19f, 60f);
				popMatrix();
			}  
			popMatrix();
		}
	}


	// draws the coordinate system and sweet legend
	class effectAxes extends localEffects {
		// normal font and big font
		// for best results install iosevka, otherwise Arial is friendly enough
		private PFont f, b;
		effectAxes (String path) {
			// contains local effects info
			super(path);
			f = createFont(font, 16);
			b = createFont(font, 32);
		}

		// draw the axis for tonnage
		public void tonaxes() {
			stroke(255);
			strokeWeight(4);
			pushMatrix();
			// this is so overcomplicated which is why i didnt do it in the next axes
			translate(width/3 + 20,400);
			line(0, 0, 0, -400, 0, 0);
			textFont(f);
			fill(255);
			// simple labeling of axis
			text(min(tonnage.series),-400,20, 10);
			text(max(tonnage.series), 0, 20, 10);
			text((max(tonnage.series)+min(tonnage.series))/2, -200, 20, 10);
			textFont(b);
			// Cool as hell 3D text
			colorMode(HSB,20);
			for (int i = 0; i < 20; i++) {
				fill(0,i/10,i);
				text(tonnage.name, -250, 100, i); 
			}
			// undo the HSB
			colorMode(RGB, 255);
			popMatrix();
		}

		// draw the axis for cabins
		public void cabAxes() {
			stroke(255);
			fill(255);
			strokeWeight(4);
			// axis sane this time
			line(-40, 400, 0, -40, -100, 0);
			textFont(f);
			fill(255);
			// labels
			text(min(cabins.series),-130,350, 10);
			text(max(cabins.series), -130, 0, 10);
			text((max(cabins.series)+min(tonnage.series))/2, -130, 200, 10);
			textFont(b);
			colorMode(HSB,20);
			// 3D TEXT
			// 3D TEXT
			for (int i = 0; i < 20; i++) {
				fill(0,i/10,i);

				text(cabins.name, -250, 100, i); 
			}
			colorMode(RGB, 255);

		}

		// draw the lovely color legend
		public void ALEaxes() {
			for (int i = 0; i<tonnage.series.length; i++) {
				float z = ale.regularize()[i] * 200;
				strokeWeight(30);
				stroke(lerpColor(color(0,0,175), color(255,0,0),z/200));
				point(z+100, -200, 100);
			}
			
			textFont(f);
			fill(255);
			text(max(ale.series), max(ale.regularize())*200 + 130, -220, 0);
			text(min(ale.series), min(ale.regularize())*200 + 10, -220, 0);
			textFont(b);
			colorMode(HSB,20);
			for (int i = 0; i < 20; i++) {
				fill(0,i/10,i);

				text(ale.name, -50, -280, i); 
			}
			colorMode(RGB,255);
			fill(0);
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
			strokeWeight(1);
			box(width, height, -20);
			translate(0, 0, -0.1f);

			popMatrix();
		}

		public void display() {
			super.display();
			colorMode(RGB, 255);
			fill(100);
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
