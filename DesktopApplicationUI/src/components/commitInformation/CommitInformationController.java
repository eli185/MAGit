
package components.commitInformation;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.MainAppFormController;

public class CommitInformationController {

    @FXML
    private Button chooseCommitSha1Submit;
    @FXML
    private ComboBox<String> chooseCommitSha1;
    @FXML
    private BorderPane emptyBorderPane;
    @FXML
    private MainAppFormController m_MainController;

    private TreeView<ViewMagitFile> m_CommitInformationTree;
    private Stage m_MyStage;
    private String m_SelectedCommitSha1;


    @FXML
    private void initialize() {
    }
    public void setCommitInformationTree(TreeView<ViewMagitFile> m_CommitInfromationTree) {
        this.m_CommitInformationTree = m_CommitInfromationTree;
    }

    public void setChooseCommitSha1(ObservableList<String> commitsListOfSha1) {
        chooseCommitSha1.setItems(commitsListOfSha1);
    }

    public void setMainController(MainAppFormController m_MainController) {
        this.m_MainController = m_MainController;
    }

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }
    @FXML
    void OnChooseCommitSha1Submit(ActionEvent event) {
        m_SelectedCommitSha1 = chooseCommitSha1.getValue();
        if(m_SelectedCommitSha1.isEmpty() || m_SelectedCommitSha1 == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("No Commit was selected!Please try again!");
            errorAlert.showAndWait();
        }
        else{
            m_MainController.createTheSelectedCommitInformationTree(m_SelectedCommitSha1);
            m_MyStage.close();
        }

    }

    public void ShowCommitInformation() {
        m_CommitInformationTree.setPrefHeight(300);
        m_CommitInformationTree.setMaxHeight(400);
        emptyBorderPane.setCenter(m_CommitInformationTree);
    }

}
