package lmc.danielcain;

import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextField;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class LittleManComputer {
    private ArrayList<Instruction> registeredInstructions = new ArrayList<>();
    private ArrayList<Label> labelList = new ArrayList<>();
    protected int[] mailboxes = new int[100];
    protected int programCounter;
    protected int accumulator;
    protected Instruction currentInstruction;
    protected boolean running;

    public int getProgramCounter() {
        return programCounter;
    }

    public interface Instruction {
        String getMnemonic();

        boolean matches(int mailboxValue);

        void decode(int mailboxValue);

        int getOpCode();

        int getAddress();

        void execute();

    }

    // Registers a new instruction that can then be loaded in a program and executed.
    public void registerInstruction(Instruction instruction) {
        registeredInstructions.add(instruction);
    }

    // Finds an Instruction that matches the mailboxValue. If not found, returns null.
    protected Instruction findInstruction(int mailboxValue) {
        for (Instruction instruction : registeredInstructions) {
            if (instruction.matches(mailboxValue))
                return instruction;
        }

        return null;
    }

    public void load(String filename) throws CompilerException {

        InputStream is = LittleManComputer.class.getResourceAsStream(filename);  //File holding lmc instructions
        Scanner in = new Scanner(is);

        int mailBox = 0;        // Counter to indicate current mailbox
        in.nextLine();
        while (in.hasNext()) {
            String[] inputString = in.nextLine().split(",");          // Delimit file by comma
            String dat = inputString[1];
            if (dat.equals("DAT"))                                          // If mnemonic labelled dat
            {
                Label curLabel = new Label(inputString[0], mailBox);        // Create new label to link to dat
                labelList.add(curLabel);
                int datValue;
                if (inputString[2].equals(null)) {                          // If no value declared, make it zero
                    datValue = 0;
                } else {
                    try {
                        if (Integer.parseInt(inputString[2]) < 0 || Integer.parseInt(inputString[2]) > 99) {
                            throw new CompilerException();
                        }
                        datValue = Integer.parseInt(inputString[2]);
                        mailboxes[mailBox] = datValue;                      //Place DAT value into mailbox
                    } catch (Exception e) {
                        System.out.println("DAT value out of bounds");
                    }
                }
            } else if (inputString[0].length() > 1)                          // Add label if it exists and isn't a dat
            {
                Label curLabel = new Label(inputString[0], mailBox);
                labelList.add(curLabel);
            }
            mailBox++;
        }

        mailBox = 0;

        InputStream is1 = LittleManComputer.class.getResourceAsStream(filename);
        in = new Scanner(is1);
        in.nextLine();
        while (in.hasNext()) {

            String[] inputString = in.nextLine().split(",");
            String addressLabel = inputString[0];
            String mnemonic = inputString[1];


            try {
                for (Instruction instruction : registeredInstructions) {

                    if (instruction.getMnemonic().equals(mnemonic)) {              //If instruction has valid mnemonic
                        int opCode = instruction.getOpCode();
                        mailboxes[mailBox] = opCode;
                    }
                    if (instruction.getMnemonic().equals(addressLabel)) {
                        throw new CompilerException();
                    }
                }
            } catch (Exception e) {
                System.out.println("Label used cannot be a mnemonic OR Address is invalid");
            }

            if (inputString[2].length() > 2) {
                int index = findLabelAddress(inputString[2]);                 // Find address instruction operates on
                mailboxes[mailBox] += index;                                  // Add address onto current operation
            }


            mailBox++;
        }
    }

    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Method to simulate the lmc from instructions which are executed elsewhere
     * For each instruction in the registered instruction, check to see if instruction is INP, OUT or other
     * This is done because the method used to see if a mailbox is associated to a specific
     * instruction, doesn't differentiate between INP and OUT
     * Once confirmed, associated executing method is called
     * Halt/Stop opCode set to -100;
     */
    public void run() {

        programCounter = 0;

        while (programCounter != -100) {
            int mailboxValue = mailboxes[programCounter];      // Value of current mailbox in loop

            for (Instruction instruction : registeredInstructions) {
                if (instruction.matches(mailboxValue)) {
                    if (mailboxes[programCounter] == 901) {
                        if (instruction.getMnemonic().equals("INP")) {
                            instruction.execute();
                        }
                    } else if (mailboxes[programCounter] == 902) {
                        if (instruction.getMnemonic().equals("OUT"))
                            instruction.execute();
                    } else {
                        instruction.execute();
                    }
                }
            }
            if (programCounter != -100)          //Increase program counter to next mailbox if HLT not current
                programCounter++;
        }
    }

    // Reset the mailbox values
    protected void reset() {
        mailboxes = new int[100];
        labelList.clear();
        programCounter = 0;
    }

    /**
     * Return the address in the mailboxes corresponding to associated label
     * If none found, -1 returned
     */
    protected int findLabelAddress(String label) {

        for (Label curLabel : labelList) {
            if (curLabel.getLabel().equals(label)) {
                return curLabel.getAddress();
            }
        }
        return -1;
    }
}
