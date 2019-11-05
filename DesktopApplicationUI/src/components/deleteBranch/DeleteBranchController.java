package components.deleteBranch;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;

public class DeleteBranchController {
    @FXML
    private TextField branchNameToDeleteTextField;
    @FXML
    private Button branchNameToDeleteSubmitButton;
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
    void OnBranchNameToDeleteSubmitButtonClick(ActionEvent event) {
        String branchName = branchNameToDeleteTextField.getText();

        if(branchName.isEmpty()){
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        }
        else {
            try {
                if (!m_MainAppFormController.getMagitLogic().isBranchExistsInFileSystem(branchName)) {
                    GUIUtilities.showErrorToUser("This branch does not exist");
                } else if (m_MainAppFormController.getMagitLogic().isHeadBranch(branchName)) {
                    GUIUtilities.showErrorToUser("You can not delete the head branch!");
                } else {
                    m_MainAppFormController.getMagitLogic().deleteBranch(branchName);
                    m_MainAppFormController.createCommitTree();
                    m_MyStage.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
