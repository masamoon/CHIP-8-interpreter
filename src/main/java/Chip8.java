import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * Created by Andre on 02/03/2016.
 */
public class Chip8 {

    private short pc;
    private int opcode;
    private short I;
    private short sp;
    private byte[] memory;
    private byte delay_timer;
    private byte sound_timer;
    private short[] stack;
    private byte[] V;
    private short[] gfx;
    private boolean drawFlag;
    private char[] chip8_fontset;
    private char[] key;


    public Chip8(){
        memory = new byte[4096]; //4K memory
        stack = new short[16]; //16 levels of stack
        gfx = new short[2048]; // 64*32 pixels
        chip8_fontset = new char[80];
        key = new char[16];

    }

    public void initialize()
    {

        this.pc     = 0x200;  // Program counter starts at 0x200
        this.opcode = 0;      // Reset current opcode
        this.I      = 0;      // Reset index register
        this.sp     = 0;      // Reset stack pointer
        this.chip8_fontset = new char[]{0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        // Clear display
        // Clear stack
        // Clear registers V0-VF
        // Clear memory

        // Load fontset
        for(int i = 0; i < 80; ++i)
            memory[i] = (byte) chip8_fontset[i];

        // Reset timers
    }

    public void emulateCycle() {
        // Fetch Opcode
        opcode = ((memory[pc] << 8) + memory[pc + 1]);
        //System.out.println("current opcode: "+Integer.toHexString(opcode & 0xffff));
        //System.out.println("current pc: "+pc);
        // Decode Opcode
        switch (opcode & 0xF000) {
            //opcodes//

            case 0x0000:
                switch ((opcode & 0x000F)) {
                    case 0x0000: //0x00E0 : Clears the screen
                        //execute opcode
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        //execute opcode
                        break;
                    default:
                        System.out.printf("Unknown opcode [0x0000]: 0x%X\n", opcode);
                }
                break;

            case 0x1000: // jumps to address NNN
                pc = (short) (opcode & 0x0FFF);
                break;
            case 0x3000: // skips the next instruction if VX equals NN
                if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF))
                    pc += 4;
                else
                    pc += 2;
                break;

            case 0x4000: // skips the next instruction if VX isn't equal to NN
                if (V[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF))
                    pc += 4;
                else
                    pc += 2;
                break;

            case 0x5000: //skips the next instrction if VX equals VY
                if (V[(opcode & 0x0F00) >> 8] == V[(opcode & 0x00F0) >> 4])
                    pc += 4;
                else
                    pc += 2;
                break;

            case 0x6000: //Sets VX to NN.
                V[(opcode & 0x0F00) >> 8] = (byte) (opcode & 0x00FF);
                pc += 2;
                break;

            case 0x7000: //Adds NN to VX.
                V[(opcode & 0x0F00) >> 8] += opcode & 0x00FF;
                pc += 2;
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x8XY0: Sets VX to the value of VY
                        V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0001: // 0x8XY1: Sets VX to "VX OR VY"
                        V[(opcode & 0x0F00) >> 8] |= V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0002: // 0x8XY2: Sets VX to "VX AND VY"
                        V[(opcode & 0x0F00) >> 8] &= V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0003: // 0x8XY3: Sets VX to "VX XOR VY"
                        V[(opcode & 0x0F00) >> 8] ^= V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0004: // 0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't
                        if (V[(opcode & 0x00F0) >> 4] > (0xFF - V[(opcode & 0x0F00) >> 8]))
                            V[0xF] = 1; //carry
                        else
                            V[0xF] = 0;
                        V[(opcode & 0x0F00) >> 8] += V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0005: // 0x8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't
                        if (V[(opcode & 0x00F0) >> 4] > V[(opcode & 0x0F00) >> 8])
                            V[0xF] = 0; // there is a borrow
                        else
                            V[0xF] = 1;
                        V[(opcode & 0x0F00) >> 8] -= V[(opcode & 0x00F0) >> 4];
                        pc += 2;
                        break;

                    case 0x0006: // 0x8XY6: Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift
                        V[0xF] = (byte) (V[(opcode & 0x0F00) >> 8] & 0x1);
                        V[(opcode & 0x0F00) >> 8] >>= 1;
                        pc += 2;
                        break;

                    case 0x0007: // 0x8XY7: Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't
                        if (V[(opcode & 0x0F00) >> 8] > V[(opcode & 0x00F0) >> 4])    // VY-VX
                            V[0xF] = 0; // there is a borrow
                        else
                            V[0xF] = 1;
                        V[(opcode & 0x0F00) >> 8] =(byte)( V[(opcode & 0x00F0) >> 4] - V[(opcode & 0x0F00) >> 8]);
                        pc += 2;
                        break;

                    case 0x000E: // 0x8XYE: Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift
                        V[0xF] = (byte)(V[(opcode & 0x0F00) >> 8] >> 7);
                        V[(opcode & 0x0F00) >> 8] <<= 1;
                        pc += 2;
                        break;

                    default:
                        System.out.printf("Unknown opcode [0x8000]: 0x%X\n", opcode);
                }
                break;

