import java.io.IOException;

/**
 * Created by Andre on 02/03/2016.
 */
public class Main {

    public static void main(String[] args) throws IOException{
        // Set up render system and register input callbacks
        //setupGraphics();
        //setupInput();

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

            // If the draw flag is set, update the screen
            if(myChip8.drawFlag())
                drawGraphics();

            // Store key press state (Press and Release)
            myChip8.setKeys();
        }
    }

    public static void drawGraphics(){

    }
}
