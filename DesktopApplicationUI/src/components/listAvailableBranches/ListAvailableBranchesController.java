package components.listAvailableBranches;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.GUIUtilities;
import main.MainAppFormController;

public class ListAvailableBranchesController {

    @FXML
    private Label availableBranchesLabel;
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

    public void updateAvailableBranchesLabel() {
        try {
            availableBranchesLabel.setText(m_MainAppFormController.getMagitLogic().getAvailableBranchesData());
        }
        catch (Exception e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }
}