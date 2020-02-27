package game;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import neat.Client;
import static game.Game.*;
import static game.App.*;

public class Bird {

    public int x;
    public int y;
    public int width;
    public int height;
    public int score;
    public long livedTime;

    public boolean dead;

    public double yvel;
    public double gravity;

    private int jumpDelay;
    private double rotation;

    private Image image;

    public Client client;
    public Pipe nextNorthPipe;
    public int southPipeY = 0;
    public double previous_output = 0;
    //private Keyboard keyboard;

    public Bird(Client client) {
        x = 100;
        y = 150;
        yvel = 0;
        width = 45;
        height = 32;
        gravity = 0.5;
        jumpDelay = 0;
        rotation = 0.0;
        dead = false;
        livedTime = 0;
        nextNorthPipe = null;
        //nextSouthPipe = null;

        this.client = client;

        //keyboard = Keyboard.getInstance();
    }

    double map(double value, double minA, double maxA, double minB, double maxB) {
        return (1 - ((value - minA) / (maxA - minA))) * minB + ((value - minA) / (maxA - minA)) * maxB;
    }

    double dist(int x1, int y1, int x2, int y2) {
        double x = Math.pow(Math.abs(x1 - x2), 2);
        double y = Math.pow(Math.abs(y1 - y2), 2);
        return Math.sqrt(x + y);
    }

    synchronized public boolean output() {

        double[] inputs = new double[INPUT_SIZE];
        
        
      

        //inputs[0] = map(this.x, 0, WIDTH, -1, 1);
        //inputs[0] = map(this.y, 0, HEIGHT, -1, 1);
        inputs[0] = this.yvel;//map(, -10, 10, -1, 1);
        //inputs[3] = map(this.score, 0, 100, 0, 1);

        if (nextNorthPipe != null) {
            southPipeY = nextNorthPipe.y - 175;
            inputs[1] = dist(nextNorthPipe.x, nextNorthPipe.y, this.x, this.y);//map(nextNorthPipe.x, 0, WIDTH, 0, 1);
            //inputs[2] = ;//map(nextNorthPipe.y, 0, HEIGHT, 0, 1);
            inputs[2] = dist(nextNorthPipe.x, southPipeY, this.x, this.y);//southPipeY;//map(southPipeY, 0, WIDTH, 0, 1);
            //inputs[4] = nextNorthPipe.width;//map(nextNorthPipe.width, 0, WIDTH, 0, 1);
            //System.out.println("pipes");
        } else {
            inputs[1] = 0;
            inputs[2] = 0;
            //inputs[3] = 0;
            //inputs[4] = 0;
            //System.out.println("null");
        }

        inputs[3] = previous_output;

        double[] outputs = this.client.calculate(inputs);

        //outputs[0] = rand.nextDouble();
        //outputs[1] = rand.nextDouble();

        //System.out.println("output 0:" + outputs[0] + " 1:" + outputs[1]);
        //System.out.println("score :");

        //return outputs[0] > outputs[1];
        return outputs[0] > 0.5;
    }

    synchronized public void update() {
        yvel += gravity;

        if (jumpDelay > 0) jumpDelay--;

        if (!dead && output() /*&& jumpDelay <= 0*/) {
            yvel = -10;
            jumpDelay = 10;
        }

        y += (int)yvel;
    }

    public Render getRender() {
        Render r = new Render();
        r.x = x;
        r.y = y;

        if (image == null) {
            image = Util.loadImage("lib/bird.png");     
        }
        r.image = image;

        rotation = (90 * (yvel + 20) / 20) - 90;
        rotation = rotation * Math.PI / 180;

        if (rotation > Math.PI / 2)
            rotation = Math.PI / 2;

        r.transform = new AffineTransform();
        r.transform.translate(x + width / 2, y + height / 2);
        r.transform.rotate(rotation);
        r.transform.translate(-width / 2, -height / 2);

        return r;
    }

	public Pipe getNextNorthPipe() {
		return nextNorthPipe;
	}

	public void setNextNorthPipe(Pipe nextNorthPipe) {
		this.nextNorthPipe = nextNorthPipe;
	}

	/*public Pipe getNextSouthPipe() {
		return nextSouthPipe;
	}

	public void setNextSouthPipe(Pipe nextSouthPipe) {
		this.nextSouthPipe = nextSouthPipe;
	}*/
}
