package components.merge.conflictSolver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logic.MergeConflict;

public class ConflictSolverController {

    @FXML
    private Label fileNameLabel;
    @FXML
    private TextArea finalTextArea;
    @FXML
    private TextArea oursTextArea;
    @FXML
    private TextArea theirsTextArea;
    @FXML
    private TextArea ancestorTextArea;
    @FXML
    private Button solveConflictButton;
    @FXML
    private Button deleteFileButton;
    @FXML
    private Button getOurVersionButton;
    @FXML
    private Button getAncestorVersionButton;
    @FXML
    private Button getTheirsVersionButton;
    private Stage m_MyStage;
    private MergeConflict m_MergeConfilct;

    public void setMergeConfilct(MergeConflict i_MergeConfilct){
        this.m_MergeConfilct = i_MergeConfilct;
        setFileNameLabel(i_MergeConfilct.getPath());
    }

    public void setMyStage(Stage primaryStage) {
        this.m_MyStage = primaryStage;
    }

    public void setOursTextArea(String i_Text) {
        this.oursTextArea.setText(i_Text);
    }

    public void setAncestorTextArea(String i_Text) {
        this.ancestorTextArea.setText(i_Text);
    }

    public void setTheirsTextArea(String i_Text) {
        this.theirsTextArea.setText(i_Text);
    }

    public void setFileNameLabel(String i_FileFullPath){
        fileNameLabel.setText(i_FileFullPath);
    }

    @FXML
    private void initialize() {
    }

    @FXML
    void deleteFileButtonClicked(ActionEvent event) {
        m_MyStage.close();
    }

    @FXML
    void getAncestorVersionButtonClicked(ActionEvent event) {
        finalTextArea.setText(ancestorTextArea.getText());
    }

    @FXML
    void getOurVersionButtonClicked(ActionEvent event) {
        finalTextArea.setText(oursTextArea.getText());
    }

    @FXML
    void getTheirsVersionButtonClicked(ActionEvent event) {
        finalTextArea.setText(theirsTextArea.getText());
    }

    @FXML
    void solveConflictButtonClicked(ActionEvent event) {
        String fixedText = (finalTextArea.getText()).replace("\n", "\r\n");
        m_MergeConfilct.setResolveContent(fixedText);
        m_MyStage.close();
    }
}