            case 0x9000: // Skips the next instruction if VX doesn't equal VY.

                if (V[(opcode & 0x0F00) >> 8] != V[(opcode & 0x00F0) >> 4])
                    pc += 4;
                else
                    pc += 2;
                break;


            case 0xA000: //ANNN: Setts I to the address NNN
                I = (byte) (opcode & 0x0FFF);
                pc += 2;
                break;

            case 0xB000: //Jumps to the address NNN plus V0.
                pc = (short) ((opcode & 0xFFF) + V[0]);

                break;
            case 0xC000: // CXNN: Sets VX to a random number and NN

                Random rand = new Random();
                int r = rand.nextInt(255);
                V[(opcode & 0x0F00) >> 8] = (byte)((r % 0xFF) & (opcode & 0x00FF));
                pc += 2;
                break;

            case 0x2000:
                stack[sp] = pc;
                ++sp;
                pc = (short) (opcode & 0x0FFF);
                break;

            case 0x0004:
                if (V[(opcode & 0x00F0) >> 4] > (0xFF - V[(opcode & 0x0F00) >> 8]))
                    V[0xF] = 1; //carry
                else
                    V[0xF] = 0;
                V[(opcode & 0x0F00) >> 8] += V[(opcode & 0x00F0) >> 4];
                pc += 2;
                break;

            case 0x0033:
                memory[I] = (byte)(V[(opcode & 0x0F00) >> 8] / 100);
                memory[I + 1] = (byte)((V[(opcode & 0x0F00) >> 8] / 10) % 10);
                memory[I + 2] = (byte)((V[(opcode & 0x0F00) >> 8] % 100) % 10);
                pc += 2;
                break;

            case 0xD000:

                short x = (short) V[(opcode & 0x0F00) >> 8];
                short y = (short) V[(opcode & 0x00F0) >> 4];
                short height = (short) (opcode & 0x000F);
                short pixel;

                V[0xF] = 0;
                for (int yline = 0; yline < height; yline++) {
                    pixel = (short) memory[I + yline];
                    for (int xline = 0; xline < 8; xline++) {
                        if ((pixel & (0x80 >> xline)) != 0) {
                            if (gfx[(x + xline + ((y + yline) * 64))] == 1)
                                V[0xF] = 1;
                            gfx[x + xline + ((y + yline) * 64)] ^= 1;
                        }
                    }
                }

                drawFlag = true;
                pc += 2;

                break;

            case 0xE000:
                switch(opcode & 0x00FF)
                {
                    case 0x009E: // EX9E: Skips the next instruction if the key stored in VX is pressed
                        if(key[V[(opcode & 0x0F00) >> 8]] != 0)
                            pc += 4;
                        else
                            pc += 2;
                        break;

                    case 0x00A1: // EXA1: Skips the next instruction if the key stored in VX isn't pressed
                        if(key[V[(opcode & 0x0F00) >> 8]] == 0)
                            pc += 4;
                        else
                            pc += 2;
                        break;

                    default:
                        System.out.printf ("Unknown opcode [0xE000]: 0x%X\n", opcode);
                }
                break;

