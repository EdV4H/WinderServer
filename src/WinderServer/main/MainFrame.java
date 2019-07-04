package WinderServer.main;

import WinderServer.server.ServerManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFrame extends Application {
    private Stage stage;

    public static MainController mainController;
    public static ServerManager server;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("Winder Server");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/MainFrame.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

        server = new ServerManager(100);
        server.start();
    }
}