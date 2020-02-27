package game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import visual.Frame;
import neat.*;

public class Game {

    public static final int PIPE_DELAY = 100;

    private Boolean paused;

    private int pauseDelay;
    private int restartDelay;
    private int pipeDelay;

    public ArrayList<Bird> birds;
    private ArrayList<Pipe> pipes;
    private Keyboard keyboard;

    public int score;
    public Boolean gameover;
    public Boolean started;

    static final int INPUT_SIZE = 4;
    static final int OUTPUT_SIZE = 1;
    static final int BIRD_NUMBER = 1000;
    static final double SCORE_VALUE = 0.2;
    static final int TIME_FRACTION = 1000;
    static final Random rand = new Random();
    Neat neat;
    static Frame frame;
    public int gameGen = 0;
    public long startTime = 0;
    public int currScore = 0;
    public int bestScore = 0;
    public boolean stampa = false;

    public Game() {
        keyboard = Keyboard.getInstance();
        neat = new Neat(INPUT_SIZE, OUTPUT_SIZE, BIRD_NUMBER);
        started = false;

        neat.setCP(0.60);
        neat.setC1(1);
        neat.setC2(1);
        neat.setC3(1);
        neat.setWEIGHT_SHIFT_STRENGTH(0.02);
        neat.setWEIGHT_RANDOM_STRENGTH(0.95);
        neat.setSURVIVORS(0.90);
        neat.setPROBABILITY_MUTATE_LINK(0.1);
        neat.setPROBABILITY_MUTATE_NODE(0.02);
        neat.setPROBABILITY_MUTATE_WEIGHT_SHIFT(0.2);
        neat.setPROBABILITY_MUTATE_WEIGHT_RANDOM(0.01);
        neat.setPROBABILITY_MUTATE_TOGGLE_LINK(0.005);

        this.startTime = 0;

        //neat.init(INPUT_SIZE, OUTPUT_SIZE);

        frame = new Frame(neat.getClient(0).getGenome());
  
        restart();
    }

    public void restart() {
        paused = false;
        gameover = false;
        startTime = System.currentTimeMillis();

        score = 0;
        pauseDelay = 0;
        restartDelay = 0;
        pipeDelay = 0;

        if (gameGen > 0) {

            for (Bird bird : birds) if (bird.score > this.bestScore) this.bestScore = bird.score;

            for (int i = 0; i < BIRD_NUMBER; i++) {
                //neat.getClient(i).setScore(birds.get(i).score * SCORE_VALUE + (birds.get(i).livedTime - (int) this.startTime) / TIME_FRACTION);
                neat.getClient(i).setScore(birds.get(i).livedTime / TIME_FRACTION);
                
                //System.out.println("score " + birds.get(i).score * SCORE_VALUE + birds.get(i).livedTime / TIME_FRACTION);

            }
            neat.evolve();
        }

        birds = new ArrayList<Bird>();
        pipes = new ArrayList<Pipe>();

        for (int i = 0; i < BIRD_NUMBER; i++) {
            birds.add(new Bird(neat.getClient(i)));
        }
        
        
        //stampa = true;
        frame.setGenome(neat.getClient(0).getGenome());
        frame.repaint();
        neat.printSpecies();
        gameGen++;
    }

    synchronized public void update() {
        watchForStart();
        

        if (!started)
            return;

        watchForPause();
        watchForReset();

        if (paused)
            return;


        if (gameover)
            return;

        movePipes();

        boolean updatedScore = false;
        for (Bird bird : birds) {
            bird.update();
            Pipe north = null;// south = null;
            for (Pipe pipe : pipes) {
                if (north == null && pipe.orientation.equals("north")) north = pipe;
                //if (south == null && pipe.orientation.equals("south")) south = pipe;
                if (bird.nextNorthPipe != null && bird.nextNorthPipe.x + bird.nextNorthPipe.width < bird.x /*+ bird.width*/) {
                    bird.setNextNorthPipe(pipe);
                }
            }
            if (bird.nextNorthPipe == null) bird.nextNorthPipe = north;
            //bird.setNextSouthPipe(south);
            
            if (!bird.dead && !updatedScore) {
                this.currScore = bird.score;
                updatedScore = true;
            }
        }
        //if (stampa) for(Pipe pipe : pipes) System.out.println("pipe x:" + pipe.x + " y:" + pipe.y);
        //stampa = false;
        checkForCollisions();
    }

    public ArrayList<Render> getRenders() {
        ArrayList<Render> renders = new ArrayList<Render>();
        renders.add(new Render(0, 0, "lib/background.png"));
        for (Pipe pipe : pipes)
            renders.add(pipe.getRender());
        renders.add(new Render(0, 0, "lib/foreground.png"));
        for (Bird bird : birds) if (!bird.dead) renders.add(bird.getRender());
        return renders;
    }

    private void watchForStart() {
        if (!started && keyboard.isDown(KeyEvent.VK_SPACE)) {
            started = true;
        }
    }

    private void watchForPause() {
        if (pauseDelay > 0)
            pauseDelay--;

        if (keyboard.isDown(KeyEvent.VK_P) && pauseDelay <= 0) {
            paused = !paused;
            pauseDelay = 10;
        }
    }

    private void watchForReset() {
        if (restartDelay > 0)
            restartDelay--;

        if (keyboard.isDown(KeyEvent.VK_R) && restartDelay <= 0) {
            restart();
            restartDelay = 10;
            return;
        }
    }

    private void movePipes() {
        pipeDelay--;

        if (pipeDelay < 0) {
            pipeDelay = PIPE_DELAY;
            Pipe northPipe = null;
            Pipe southPipe = null;

            // Look for pipes off the screen
            for (Pipe pipe : pipes) {
                if (pipe.x - pipe.width < 0) {
                    if (northPipe == null) {
                        northPipe = pipe;
                    } else if (southPipe == null) {
                        southPipe = pipe;
                        break;
                    }
                }
            }

            if (northPipe == null) {
                Pipe pipe = new Pipe("north");
                pipes.add(pipe);
                northPipe = pipe;
            } else {
                northPipe.reset();
            }

            if (southPipe == null) {
                Pipe pipe = new Pipe("south");
                pipes.add(pipe);
                southPipe = pipe;
            } else {
                southPipe.reset();
            }

            northPipe.y = southPipe.y + southPipe.height + 175;
        }

        for (Pipe pipe : pipes) {
            //System.out.println(pipe + "       " + pipes.indexOf(pipe));
            pipe.update();
        }
        
    }

    private void checkForCollisions() {
        boolean endGen = true;
        for (Bird bird : birds) {
            for (Pipe pipe : pipes) {
                    if (pipe.collides(bird.x, bird.y, bird.width, bird.height)) {
                        //gameover = true;
                        bird.dead = true;
                        bird.livedTime = System.currentTimeMillis() - this.startTime;
                    } else if (pipe.x == bird.x && pipe.orientation.equalsIgnoreCase("south")) {
                        score++;
                        bird.score = score;
                    }
                }
            
            // Ground + Bird collision
            if (bird.y + bird.height > App.HEIGHT - 80 || bird.y + bird.height < 0) {
                //gameover = true;
                bird.dead = true;
                bird.livedTime = System.currentTimeMillis() - this.startTime;
                bird.y = App.HEIGHT - 80 - bird.height;
            }
            if (!bird.dead) endGen = false;
        }
        if (endGen) restart();
    }
}
