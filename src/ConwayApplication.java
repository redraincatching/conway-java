import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;

public class ConwayApplication extends JFrame implements Runnable, MouseListener {
    private static final int SQUARE_SIZE = 20;   // will probably make this mutable depending on window size later
    private static final int BOARD_SIZE = 40;
    private static final Dimension windowSize = new Dimension(800, 800);
    private final BufferStrategy strategy;
    private final boolean[][][] gameState = new boolean[BOARD_SIZE][BOARD_SIZE][2]; // front and back buffers
    private boolean pause = true;
    // button size and stuff for x location comparisons
    private static final int buttonY = 35, startWidth = 30, randWidth = 70, buttonHeight = 20, startX = 35, randX = 80;

    public ConwayApplication() {
        // adding a keyListener
        addMouseListener(this);

        // create and set up the window
        this.setTitle("conway's game of life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // display the window, centred on the screen
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = screensize.width/2 - windowSize.width/2;
        int y = screensize.height/2 - windowSize.height/2;
        setBounds(x, y, windowSize.width, windowSize.height);
        setVisible(true);

        // init buffer
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // start animation thread
        Thread t = new Thread(this);
        t.start();
    }

    // main
    public static void main(String[] args) { ConwayApplication app = new ConwayApplication(); }


    // running
    public void run() {
        randomState();

        do {
            if (!pause) {
                // paint from current buffer 0
                this.repaint();

                // fill back buffer
                fillBackBuffer();

                // swap buffers, so 0 becomes 1
                swapArrays();
            }

            try {
                // busy waiting but i don't really care
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }


    // paint
    public void paint (Graphics g) {
        // redirect drawing calls to offscreen buffer
        g = strategy.getDrawGraphics();

        // draw a black rectangle on the whole canvas
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, windowSize.width, windowSize.height);
        g.setColor(Color.WHITE);

        // note: paint from buffer 0
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                if (gameState[i][j][0]) {
                    // draw a single square, no margin
                    g.fillRect(i*SQUARE_SIZE, j*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        }

        // buttons go here
        g.setColor(Color.GREEN);
        g.fillRect(startX, buttonY, startWidth, buttonHeight);
        g.fillRect(randX, buttonY, randWidth, buttonHeight);
        g.setColor(Color.BLACK);
        g.drawString("Start", startX + 1, buttonY + 15);
        g.drawString("Randomise", randX + 1, buttonY + 15);
        // they're kind of ass but i fuck with it

        // flip buffers
        strategy.show();
    }


    // set random gamestate, possibly add a button for it
    // lmao that was part of the assignment, i just wrote this for testing purposes
    public void randomState() {
        int guess;
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                // honestly this is stupid but i refuse to care
                guess = ((int) (Math.random() * 12345)) % 2;
                gameState[i][j][0] = guess == 1;
            }
        }
        // to show the state
        this.repaint();
    }

    // rule checkers
    public void fillBackBuffer() {
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                gameState[i][j][1] = countSurroundingCells(i, j);
            }
        }
    }

    public boolean countSurroundingCells(int x, int y) {
        // the board wraps around, i've decided, so 0 - 1 = 39 and so on
        int count = 0, xPrime, yPrime;


        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // if not the original cell
                if (!(i == 0 && j == 0)) {
                    // if larger than the board itself, wraps around with mod
                    // and the + BOARD_SIZE makes sure it's never negative, so no index errors
                    xPrime = (x + i + BOARD_SIZE) % BOARD_SIZE;
                    yPrime = (y + j + BOARD_SIZE) % BOARD_SIZE;
                    // count from buffer 1
                    if (gameState[xPrime][yPrime][0]) {
                        count++;
                    }
                }
            }
        }

        // return true if alive and 2 or 3, or if dead and exactly 3
        return (
                (2 <= count && count <= 3 && gameState[x][y][0])
                    ||
                (count == 3 && !gameState[x][y][0])
                );
    }

    // array swap
    public void swapArrays() {
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++) {
                gameState[i][j][0] = gameState[i][j][1];
            }
        }
    }


    // mouseclick handlers
    @Override
    public void mousePressed(MouseEvent e) {

    }
    @Override
    public void mouseReleased(MouseEvent e) {

    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }
    @Override
    public void mouseExited(MouseEvent e) {

    }
    @Override
    public void mouseClicked(MouseEvent e) {
        // gonna be a start/pause and random button
        // not going to matter if paused, actually
        int x = e.getX();
        int y = e.getY();

        if ((x >= startX && x <= startX + startWidth) && (y >= buttonY && y <= buttonY + buttonHeight)) {
            pause = !pause;
        }
        else if ((x >= randX && x <= randX + randWidth) && (y >= buttonY && y <= buttonY + buttonHeight)) {
            randomState();
        }
    }
}
