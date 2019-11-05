package components.commit;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;

public class CommitController {
    @FXML
    private TextField newCommitTextField;
    @FXML
    private Button newCommitMessageSubmitButton;
    private MainAppFormController m_MainAppFormController;
    private Stage m_MyStage;
    private String m_SecondPrecedingCommit = null;

    public void setMainController(MainAppFormController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setSecondPrecedingCommit(String m_SecondPrecedingCommit) {
        this.m_SecondPrecedingCommit = m_SecondPrecedingCommit;
    }

    @FXML
    void OnNewCommitMessageSubmitButtonClick(ActionEvent event) {
        String commitMessage = newCommitTextField.getText();

        if(commitMessage.isEmpty() || commitMessage == null){
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty message");
        }
        else {
            try {
                if(m_SecondPrecedingCommit == null) {
                    m_MainAppFormController.getMagitLogic().commit(newCommitTextField.getText());
                }
                else{
                    m_MainAppFormController.getMagitLogic().commit(newCommitTextField.getText(), m_SecondPrecedingCommit);
                }
                m_MainAppFormController.createCommitTree();
                m_MyStage.close();
            }
            catch (Exception e){
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }
}
