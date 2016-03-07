import javax.swing.*;
import java.awt.*;

/**
 * Created by Andre on 03/03/2016.
 */
public class Screen extends JFrame {
    int posx;
    int posy;

    public Screen() {
        this.setPreferredSize(new Dimension(400, 400));
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.posx = 0;
        this.posy = 0;
    }

    public void setPos(int x, int y){
        this.posx = x;
        this.posy = y;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // define the position
        int locX = posx;
        int locY = posy;

        // draw a line (there is no drawPoint..)
        g.drawLine(locX, locY, locX, locY);
    }



    public static void main(String[] args){
        Screen screen = new Screen();
    }

}