            case 0xF000:
                        switch (opcode & 0x00FF) {
                            case 0x0007: // FX07: Sets VX to the value of the delay timer
                                V[(opcode & 0x0F00) >> 8] = delay_timer;
                                pc += 2;
                                break;

                            case 0x000A: // FX0A: A key press is awaited, and then stored in VX
                            {
                                boolean keyPress = false;

                                for (int i = 0; i < 16; ++i) {
                                    if (key[i] != 0) {
                                        V[(opcode & 0x0F00) >> 8] = (byte)i;
                                        keyPress = true;
                                    }
                                }

                                // If we didn't received a keypress, skip this cycle and try again.
                                if (!keyPress)
                                    return;

                                pc += 2;
                                break;
                            }


                            case 0x0015: // FX15: Sets the delay timer to VX
                                delay_timer = (byte) V[(opcode & 0x0F00) >> 8];
                                pc += 2;
                                break;

                            case 0x0018: // FX18: Sets the sound timer to VX
                                sound_timer = (byte) V[(opcode & 0x0F00) >> 8];
                                pc += 2;
                                break;

                            case 0x001E: // FX1E: Adds VX to I
                                if (I + V[(opcode & 0x0F00) >> 8] > 0xFFF)    // VF is set to 1 when range overflow (I+VX>0xFFF), and 0 when there isn't.
                                    V[0xF] = 1;
                                else
                                    V[0xF] = 0;
                                I += V[(opcode & 0x0F00) >> 8];
                                pc += 2;
                                break;

                            case 0x0029: // FX29: Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font
                                I = (byte)(V[(opcode & 0x0F00) >> 8] * 0x5);
                                pc += 2;
                                break;

                            case 0x0033: // FX33: Stores the Binary-coded decimal representation of VX at the addresses I, I plus 1, and I plus 2
                                memory[I] = (byte)(V[(opcode & 0x0F00) >> 8] / 100);
                                memory[I + 1] = (byte)((V[(opcode & 0x0F00) >> 8] / 10) % 10);
                                memory[I + 2] = (byte)((V[(opcode & 0x0F00) >> 8] % 100) % 10);
                                pc += 2;
                                break;

                            case 0x0055: // FX55: Stores V0 to VX in memory starting at address I
                                for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i)
                                    memory[I + i] = V[i];

                                // On the original interpreter, when the operation is done, I = I + X + 1.
                                I += ((opcode & 0x0F00) >> 8) + 1;
                                pc += 2;
                                break;

                            case 0x0065: // FX65: Fills V0 to VX with values from memory starting at address I
                                for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i)
                                    V[i] = memory[I + i];

                                // On the original interpreter, when the operation is done, I = I + X + 1.
                                I += ((opcode & 0x0F00) >> 8) + 1;
                                pc += 2;
                                break;
                            default:
                                System.out.printf("Unknown opcode: 0x%X\n", opcode);
                        }

                        // Update timers
                        if (delay_timer > 0)
                            --delay_timer;

                        if (sound_timer > 0) {
                            if (sound_timer == 1)
                                System.out.printf("BEEP\n");
                            --sound_timer;
                        }
                }
        }




    public void setI(String setto){
        I = (byte)(opcode & 0x0FFF);
    }

    public static void copy(InputStream input,
                            OutputStream output,
                            int bufferSize)
            throws IOException {
        byte[] buf = new byte[bufferSize];
        int bytesRead = input.read(buf);
        while (bytesRead != -1) {
            output.write(buf, 0, bytesRead);
            bytesRead = input.read(buf);
        }
        output.flush();
    }

    public void loadGame(String game ) throws FileNotFoundException, IOException{

        File f = new File(game);

        FileInputStream fin = new FileInputStream(f);


        long buffer_size_long = f.length();
        int buffer_size = (int) buffer_size_long;

        byte[] byte_buffer = new byte[buffer_size];

       /* fin.read(byte_buffer);
        fin.close();
        byte[] encoded_bytes = Base64.getEncoder().encode(byte_buffer);*/


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(new FileInputStream(f), baos, buffer_size);
        byte_buffer = baos.toByteArray();


        //for(byte b: byte_buffer){
        String encoded_hex = HexBin.encode(byte_buffer);
        int len = encoded_hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(encoded_hex.charAt(i), 16) << 4)
                    + Character.digit(encoded_hex.charAt(i+1), 16));
        }

        if((4096-512) > buffer_size) {
            for (int i = 0; i < buffer_size; i++) {

                memory[512 + i] = data[i];
            }
        }
        else{
            System.out.println("ROM file to big for memory");
            System.exit(1);
        }

        System.out.println("buffer dump");

        for(byte b: data){
            System.out.println(Integer.toHexString(b));
        }


       // }
    }

    public boolean drawFlag(){
        return this.drawFlag;
    }

    public int getOpcode(){
        return this.opcode;
    }

    public void setKeys(){

    }

    public short[] gfx(){
        return this.gfx;
    }

}


