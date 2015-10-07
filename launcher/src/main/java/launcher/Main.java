package launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jsonprosessing.RefelctImport;

import java.util.ArrayList;

public class Main extends Application
{

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("/launcher.fxml"));
        primaryStage.setTitle("Launcher");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.setHeight(400);
        primaryStage.setWidth(600);
        primaryStage.setResizable(false);
        primaryStage.show();

        //TODO replace with data from API
        VBox checkBoxPane = (VBox)primaryStage.getScene().lookup("#checkBoxPane");
        ArrayList<CheckBox> checkBoxes = new ArrayList();
        for (int i = 0; i < 100; i++)
        {
            CheckBox cb = new CheckBox(Integer.toString(i));
            cb.setPadding(new Insets(5, 0, 0, 0));
            checkBoxes.add(cb);
        }
        checkBoxPane.getChildren().addAll(checkBoxes);

        RefelctImport ri = new RefelctImport();
        ri.load();
        return;
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
