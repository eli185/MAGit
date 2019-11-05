package components.createNewBranch;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import logic.Branch;
import main.GUIUtilities;
import main.MainAppFormController;
import java.util.Optional;

public class CreateNewBranchController {
    private final static int SHA1_LENGTH = 40;
    @FXML
    private TextField newBranchNameTextField;
    @FXML
    private TextField newBranchPointedCommitSha1TextField;
    @FXML
    private Button newBranchSubmitButton;
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
    void OnNewBranchSubmitButtonClick(ActionEvent event) {
        String branchName = newBranchNameTextField.getText();
        String pointedCommitSha1 = newBranchPointedCommitSha1TextField.getText();

        if (branchName.isEmpty() || branchName == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        } else if (pointedCommitSha1.isEmpty() || pointedCommitSha1 == null) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty commit SHA-1");
        } else if (m_MainAppFormController.getMagitLogic().isBranchExistsInFileSystem(branchName)) {
            GUIUtilities.showErrorToUser("This branch already exists");
        } else if (pointedCommitSha1.length() != SHA1_LENGTH) {
            GUIUtilities.showErrorToUser("Invalid input! SHA-1 must include 40 Hexa characters");
        } else if (!m_MainAppFormController.getMagitLogic().isSha1ExistsInFileSystem(pointedCommitSha1)) {
            GUIUtilities.showErrorToUser("This SHA-1 does not exist!");
        } else if (!m_MainAppFormController.getMagitLogic().isSha1OfReachableCommit(pointedCommitSha1)) {
            GUIUtilities.showErrorToUser("This is not a SHA-1 of reachable commit!");
        } else if(m_MainAppFormController.getMagitLogic().getActiveRepository().isSha1OfCommitThatRemoteBranchPointingOn(pointedCommitSha1) != null) {
            Optional<ButtonType> result = GUIUtilities.showYesNoAlertToUser("There is a remote branch that pointing on this commit SHA-1. Do you want to create the branch as a remote tracking branch on the remote branch? (If not - The branch will be created normally)");
            try {
                if (result.get() == ButtonType.YES) {
                    Branch remoteBranch = m_MainAppFormController.getMagitLogic().getActiveRepository().isSha1OfCommitThatRemoteBranchPointingOn(pointedCommitSha1);
                    m_MainAppFormController.getMagitLogic().createRemoteTrackingBranchInFileSystemAndInOurObjects(remoteBranch.getName().split("\\\\")[1], pointedCommitSha1, remoteBranch.getName());
                    m_MainAppFormController.createCommitTree();
                } else {
                    m_MainAppFormController.getMagitLogic().createNewBranchInFileSystemAndInOurObjects(branchName, pointedCommitSha1);
                    m_MainAppFormController.createCommitTree();
                }
                m_MyStage.close();
            } catch (Exception e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
        else {
            try {
                m_MainAppFormController.getMagitLogic().createNewBranchInFileSystemAndInOurObjects(branchName, pointedCommitSha1);
                m_MainAppFormController.createCommitTree();
                m_MyStage.close();
            } catch (Exception e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }
}
