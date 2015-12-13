package launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jsonprosessing.DisplayedModule;
import jsonprosessing.Modules;

import java.util.ArrayList;


public class Main extends Application
{
    private static final int CHECKBOXES_PER_ROW = 3;

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

        Modules modules = new Modules();
        ArrayList<DisplayedModule> displayedModules = modules.load();

        //TODO replace with data from API
        GridPane checkBoxPane = (GridPane)primaryStage.getScene().lookup("#checkBoxPane");

        ArrayList<CheckBox> checkBoxes = new ArrayList();
        for (int i = 0; i < displayedModules.size(); i++)
        {
            //TODO have grid with wit to max content length --> check if "hardcoded" or nicer way (auto grow)
            displayedModules.get(i).getCheckBox().setPadding(new Insets(5,0,0,0));
            checkBoxPane.add(displayedModules.get(i).getCheckBox(), i % CHECKBOXES_PER_ROW, i / CHECKBOXES_PER_ROW);
        }
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
