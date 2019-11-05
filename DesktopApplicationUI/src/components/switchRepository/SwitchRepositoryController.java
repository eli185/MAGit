package components.switchRepository;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.FileUtilities;
import main.GUIUtilities;
import main.MainAppFormController;

public class SwitchRepositoryController {
    @FXML
    private TextField repositoryPathTextField;

    @FXML
    private Button repositoryPathSubmitButton;

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
    void OnRepositoryPathSubmitButtonClick(ActionEvent event) {

        String repositoryFullPath = repositoryPathTextField.getText();
        if (repositoryFullPath.isEmpty() || repositoryFullPath == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty path");
        } else {
            if (FileUtilities.isRepositoryFileAlreadyExists(repositoryFullPath)) {
                if (m_MainAppFormController.getMagitLogic().isTheActiveRepository(repositoryFullPath)) {
                    GUIUtilities.showErrorToUser("This is already the active repository, nothing has changed");
                } else {
                    try {
                        m_MainAppFormController.getMagitLogic().spreadHeadBranchInFileSystemToOurObjects(repositoryFullPath);
                        m_MainAppFormController.createCommitTree();
                        m_MainAppFormController.updateActiveRepositoryLocation(repositoryFullPath);
                        m_MainAppFormController.changeAvailableButtonsStatusIfRepositoryLoaded(false);
                        m_MyStage.close();
                    } catch (Exception e) {
                        GUIUtilities.showErrorToUser(e.toString());
                    }
                }
            } else {
                GUIUtilities.showErrorToUser("This repository does not exist!");
            }
        }
    }
}
