import java.awt.*;
import java.io.IOException;

/**
 * Created by Andre on 02/03/2016.
 */
public class Main {

    public static void main(String[] args) throws IOException{
        // Set up render system and register input callbacks
        //setupGraphics();
        //setupInput();
        Screen screen = new Screen();

        Chip8 myChip8 = new Chip8();
        // Initialize the Chip8 system and load the game into the memory
        myChip8.initialize();

        String gamename = "pong2.C8";

        myChip8.loadGame("C:\\Users\\Andre\\Documents\\C8games\\"+gamename);

        // Emulation loop
        for(;;)
        {
            // Emulate one cycle
            myChip8.emulateCycle();

           // System.out.println("current opcode: "+ Integer.toHexString(myChip8.getOpcode()));
            // If the draw flag is set, update the screen
            if(myChip8.drawFlag())
                drawGraphics(myChip8,screen);

            // Store key press state (Press and Release)
            myChip8.setKeys();
        }
    }

    public static void drawGraphics(Chip8 c8, Screen screen){
        // Draw

        for(int y = 0; y < 32; ++y)
            for(int x = 0; x < 64; ++x)
            {
                if(c8.gfx()[(y*64) + x] == 0){

                    screen.setPos(x,y);
                }
                    //glColor3f(0.0f,0.0f,0.0f);
                else{
                    screen.setPos(x,y);
                }
                    //glColor3f(1.0f,1.0f,1.0f);

                drawPixel(x, y);
            }
    }

    public static void drawPixel(int x, int y){

    }
}
