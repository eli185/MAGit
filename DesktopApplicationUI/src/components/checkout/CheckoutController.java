package components.checkout;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.IOException;
import java.util.Optional;

public class CheckoutController {
    @FXML
    private TextField newHeadBranchNameTextField;
    @FXML
    private Button newHeadBranchSubmitButton;
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
    void OnNewHeadBranchSubmitButtonClick(ActionEvent event) {
        String branchName = newHeadBranchNameTextField.getText();

        if (branchName.isEmpty()) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        } else if (m_MainAppFormController.getMagitLogic().getActiveRepository().getHeadBranch().getName().equals(branchName)) {
            GUIUtilities.showErrorToUser("You cannot checkout to the current head branch!");
        }
        else if (!m_MainAppFormController.getMagitLogic().isBranchExistsInFileSystem(branchName)) {
            GUIUtilities.showErrorToUser("This branch does not exist!");
        }
        else if (m_MainAppFormController.getMagitLogic().getActiveRepository().getBranches().get(branchName).getIsRemote()) {
            Optional<ButtonType> result = GUIUtilities.showYesNoAlertToUser("Cannot checkout to remote branch. Do you want to create a remote tracking branch and checkout?");
            if (result.get() == ButtonType.YES) {
                try {
                    m_MainAppFormController.getMagitLogic().createRemoteTrackingBranchAndCheckout(branchName, m_MainAppFormController.getMagitLogic().getActiveRepository().getBranches().get(branchName).getPointedCommitId());
                    m_MainAppFormController.createCommitTree();
                } catch (IOException e) {
                    GUIUtilities.showErrorToUser(e.toString());
                }
            }
            m_MyStage.close();
        }
        else {
            try {
                m_MainAppFormController.getMagitLogic().checkoutBranch(branchName);
                m_MainAppFormController.createCommitTree();
                m_MyStage.close();
            } catch (IOException e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }
}


