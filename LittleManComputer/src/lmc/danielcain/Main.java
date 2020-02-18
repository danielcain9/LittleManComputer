package lmc.danielcain;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Main extends Application {

    private Scene scene;
    private Scene secondScene;
    private String inputValue;
    private LittleManComputer lmc;
    private Stage primaryStage;
    private Stage secondaryStage;
    private Thread thread;
    private String selection;
    private ListView uiOutputList;
    private LinkedList<Integer> linkedList;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        primaryStage.setTitle("LMC");
        scene = new Scene(root, 400, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        lmc = new LittleManComputer();
        setUpHomeListView();
        enterPressed();
        run();

        Button homeButton = (Button) secondScene.lookup("#idHome");
        homeButton.setOnMouseClicked(e -> {
            homeClicked();
        });

    }

    /**
     * Display each files function in a list where it can be chosen to be run
     */
    private void setUpHomeListView() {
        ListView uiInputList = (ListView) scene.lookup("#idInputList");
        String inputString = "Input";
        uiInputList.setOnMouseClicked(e -> {
            selection = inputString + (uiInputList.getSelectionModel().getSelectedIndex() + 1);
        });
        int fileCount = new File("../LittleManComputer/data").list().length;
        for (int i = 0; i < fileCount; i++) {
            uiInputList.getItems().add(getFunctionName(inputString + String.valueOf(i + 1)));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Define how the AbstractInstructions operate.
     * Register these instructions to the LMC
     */
    private void run() {
        linkedList = new LinkedList<>();
        // The instruction to add the accumulators value to a value in a specific address
        AbstractInstruction add = new AbstractInstruction(lmc, 100, "ADD") {
            @Override
            public void execute() {
                int index = lmc.mailboxes[lmc.programCounter] % opcode;
                lmc.accumulator = lmc.accumulator + lmc.mailboxes[index];
            }
        };

        // The instruction to minus a value in a specific address from the accumulators value
        AbstractInstruction sub = new AbstractInstruction(lmc, 200, "SUB") {
            @Override
            public void execute() {
                int index = lmc.mailboxes[lmc.programCounter] % opcode;
                lmc.accumulator = lmc.accumulator - lmc.mailboxes[index];
            }
        };

        // The instruction to store the accumulators value in a specific address
        AbstractInstruction sta = new AbstractInstruction(lmc, 300, "STA") {
            @Override
            public void execute() {
                int index = lmc.mailboxes[lmc.programCounter] % opcode;
                lmc.mailboxes[index] = lmc.accumulator;
            }
        };

        // The instruction to load a value in a specific address to the accumulator
        AbstractInstruction lda = new AbstractInstruction(lmc, 500, "LDA") {
            @Override
            public void execute() {

                int index = lmc.mailboxes[lmc.programCounter];
                index = index % 100;
                lmc.accumulator = lmc.mailboxes[index];
            }
        };

        // Instruction to go to a specific instruction unconditionally
        AbstractInstruction bra = new AbstractInstruction(lmc, 600, "BRA") {
            @Override
            public void execute() {
                lmc.programCounter = lmc.mailboxes[lmc.programCounter] % 100 - 1;
            }
        };

        // Instruction to go to a specific instruction if the accumulator value is zero
        AbstractInstruction brz = new AbstractInstruction(lmc, 700, "BRZ") {
            @Override
            public void execute() {
                if (lmc.accumulator == 0) {
                    lmc.programCounter = lmc.mailboxes[lmc.programCounter] % 100 - 1;
                }
            }
        };

        // Instruction to go to a specific instruction if the accumulator value less than or equal to zero
        AbstractInstruction brp = new AbstractInstruction(lmc, 800, "BRP") {
            @Override
            public void execute() {
                if (lmc.accumulator <= 0) {
                    lmc.programCounter = lmc.mailboxes[lmc.programCounter] % 100 - 1;
                }
            }
        };

        // Instruction to receive an input from the user into the accumulator */
        AbstractInstruction inp = new AbstractInstruction(lmc, 901, "INP") {

            @Override
            public void execute() {
                TextField textField = (TextField) secondScene.lookup("#idUserInput");

                Label label = (Label) secondScene.lookup("#idUserPrompt");


                Button valueConfirmedButton = (Button) secondScene.lookup("#idEnterInput");

                valueConfirmedButton.setVisible(true);
                label.setVisible(true);

                valueConfirmedButton.setOnMouseClicked(event -> {
                    inputValue = textField.getText();
                    int numericalValue = Integer.valueOf(inputValue);
                    if (numericalValue < 0 || numericalValue > 99) {
                        label.setText("Invalid entry, Enter value between 0 and 99 ");
                        textField.setText("");
                    } else {
                        thread.interrupt();
                        valueConfirmedButton.setVisible(false);
                        label.setVisible(false);

                    }

                });

                try {
                    thread.sleep(60000);
                    label.setText("Enter number between 0 and 99");
                } catch (InterruptedException e) {

                }

                int inputValue = Integer.parseInt(Main.this.inputValue);

                lmc.accumulator = inputValue;
            }
        };

        // Instruction to display the accumulator's value
        AbstractInstruction out = new AbstractInstruction(lmc, 902, "OUT") {
            @Override
            public void execute() {

                linkedList.addLast(lmc.accumulator);

               if(linkedList.size()>0){
                   Platform.runLater(() -> {
                       uiOutputList = (ListView) secondScene.lookup("#idOutputList");
                       uiOutputList.getItems().add(linkedList.removeFirst());
                   });
               }

            }
        };

        // Instruction to stop the program */
        AbstractInstruction hlt = new AbstractInstruction(lmc, 000, "HLT") {
            @Override
            public void execute() {
                lmc.programCounter = -100;
            }
        };

        // Register all the created instructions, so they can be used
        lmc.registerInstruction(add);
        lmc.registerInstruction(sub);
        lmc.registerInstruction(inp);
        lmc.registerInstruction(out);
        lmc.registerInstruction(sta);
        lmc.registerInstruction(lda);
        lmc.registerInstruction(hlt);
        lmc.registerInstruction(bra);
        lmc.registerInstruction(brp);
        lmc.registerInstruction(brz);

    }


    /* Return filename corresponding to the description shown*/
    private String getFunctionName(String fileName) {
        InputStream is = LittleManComputer.class.getResourceAsStream("/" + fileName + ".csv");  //File holding lmc instructions
        Scanner in = new Scanner(is);
        String[] inputString = in.nextLine().split(",");
        return inputString[0];
    }

    /**
     * On enter pressed, to confirm input selection
     * Set up new scene to show where lmc functioniong is shown
     * On enter clicked:
     * Show this scene when enter clicked
     * Start new thread to handle lmc operations
     */
    public void enterPressed() {
        Button button = (Button) scene.lookup("#idSelectInput");

        Parent operationsPage = null;
        try {
            operationsPage = FXMLLoader.load(getClass().getResource("Main.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        secondScene = new Scene(operationsPage, 400, 275);
        secondaryStage = new Stage();

        button.setOnMouseClicked(event -> {
            secondaryStage.setScene(secondScene);
            secondaryStage.show();
            primaryStage.hide();
            TaskOperator taskOperator = new TaskOperator(lmc, selection);
            thread = new Thread(taskOperator);
            thread.start();
        });


    }


    private void homeClicked() {
        secondaryStage.hide();
        lmc.reset();
        try {
            start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
