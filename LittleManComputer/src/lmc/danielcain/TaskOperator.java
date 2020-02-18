package lmc.danielcain;

import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class TaskOperator implements Runnable {

    private final LittleManComputer lmc;
    private final String selection;
    @Override
    public void run() {
        String input = "/" + selection + ".csv";
        try {
            lmc.load(input);
        } catch (CompilerException e) {
            e.printStackTrace();
        }
       lmc.run();
    }

    public TaskOperator(LittleManComputer lmc, String selection) {
        this.lmc = lmc;
        this.selection = selection;
    }




}
