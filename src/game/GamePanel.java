package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import static java.lang.String.format;



public class GamePanel extends JPanel implements Runnable {

    private Game game;

    public GamePanel() {
        game = new Game();
        new Thread(this).start();
    }

    public void update() {
        game.update();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        for (Render r : game.getRenders())
            if (r.transform != null)
                g2D.drawImage(r.image, r.transform, null);
            else
                g.drawImage(r.image, r.x, r.y, null);


        g2D.setColor(Color.BLACK);

        if (!game.started) {
            g2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            g2D.drawString("Press SPACE to start", 150, 240);
        } else {
            g2D.setFont(new Font("TimesRoman", Font.PLAIN, 24));
            g2D.drawString(Integer.toString(game.score), 10, 465);
        }

        if (game.gameover) {
            g2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            g2D.drawString("Press R to restart", 150, 240);
        }

        g2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        
        FontMetrics metrics = g2D.getFontMetrics(g2D.getFont());
        String s1 = format("Generation :%d", game.gameGen);
        String s2 = format("Current score :%d", game.currScore);
        String s3 = format("Best score :%d", game.bestScore);

        //g.drawString(s, this.head().x - (metrics.stringWidth(s) / 2), this.head().y - POINTSIZE * 2);
        g2D.drawString(s1, (metrics.stringWidth(s1) / 2) - 50, metrics.getHeight());
        g2D.drawString(s2, 450 - (metrics.stringWidth(s2)), metrics.getHeight());
        g2D.drawString(s3, 450 - (metrics.stringWidth(s3)), metrics.getHeight() * 2);
        //g2D.drawString("Best score :" + game.bestScore, WIDTH - 10, 20);

        for (Bird bird : game.birds) {
            if (bird.nextNorthPipe != null && !bird.dead) {
                g2D.setColor(Color.red);
                g2D.fillRect(bird.nextNorthPipe.x, bird.nextNorthPipe.y, 10, 10);
                g2D.setColor(Color.blue);
                //g2D.fillRect(bird.nextSouthPipe.x, bird.nextSouthPipe.y, 100, 100);
                g2D.fillRect(bird.nextNorthPipe.x, bird.southPipeY, 10, 10);
                g2D.setColor(Color.black);
            }
        }
    }

    public void run() {
        try {
            while (true) {
                update();
                Thread.sleep(25);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
