package components.resetHeadBranch;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.IOException;
import java.util.Optional;

public class ResetHeadBranchController {
    private final static int SHA1_LENGTH = 40;
    @FXML
    private TextField newPointedCommitSha1TextField;
    @FXML
    private Button newPointedCommitSha1SubmitButton;
    private MainAppFormController m_MainAppFormController;
    private Stage m_MyStage;

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setMainController(MainAppFormController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    @FXML
    void OnNewPointedCommitSha1SubmitButtonClick(ActionEvent event) {
        String newPointedCommitSha1 = newPointedCommitSha1TextField.getText();
        if (newPointedCommitSha1.isEmpty() || newPointedCommitSha1 == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty SHA-1");
        } else if (newPointedCommitSha1.length() != SHA1_LENGTH) {
            GUIUtilities.showErrorToUser("Invalid input! SHA-1 must include 40 Hexa characters");
        } else if (!m_MainAppFormController.getMagitLogic().isSha1ExistsInFileSystem(newPointedCommitSha1)) {
            GUIUtilities.showErrorToUser("This SHA-1 does not exist!");
        } else if (!m_MainAppFormController.getMagitLogic().isSha1OfReachableCommit(newPointedCommitSha1)) {
            GUIUtilities.showErrorToUser("This is not a SHA-1 of reachable commit!");
        } else if (m_MainAppFormController.getMagitLogic().isSha1OfPointedCommitOfHeadBranch(newPointedCommitSha1)) {
            GUIUtilities.showErrorToUser("This is already the pointed commit SHA-1!");
        } else {
            try {
                if (!m_MainAppFormController.getMagitLogic().isWCClean()) {
                    Optional<ButtonType> result = GUIUtilities.showYesNoAlertToUser("There are open changes in the working copy. Do you want to override?");

                    if (result.get() == ButtonType.YES){
                        m_MainAppFormController.getMagitLogic().resetHeadBranch(newPointedCommitSha1);
                        m_MainAppFormController.createCommitTree();
                        m_MyStage.close();
                    } else {
                        m_MyStage.close();
                    }
                } else {
                    try {
                        m_MainAppFormController.getMagitLogic().resetHeadBranch(newPointedCommitSha1);
                        m_MainAppFormController.createCommitTree();
                        m_MyStage.close();
                    } catch (IOException e) {
                        GUIUtilities.showErrorToUser(e.toString());
                    }
                }
            } catch (IOException e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }
}