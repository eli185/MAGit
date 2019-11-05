package components.userName;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;

public class ChangeActiveUserNameController {
    @FXML
    private TextField newUserNameTextField;
    @FXML
    private Button newActiveUserSubmitButton;
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
    void OnNewActiveUserSubmitButtonClick(ActionEvent event) {
        String userName = newUserNameTextField.getText();
        if(userName.isEmpty()){
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        }
        else {
            m_MainAppFormController.updateActiveUser(userName);
            m_MyStage.close();
        }
    }
}
