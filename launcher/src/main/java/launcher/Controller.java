package launcher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller
{


    @FXML
    private Button openFileChooserButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button backButton;
    @FXML
    private Label instructionLabel;
    @FXML
    private ScrollPane modulesPane;
    @FXML
    private TextField filePathInput;

    public void openFilechooser(ActionEvent event)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        filePathInput.setText(directoryChooser.showDialog(getPrimaryStageFromActionEvent(event)).getAbsolutePath());
    }

    public void continueToFileChooser()
    {
        openFileChooserButton.setDisable(false);
        backButton.setDisable(false);
        modulesPane.setDisable(true);
        filePathInput.setDisable(false);
        instructionLabel.setText("Please choose the location for the installation.");
        nextButton.setOnAction(e -> continueToInstallation());
    }

    public void backToModuleSelection()
    {
        openFileChooserButton.setDisable(true);
        backButton.setDisable(true);
        modulesPane.setDisable(false);
        filePathInput.setDisable(true);
        instructionLabel.setText("Please choose which modules you want to install.");
        nextButton.setOnAction(e -> continueToFileChooser());
    }

    public void continueToInstallation()
    {
        System.out.println("Lorem ipsum");
    }

    private Stage getPrimaryStageFromActionEvent(ActionEvent event)
    {
        return (Stage)((Node)event.getSource()).getScene().getWindow();
    }
}
