package components.addNewRepository;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.FileUtilities;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.File;

public class AddNewRepositoryController {
    @FXML
    private TextField fullPathTextField;
    @FXML
    private TextField repositoryNameTextField;
    @FXML
    private Button addNewRepositorySubmitButton;
    private MainAppFormController m_MainAppFormController;
    private Stage m_MyStage;

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setMainController(MainAppFormController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    @FXML
    private void initialize() {
    }

    @FXML
    void OnAddNewRepositorySubmitButtonClick(ActionEvent event) {
        String fullPathFromUser = fullPathTextField.getText();
        String newRepositoryName = repositoryNameTextField.getText();


        if (fullPathFromUser.isEmpty() || fullPathFromUser == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty path");
        } else if (newRepositoryName.isEmpty() || newRepositoryName == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        } else {
            if (!FileUtilities.isFileExists(fullPathFromUser)) {
                File parentFile = new File(fullPathFromUser).getParentFile();
                if(parentFile != null) {
                    String parentFullPath = parentFile.getPath();
                    if (FileUtilities.isFileFolderAndExists(parentFullPath)) {
                        try {
                            m_MainAppFormController.getMagitLogic().addNewRepositoryInFileSystem(fullPathFromUser, newRepositoryName, true);
                            m_MainAppFormController.createCommitTree();
                            m_MyStage.close();
                        } catch (Exception e) {
                            GUIUtilities.showErrorToUser(e.toString());
                        }
                    } else {
                        GUIUtilities.showErrorToUser("The given path does not exist / The given path is not a folder");
                    }
                }
                else{
                    GUIUtilities.showErrorToUser("The given path does not exist / The given path is not a folder");
                }
            }
            else{
                GUIUtilities.showErrorToUser("This path already exists!");
            }
        }
    }
}
