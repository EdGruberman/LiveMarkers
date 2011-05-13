package edgruberman.bukkit.simplemarkers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.TimerTask;

import org.json.simple.JSONArray;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class WriteFileTimerTask extends TimerTask {
    
    private final Main main;
    private String file;
    
    public WriteFileTimerTask(Main main, String file) {
        this.main = main;
        this.file = file;
    }
    
    public void run() {
        JSONArray json = main.getJson();
        if (json == null) return;
        
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer.print(json);
            writer.close();
        } catch (java.io.IOException e) {
            Main.messageManager.log(MessageLevel.SEVERE, "Error writing to " + file, e);
        }
    }
}

