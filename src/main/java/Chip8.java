/**
 * Created by Andre on 02/03/2016.
 */
public class Chip8 {

    private short pc;
    private int opcode;
    private int I;
    private int sp;
    private int[] memory;
    private char delay_timer;
    private char sound_timer;
    private short[] stack;
    private int[] V;
    private short[] gfx;
    private boolean drawFlag;
    private char[] chip8_fontset;
    private char[] key;


    public Chip8(){
        memory = new int[4096]; //4K memory
        stack = new short[16]; //16 levels of stack
        gfx = new short[2048]; // 64*32 pixels
        chip8_fontset = new char[80];
        key = new char[16];

    }

    public void initialize()
    {

        pc     = 0x200;  // Program counter starts at 0x200
        opcode = 0;      // Reset current opcode
        I      = 0;      // Reset index register
        sp     = 0;      // Reset stack pointer
        chip8_fontset = new char[]{0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
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
            memory[i] = chip8_fontset[i];

        // Reset timers
    }

    public void emulateCycle() {
        // Fetch Opcode
        opcode = (memory[pc] << 8) | memory[pc + 1];
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
                pc = (short)(opcode & 0x0FFF);
                break;
            case 0x3000: // skips the next instruction if VX equals NN
            case 0xA000: //ANNN: Setts I to the address NNN
                I = opcode & 0x0FFF;
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
                memory[I] = V[(opcode & 0x0F00) >> 8] / 100;
                memory[I + 1] = (V[(opcode & 0x0F00) >> 8] / 10) % 10;
                memory[I + 2] = (V[(opcode & 0x0F00) >> 8] % 100) % 10;
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
                switch (opcode & 0x00FF) {
                    // EX9E: Skips the next instruction
                    // if the key stored in VX is pressed
                    case 0x009E:
                        if (key[V[(opcode & 0x0F00) >> 8]] != 0)
                            pc += 4;
                        else
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

    public void decodeOpcode(Integer opcode){
        if(opcode.toString().startsWith("A")){
            setI(opcode.toString().substring(1));
        }
    }

    public void setI(String setto){
        I = opcode & 0x0FFF;
    }

    public void loadGame(String game ){

    }

    public boolean drawFlag(){
        return this.drawFlag;
    }

    public void setKeys(){

    }

}


