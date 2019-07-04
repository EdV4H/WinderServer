package WinderServer.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MainController {

    private Stage stage;
    private int member_count;

    @FXML
    private MenuItem menu_close;

    @FXML
    private MenuItem menu_about;

    @FXML
    private ListView<String> list_user;

    @FXML
    private TextArea entry_log;

    @FXML
    private Button button_new_dummy;

    @FXML
    private Button button_chat;

    @FXML
    private void DummyCreate (MouseEvent e) {
        Registration("Dummy", "1990/01/01", "dummy comment");
    }

    @FXML
    private void DummyNewChat (MouseEvent e) {
        Random rand = new Random();
        String str = "NEWCHAT " + String.valueOf(rand.nextInt(100000)) + " Dummy";
        MainFrame.server.SendAll(str);
    }

    @FXML
    void initialize () {
        PrintLog("[MainController] initializing...");
        int current_id = 1;
        while (true) {
            try{
                File file = new File("dat/userlist/" + String.valueOf(current_id) + ".user");
                BufferedReader br = new BufferedReader(new FileReader(file));
    
                String name = br.readLine();
                String birth = br.readLine();
                String comment = br.readLine();

                list_user.getItems().add(name + " " + birth + " " + comment);
              
                br.close();
                
            }catch(FileNotFoundException e){
                PrintLog("[MainController] ID : " + String.valueOf(current_id) + " is not exist.");
                break;
            }catch(IOException e){
                System.out.println(e);
            }
            current_id++;
        }
        member_count = current_id-1;
    }

    public void PrintLog (String msg) {
        entry_log.setText(msg + "\n" + entry_log.getText());
    }

    public int Registration (String name, String birth, String comment) {
        try {
            PrintLog("[MainController] Registration (User ID : "+(member_count+1)+")");
            File file = new File("dat/userlist/" + String.valueOf(member_count+1) + ".user");
            file.createNewFile();

            File resv = new File("dat/reservation/" + String.valueOf(member_count+1) + ".resv");
            resv.createNewFile();

            File like = new File("dat/likelist/" + String.valueOf(member_count+1) + ".like");
            like.createNewFile();

            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
          
            pw.println(name);
            pw.println(birth);
            pw.println(comment);

            pw.close();
            
            list_user.getItems().add(name + " " + birth + " " + comment);
            member_count++;
            PrintLog("[MainController] Register completed.");
            return member_count;
        }catch(FileNotFoundException e){
            e.printStackTrace();
            return -1;
        }catch(IOException e){
            System.out.println(e);
            return -1;
        }
    }

    public int getMember() {
        return member_count;
    }

    public void setStage (Stage stage) {this.stage = stage;}
}
