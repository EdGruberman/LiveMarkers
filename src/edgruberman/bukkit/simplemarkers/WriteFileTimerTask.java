package edgruberman.bukkit.simplemarkers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.TimerTask;

import org.json.simple.JSONArray;

import edgruberman.bukkit.messagemanager.MessageLevel;

public final class WriteFileTimerTask extends TimerTask {
    
    private String file;
    
    public WriteFileTimerTask(String file) {
        this.file = file;
    }
    
    public void run() {
        JSONArray json = Main.getJson();
        if (json == null) return;
        
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(this.file)));
            writer.print(json);
            writer.close();
            
        } catch (java.io.IOException e) {
            Main.messageManager.log("Error writing to " + this.file, MessageLevel.SEVERE, e);
        }
    }
}

