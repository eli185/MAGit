package components.loadRepositoryFromXML;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.Magit;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadRepositoryFromXMLController {
    @FXML
    private Button browseFileButton;
    @FXML
    private Label selectedFileName;
    @FXML
    private Label taskMessageLabel;
    @FXML
    private ProgressBar taskProgressBar;
    @FXML
    private Label progressPercentLabel;

    private final long SLEEP_TIME = 500;
    private SimpleStringProperty m_SelectedFileProperty;
    private SimpleBooleanProperty m_IsFileSelectedProperty;

    private MainAppFormController m_MainAppFormController;
    private Stage m_MyStage;

    public LoadRepositoryFromXMLController() {
        m_SelectedFileProperty = new SimpleStringProperty();
        m_IsFileSelectedProperty = new SimpleBooleanProperty(false);
    }

    public void setMyStage(Stage primaryStage) {
        this.m_MyStage = primaryStage;
    }

    public void setMainController(MainAppFormController i_MainController) {
        this.m_MainAppFormController = i_MainController;
    }

    @FXML
    private void initialize() {
        selectedFileName.textProperty().bind(m_SelectedFileProperty);
    }

    @FXML
    void browseFileButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(m_MyStage);
        if (selectedFile == null) {
            return;
        }

        m_SelectedFileProperty.set(selectedFile.getAbsolutePath());
        LoadRepositoryFromXMLTask task = new LoadRepositoryFromXMLTask(m_MainAppFormController.getMagitLogic(), selectedFile.getAbsolutePath(),
                () -> Platform.runLater(() -> {
                    try {
                        final Magit repoManager = m_MainAppFormController.getMagitLogic();

                        if (repoManager.isRepositoryFileAlreadyExists()) {
                            Optional<ButtonType> result = GUIUtilities.showYesNoAlertToUser("There is already a repository in the current location. Do you want to override? (If not - The repository in this location will be loaded)");
                            if (result.get() == ButtonType.NO) {
                                repoManager.spreadHeadBranchInFileSystemToOurObjects(repoManager.getDataFromXML().getMagitRepository().getLocation());
                            }
                            else{
                                repoManager.deleteRepositoryWithLocationFromXML();
                                repoManager.createRepositoryFromXML();
                            }
                        }
                        else{
                            repoManager.createRepositoryFromXML();
                        }

                        m_MainAppFormController.getMagitLogic().setTaskFlag(false);
                        m_MainAppFormController.updateActiveRepositoryLocation(repoManager.getActiveRepository().getLocation());
                        m_MainAppFormController.changeAvailableButtonsStatusIfRepositoryLoaded(false);
                        m_MainAppFormController.createCommitTree();
                    }
                    catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e){
                        ArrayList<String> errors = new ArrayList<>();
                        errors.add(e.toString());
                        displayErrors(errors);
                    }
                }),

                this::displayErrors);

        bindTaskToUIComponents(task);
        browseFileButton.setDisable(true);
        new Thread(task).start();
    }

    private void bindTaskToUIComponents(Task<Void> i_Task) {
        // task message
        taskMessageLabel.textProperty().bind(i_Task.messageProperty());

        // task progress bar
        taskProgressBar.progressProperty().bind(i_Task.progressProperty());

        // task percent label
        progressPercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(
                                        i_Task.progressProperty(),
                                        100)),
                        " %"));
    }

    private void displayErrors(List<String> i_Errors) {
        Platform.runLater(()-> {
            StringBuilder sb = new StringBuilder("Invalid XML:" +  System.lineSeparator());
            for (String str : i_Errors) {
                sb.append(str + System.lineSeparator());
            }
            GUIUtilities.showErrorToUser(sb.toString());
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ignored) {
            }
            m_MyStage.close();
        });
    }
}