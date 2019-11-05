package components.commitTree.commitNode;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import main.MainAppFormController;
import java.util.List;
import java.util.regex.Pattern;

public class CommitNodeController {
    @FXML
    private Circle CommitCircle;
    @FXML
    private Label commitTimeStampLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label committerLabel;
    @FXML
    private TextFlow pointingBranchTextFlow;
    @FXML
    private GridPane gridPane;

    private String m_FirstPrecedingCommit;
    private String m_SecondPrecedingCommit;
    private String m_Sha1;
    private String m_DifferenceBetweenLastCommit;
    private MainAppFormController m_MainAppFormController;
    private List<CommitNodeController> m_PreviousCommitNodeControllers = null;

    public void setDifferenceBetweenLastCommit(String i_DifferenceBetweenLastCommit) {
        this.m_DifferenceBetweenLastCommit = i_DifferenceBetweenLastCommit;
    }

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    public void setPointingBranch(String pointingBranch) {
        Text text1;

        if (!pointingBranch.contains(",")) {
            text1 = new Text(pointingBranch);
            if (m_MainAppFormController.getMagitLogic().getActiveRepository().getHeadBranch().getName().equals(pointingBranch)) {
                text1.fillProperty().setValue(Color.BLUE);
                text1.fontProperty().set(Font.font("Verdana", FontWeight.BOLD, 20));
            }
            if (m_MainAppFormController.getMagitLogic().isRemoteBranch(pointingBranch)) {
                text1.fillProperty().setValue(Color.GREEN);
                text1.fontProperty().set(Font.font("Verdana", FontWeight.BOLD, 15));
            }
            pointingBranchTextFlow.getChildren().add(text1);
        } else {
            String separator = ", ";
            String[] splittedPathContent = pointingBranch.split(Pattern.quote(separator));
            for (String branchName : splittedPathContent) {
                if (!branchName.equals(splittedPathContent[splittedPathContent.length - 1])) {
                    text1 = new Text(branchName + ", ");
                } else {
                    text1 = new Text(branchName);
                }

                if (m_MainAppFormController.getMagitLogic().getActiveRepository().getHeadBranch().getName().equals(branchName)) {
                    text1.fillProperty().setValue(Color.BLUE);
                    text1.fontProperty().set(Font.font("Verdana", FontWeight.BOLD, 20));
                }

                if (m_MainAppFormController.getMagitLogic().isRemoteBranch(branchName)) {
                    text1.fillProperty().setValue(Color.GREEN);
                    text1.fontProperty().set(Font.font("Verdana", FontWeight.BOLD, 15));
                }
                pointingBranchTextFlow.getChildren().add(text1);
            }
        }
    }

