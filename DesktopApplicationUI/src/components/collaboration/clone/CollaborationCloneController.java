package components.collaboration.clone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.FileUtilities;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.File;

public class CollaborationCloneController {

    @FXML
    private TextField remoteRepositoryFullPathTextField;
    @FXML
    private TextField localRepositoryFullPathTextField;
    @FXML
    private Button cloneSubmitButton;
    @FXML
    private TextField localRepositoryNameTextField;
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
    void OnCloneSubmitButtonClick(ActionEvent event) {
        String remoteRepositoryFullPath = remoteRepositoryFullPathTextField.getText();
        String localRepositoryFullPath = localRepositoryFullPathTextField.getText();
        String localRepositoryName = localRepositoryNameTextField.getText();


        if (remoteRepositoryFullPath.isEmpty() || remoteRepositoryFullPath == null || localRepositoryFullPath.isEmpty() || localRepositoryFullPath == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty path");
        } else if (localRepositoryName.isEmpty() || localRepositoryName == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        } else if (!FileUtilities.isRepositoryFileAlreadyExists(remoteRepositoryFullPath)){
            GUIUtilities.showErrorToUser("The remote repository does not exist!");
        } else {
                if (!FileUtilities.isFileExists(localRepositoryFullPath)) {
                    File parentFile = new File(localRepositoryFullPath).getParentFile();
                    if(parentFile != null) {
                        String parentFullPath = parentFile.getPath();
                        if (FileUtilities.isFileFolderAndExists(parentFullPath)) {
                            try {
                                m_MainAppFormController.getMagitLogic().cloneRepository(localRepositoryFullPath, remoteRepositoryFullPath, localRepositoryName);
                                m_MainAppFormController.updateActiveRepositoryLocation(localRepositoryFullPath);
                                m_MainAppFormController.createCommitTree();
                                m_MainAppFormController.changeAvailableButtonsStatusIfRepositoryLoaded(false);
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
