package launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jsonprosessing.DisplayedModule;
import jsonprosessing.Modules;

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

        Modules modules = new Modules();
        ArrayList<DisplayedModule> displayedModules = modules.load();

        //TODO replace with data from API
        VBox checkBoxPane = (VBox)primaryStage.getScene().lookup("#checkBoxPane");
        ArrayList<CheckBox> checkBoxes = new ArrayList();
        for (DisplayedModule mod : displayedModules)
        {
            checkBoxes.add(mod.getCheckBox());
        }
        checkBoxPane.getChildren().addAll(checkBoxes);
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