    public void setMainController(MainAppFormController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    public void setFirstPrecedingCommit(String m_FirstPrecedingCommit) {
        this.m_FirstPrecedingCommit = m_FirstPrecedingCommit;
    }

    public void setSecondPrecedingCommit(String m_SecondPrecedingCommit) {
        this.m_SecondPrecedingCommit = m_SecondPrecedingCommit;
    }

    public void setSha1(String m_Sha1) {
        this.m_Sha1 = m_Sha1;
    }

    public int getCircleRadius() {
        return (int) CommitCircle.getRadius();
    }

    public String getFirstPrecedingCommit() {
        return m_FirstPrecedingCommit;
    }

    public String getSecondPrecedingCommit() {
        return m_SecondPrecedingCommit;
    }

    public TextFlow getPointingBranchTextFlow() {
        return pointingBranchTextFlow;
    }

    public void setPreviousCommitNodeControllers(List<CommitNodeController> m_PreviousCommitNodeControllers) {
        this.m_PreviousCommitNodeControllers = m_PreviousCommitNodeControllers;
    }

    public Circle getCommitCircle() {
        return CommitCircle;
    }

    @FXML
    void OnNodeClicked(MouseEvent event) {
        m_MainAppFormController.setCommitAuthor(committerLabel.textProperty().get());
        m_MainAppFormController.setCommitDateOfCreation(commitTimeStampLabel.textProperty().get());
        m_MainAppFormController.setCommitMessage(messageLabel.textProperty().get());
        m_MainAppFormController.setCommitSha1(m_Sha1);
        m_MainAppFormController.setFirstPrecedingCommits(m_FirstPrecedingCommit);
        m_MainAppFormController.setSecondPrecedingCommits(m_SecondPrecedingCommit);
        m_MainAppFormController.setDifferenceBetweenLastCommit(m_DifferenceBetweenLastCommit);
    }

    @FXML
    void OnNodeEntered(MouseEvent event) {
        highlightNodeCircle(this, 12, Color.PURPLE);
    }

    @FXML
    void OnNodeExited(MouseEvent event) {
        highlightNodeCircle(this, 10, Color.valueOf("087fee"));
    }

    private void highlightNodeCircle(CommitNodeController i_CommitNodeController, int i_Radius, Color i_Color) {
        if(m_MainAppFormController.getShowFullCommitTreeCheckBox().isSelected()) {
            CommitCircle.setRadius(i_Radius);
            CommitCircle.setFill(i_Color);
            if (i_CommitNodeController.getFirstPrecedingCommit() != null && !i_CommitNodeController.getFirstPrecedingCommit().isEmpty()) {
                CommitNode firstChild = m_MainAppFormController.getCommitNodesOfFullTree().get(i_CommitNodeController.getFirstPrecedingCommit());
                firstChild.getCommitNodeController().getCommitCircle().setRadius(i_Radius);
                firstChild.getCommitNodeController().getCommitCircle().setFill(i_Color);
                highlightNodeCircle(firstChild.getCommitNodeController(), i_Radius, i_Color);
            }
            if (i_CommitNodeController.getSecondPrecedingCommit() != null && !i_CommitNodeController.getSecondPrecedingCommit().isEmpty()) {
                CommitNode secondChild = m_MainAppFormController.getCommitNodesOfFullTree().get(i_CommitNodeController.getSecondPrecedingCommit());
                secondChild.getCommitNodeController().getCommitCircle().setRadius(i_Radius);
                secondChild.getCommitNodeController().getCommitCircle().setFill(i_Color);
                highlightNodeCircle(secondChild.getCommitNodeController(), i_Radius, i_Color);
            }
        }
        else {
            CommitCircle.setRadius(i_Radius);
            CommitCircle.setFill(i_Color);
            if (!i_CommitNodeController.m_PreviousCommitNodeControllers.isEmpty()) {
                for (CommitNodeController cnc : i_CommitNodeController.m_PreviousCommitNodeControllers) {
                    cnc.getCommitCircle().setRadius(i_Radius);
                    cnc.getCommitCircle().setFill(i_Color);
                }
            }
        }
    }

    @FXML
    void OnBranchNameClick(MouseEvent event) {
        if(!m_MainAppFormController.getAnimationCheckBox().isSelected()) {
            if (m_MainAppFormController.getShowFullCommitTreeCheckBox().isSelected()) {
                Node textNode = pointingBranchTextFlow.getChildren().get(0);
                String text = ((Text) textNode).getText();
                if (!text.equals("No branch")) {
                    flashAnimationForFullTree(this);
                }
            } else {
                Node textNode = pointingBranchTextFlow.getChildren().get(0);
                String text = ((Text) textNode).getText();
                if (!text.equals("No branch")) {
                    flashAnimation(getCommitCircle());
                    if (!m_PreviousCommitNodeControllers.isEmpty()) {
                        for (CommitNodeController cnc : m_PreviousCommitNodeControllers) {
                            flashAnimation(cnc.CommitCircle);
                        }
                    }
                }
            }
        }
    }

    private void flashAnimationForFullTree(CommitNodeController i_CommitNodeController) {
        flashAnimation(i_CommitNodeController.getCommitCircle());
        if (i_CommitNodeController.getFirstPrecedingCommit() != null && !i_CommitNodeController.getFirstPrecedingCommit().isEmpty()) {
            CommitNode firstChild = m_MainAppFormController.getCommitNodesOfFullTree().get(i_CommitNodeController.getFirstPrecedingCommit());
            flashAnimation(firstChild.getCommitNodeController().getCommitCircle());
            flashAnimationForFullTree(firstChild.getCommitNodeController());
        }
        if (i_CommitNodeController.getSecondPrecedingCommit() != null && !i_CommitNodeController.getSecondPrecedingCommit().isEmpty()) {
            CommitNode secondChild = m_MainAppFormController.getCommitNodesOfFullTree().get(i_CommitNodeController.getSecondPrecedingCommit());
            flashAnimation(secondChild.getCommitNodeController().getCommitCircle());
            flashAnimationForFullTree(secondChild.getCommitNodeController());
        }
    }

    private void flashAnimation(Circle i_Circle){
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), i_Circle);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(2);
        fadeTransition.play();
    }


}
