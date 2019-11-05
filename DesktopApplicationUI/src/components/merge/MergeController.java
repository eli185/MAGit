package components.merge;

import components.merge.conflictSolver.ConflictSolverController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.MergeConflict;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MergeController {
    @FXML
    private TextField branchNameToMergeWithTextField;
    @FXML
    private Button mergeSubmitButton;
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
    void mergeSubmitButtonClick(ActionEvent event) {
        String branchToMerge = branchNameToMergeWithTextField.getText();
        if (branchToMerge.isEmpty()) {
            GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
        } else if (m_MainAppFormController.getMagitLogic().getActiveRepository().getHeadBranch().getName().equals(branchToMerge)) {
            GUIUtilities.showErrorToUser("You cannot merge with the current head branch!");
        } else if (!m_MainAppFormController.getMagitLogic().isBranchExistsInFileSystem(branchToMerge)) {
            GUIUtilities.showErrorToUser("This branch does not exist!");
        } else {
            try {
                List<MergeConflict> conflicts;
                conflicts = m_MainAppFormController.getMagitLogic().merge(m_MainAppFormController.getMagitLogic().getActiveRepository().getBranches().get(branchToMerge));
                handleConflicts(conflicts, branchToMerge);
            } catch (Exception e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }

    private void handleConflicts(List<MergeConflict> i_Conflicts, String i_BranchToMerge) throws IOException {
        if (i_Conflicts == null) {
            m_MainAppFormController.createCommitTree();
            m_MyStage.close();
            GUIUtilities.showInformationToUser("Fast Forward Merge has been done!");
        } else {
            for (MergeConflict conflict : i_Conflicts) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/merge/conflictSolver/conflictSolver.fxml");
                fxmlLoader.setLocation(url);
                GridPane root = fxmlLoader.load(url.openStream());
                ConflictSolverController conflictSolverController = fxmlLoader.getController();
                conflictSolverController.setMergeConfilct(conflict);
                Stage secStage = new Stage();
                secStage.setScene(new Scene(root));
                conflictSolverController.setMyStage(secStage);
                conflictSolverController.setAncestorTextArea(conflict.getAncestorContent());
                conflictSolverController.setOursTextArea(conflict.getOurContent());
                conflictSolverController.setTheirsTextArea(conflict.getTheirsContent());
                secStage.showAndWait();
            }
            m_MainAppFormController.getMagitLogic().spanWCSolvedConflictList(i_Conflicts);
            m_MainAppFormController.showCommit(i_BranchToMerge);
            m_MyStage.close();
        }
    }
}
