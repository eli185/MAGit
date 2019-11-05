package main;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import components.addNewRepository.AddNewRepositoryController;
import components.changeSkin.ChangeSkin;
import components.checkout.CheckoutController;
import components.collaboration.clone.CollaborationCloneController;
import components.commit.CommitController;
import components.commitInformation.CommitInformationController;
import components.commitInformation.ViewMagitFile;
import components.commitTree.commitNode.CommitNode;
import components.commitTree.commitTreeLayout.CommitTreeLayout;
import components.createNewBranch.CreateNewBranchController;
import components.deleteBranch.DeleteBranchController;
import components.listAvailableBranches.ListAvailableBranchesController;
import components.loadRepositoryFromXML.LoadRepositoryFromXMLController;
import components.merge.MergeController;
import components.merge.conflictSolver.ConflictSolverController;
import components.resetHeadBranch.ResetHeadBranchController;
import components.showWCStatus.ShowWCStatusController;
import components.switchRepository.SwitchRepositoryController;
import components.userName.ChangeActiveUserNameController;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import logic.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainAppFormController {

    @FXML
    private Label currentUserLable;
    @FXML
    private Label currentRepositoryLocationLable;
    @FXML
    private MenuItem changeUserNameButton;
    @FXML
    private MenuItem addNewRepositoryButton;
    @FXML
    private MenuItem loadRepositoryFromXML;
    @FXML
    private MenuItem switchRepositoryButton;
    @FXML
    private MenuItem showWorkingCopyStatusButton;
    @FXML
    private MenuItem commitButton;
    @FXML
    private MenuItem commitInformationButton;
    @FXML
    private MenuItem listAvailableBranchesButton;
    @FXML
    private MenuItem creatNewBranchButton;
    @FXML
    private MenuItem deleteBranchButton;
    @FXML
    private MenuItem checkoutButton;
    @FXML
    private MenuItem resetHeadBranchButton;
    @FXML
    private MenuItem mergeButton;
    @FXML
    private MenuItem cloneButton;
    @FXML
    private MenuItem fetchButton;
    @FXML
    private MenuItem pullButton;
    @FXML
    private MenuItem pushButton;
    @FXML
    private MenuItem normalModeButton;
    @FXML
    private MenuItem draculaModeButton;
    @FXML
    private MenuItem colourfulModeButton;
    @FXML
    private Label sha1Lable;
    @FXML
    private Label messageLable;
    @FXML
    private Label authorLable;
    @FXML
    private Label dateOfCreationLable;
    @FXML
    private Label differenceBetweenLastCommitLable;
    @FXML
    private Label firstPrecedingCommitsSha1Lable;
    @FXML
    private Label secondPrecedingCommitsSha1Lable;
    @FXML
    private ScrollPane commitTreeScrollPane;
    @FXML
    private CheckBox animationCheckBox;
    @FXML
    private CheckBox showFullCommitTreeCheckBox;
    @FXML
    private ImageView logoImageView;


    private final String DATE_FORMAT = "dd.MM.yyyy-hh:mm:ss:SSS";
    final Image FOLDER_ICON = new Image(getClass().getResourceAsStream("/components/commitInformation/FolderIcon.png"));
    final Image TEXT_ICON = new Image(getClass().getResourceAsStream("/components/commitInformation/TextFileIcon.png"));
    final Image MAGIT_LOGO = new Image(getClass().getResourceAsStream("/main/MagitLogo.png"));

    final int NORMAL_STYLE = 0;
    final int DRACULA_STYLE = 1;
    final int COLOURFUL_STYLE = 2;


    private ChangeActiveUserNameController m_ChangeActiveUserController;
    private AddNewRepositoryController m_AddNewRepositoryController;
    private SwitchRepositoryController m_SwitchRepositoryController;
    private ShowWCStatusController m_ShowWCStatusController;
    private CommitController m_CommitController;
    private DeleteBranchController m_DeleteBranchController;
    private ListAvailableBranchesController m_ListAvailableBranhcesController;
    private CreateNewBranchController m_CreateNewBranchController;
    private CheckoutController m_CheckoutController;
    private ResetHeadBranchController m_ResetHeadBranchController;
    private LoadRepositoryFromXMLController m_LoadRepositoryFromXMLTaskController;
    private MergeController m_MergeController;
    private CollaborationCloneController m_CollaborationCloneController;
    private CommitInformationController m_CommitInformationController;

    private Magit m_MagitLogic;
    private Scene m_PrimaryScene;
    private SimpleStringProperty m_ActiveUserNameProperty;
    private SimpleStringProperty m_ActiveRepositoryLocationProperty;
    private SimpleStringProperty m_CommitSha1;
    private SimpleStringProperty m_CommitMessage;
    private SimpleStringProperty m_CommitAuthor;
    private SimpleStringProperty m_CommitDateOfCreation;
    private SimpleStringProperty m_FirstPrecedingCommits;
    private SimpleStringProperty m_SecondPrecedingCommits;
    private SimpleStringProperty m_DifferenceBetweenLastCommit;
    private Map<CommitLogicNode, CommitNode> m_CommitNodes = new HashMap<>();
    private ChangeSkin m_ChangeSkin;
    private Stage m_PrimaryStage;
    private Map<String, CommitNode> m_CommitNodesOfFullTree = new HashMap<>();


    public MainAppFormController() {
        m_MagitLogic = new Magit();
        m_ActiveUserNameProperty = new SimpleStringProperty();
        m_ActiveRepositoryLocationProperty = new SimpleStringProperty();
        m_CommitSha1 = new SimpleStringProperty();
        m_CommitMessage = new SimpleStringProperty();
        m_CommitAuthor = new SimpleStringProperty();
        m_CommitDateOfCreation = new SimpleStringProperty();
        m_FirstPrecedingCommits = new SimpleStringProperty();
        m_SecondPrecedingCommits = new SimpleStringProperty();
        m_DifferenceBetweenLastCommit = new SimpleStringProperty();
        m_ActiveUserNameProperty.setValue(m_MagitLogic.getActiveUserName());
        m_ActiveRepositoryLocationProperty.setValue("N/A");
    }

    public void setCommitSha1(String i_CommitSha1) {
        this.m_CommitSha1.set(i_CommitSha1);
    }

    public void setCommitMessage(String i_CommitMessage) {
        this.m_CommitMessage.set(i_CommitMessage);
    }

    public void setCommitAuthor(String i_CommitAuthor) {
        this.m_CommitAuthor.set(i_CommitAuthor);
    }

    public void setPrimaryStage(Stage m_PrimaryStage) {
        this.m_PrimaryStage = m_PrimaryStage;
    }

    public void setDifferenceBetweenLastCommit(String i_DifferenceBetweenLastCommit) {
        this.m_DifferenceBetweenLastCommit.set(i_DifferenceBetweenLastCommit);
    }

    public void setCommitDateOfCreation(String i_CommitDateOfCreation) {
        this.m_CommitDateOfCreation.set(i_CommitDateOfCreation);
    }

    public void setFirstPrecedingCommits(String i_FirstPrecedingCommits) {
        this.m_FirstPrecedingCommits.set(i_FirstPrecedingCommits);
    }

    public void setSecondPrecedingCommits(String i_SecondPrecedingCommits) {
        this.m_SecondPrecedingCommits.set(i_SecondPrecedingCommits);
    }

    public void setPrimaryScene(Scene i_Scene){
        m_PrimaryScene = i_Scene;
    }

    public Map<String, CommitNode> getCommitNodesOfFullTree() {
        return m_CommitNodesOfFullTree;
    }

    public CheckBox getShowFullCommitTreeCheckBox() {
        return showFullCommitTreeCheckBox;
    }

    public CheckBox getAnimationCheckBox() {
        return animationCheckBox;
    }

    @FXML
    private void initialize() {
        currentRepositoryLocationLable.textProperty().bind(m_ActiveRepositoryLocationProperty);
        currentUserLable.textProperty().bind(m_ActiveUserNameProperty);
        sha1Lable.textProperty().bind(m_CommitSha1);
        messageLable.textProperty().bind(m_CommitMessage);
        authorLable.textProperty().bind(m_CommitAuthor);
        dateOfCreationLable.textProperty().bind(m_CommitDateOfCreation);
        differenceBetweenLastCommitLable.textProperty().bind(m_DifferenceBetweenLastCommit);
        firstPrecedingCommitsSha1Lable.textProperty().bind(m_FirstPrecedingCommits);
        secondPrecedingCommitsSha1Lable.textProperty().bind(m_SecondPrecedingCommits);
        differenceBetweenLastCommitLable.textProperty().bind(m_DifferenceBetweenLastCommit);
        m_ChangeSkin = new ChangeSkin();
        m_ChangeSkin.addCssSheet("/components/changeSkin/NormalStyle.css");
        m_ChangeSkin.addCssSheet("/components/changeSkin/DraculaStyle.css");
        m_ChangeSkin.addCssSheet("/components/changeSkin/ColourfulStyle.css");
        changeAvailableButtonsStatusIfRepositoryLoaded(true);
        logoImageView.setImage(MAGIT_LOGO);
    }

    public Magit getMagitLogic() {
        return m_MagitLogic;
    }

    public Map<CommitLogicNode, CommitNode> getCommitNodes() {
        return m_CommitNodes;
    }

    public void updateActiveRepositoryLocation(String i_PathFromUser){
        m_ActiveRepositoryLocationProperty.setValue(i_PathFromUser);
    }

    public void updateActiveUser(String i_UserNameFromUser){
        m_MagitLogic.setActiveUserName(i_UserNameFromUser);
        m_ActiveUserNameProperty.setValue(i_UserNameFromUser);
    }

    @FXML
    void OnAddNewRepositoryButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/addNewRepository/addNewRepository.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_AddNewRepositoryController = loader.getController();
            m_AddNewRepositoryController.setMainController(this);
            Stage stage = createStage("Add new repository",false,root);
            m_AddNewRepositoryController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnLoadRepositoryFromXMLClick(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/loadRepositoryFromXML/loadRepositoryFromXML.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_LoadRepositoryFromXMLTaskController = loader.getController();
            m_LoadRepositoryFromXMLTaskController.setMainController(this);
            Stage stage = createStage("Load repository from XML",false,root);
            m_LoadRepositoryFromXMLTaskController.setMyStage(stage);
            stage.show();
        }
        catch (NullPointerException e) {
            GUIUtilities.showErrorToUser(e.getStackTrace().toString());
        }
        catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnChangeUserNameButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/userName/changeActiveUserName.fxml");
            loader.setLocation(mainFXML);
            HBox root = loader.load();
            m_ChangeActiveUserController = loader.getController();
            m_ChangeActiveUserController.setMainController(this);
            Stage stage = createStage("Change user name", false, root);
            m_ChangeActiveUserController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void OnCheckoutButtonClick(ActionEvent event) {
        if (m_MagitLogic.isEmptyActiveRepository() || m_MagitLogic.getActiveRepository().getBranches().size() < 2) {
            GUIUtilities.showInformationToUser("Checkout cannot be done - There are less than 2 branches in the active repository!");
        } else {
            try {
                if (!m_MagitLogic.isWCClean()) {
                    GUIUtilities.showInformationToUser("Checkout cannot be done - there are open changes in the working copy!");
                } else {
                    FXMLLoader loader = new FXMLLoader();
                    URL mainFXML = getClass().getResource("/components/checkout/checkout.fxml");
                    loader.setLocation(mainFXML);
                    HBox root = loader.load();
                    m_CheckoutController = loader.getController();
                    m_CheckoutController.setMainController(this);
                    Stage stage = createStage("Checkout", false, root);
                    m_CheckoutController.setMyStage(stage);
                    stage.show();
                }
            } catch (IOException e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }

    @FXML
    void OnCommitButtonClick(ActionEvent event) {
        try {
            if (m_MagitLogic.isWCClean()) {
                GUIUtilities.showInformationToUser("There are no open changes - no need to commit!");
            } else {
                FXMLLoader loader = new FXMLLoader();
                URL mainFXML = getClass().getResource("/components/commit/commit.fxml");
                loader.setLocation(mainFXML);
                HBox root = loader.load();
                m_CommitController = loader.getController();
                m_CommitController.setMainController(this);
                Stage stage = createStage("Commit", false, root);
                m_CommitController.setMyStage(stage);
                stage.show();
            }
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnCreatNewBranchButtonClick(ActionEvent event) {
        if(m_MagitLogic.isEmptyActiveRepository()){
            GUIUtilities.showInformationToUser("You cannot create new branch - There are no commits in the active repository!");
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader();
                URL mainFXML = getClass().getResource("/components/createNewBranch/createNewBranch.fxml");
                loader.setLocation(mainFXML);
                BorderPane root = loader.load();
                m_CreateNewBranchController = loader.getController();
                m_CreateNewBranchController.setMainController(this);
                Stage stage = createStage("Create new branch", false, root);
                m_CreateNewBranchController.setMyStage(stage);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void OnDeleteBranchButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/deleteBranch/deleteBranch.fxml");
            loader.setLocation(mainFXML);
            HBox root = loader.load();
            m_DeleteBranchController = loader.getController();
            m_DeleteBranchController.setMainController(this);
            Stage stage = createStage("Delete branch", false, root);
            m_DeleteBranchController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void OnListAvailableBranchesButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/listAvailableBranches/listAvailableBranches.fxml");
            loader.setLocation(mainFXML);
            VBox root = loader.load();
            m_ListAvailableBranhcesController = loader.getController();
            m_ListAvailableBranhcesController.setMainController(this);
            Stage stage = createStage("List available branches",true,root);
            m_ListAvailableBranhcesController.setMyStage(stage);
            m_ListAvailableBranhcesController.updateAvailableBranchesLabel();
            stage.show();
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnResetHeadBranchButtonClick(ActionEvent event) {
        if(m_MagitLogic.isEmptyActiveRepository()){
            GUIUtilities.showInformationToUser("Reset cannot be done - There are no commits in the active repository!");
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader();
                URL mainFXML = getClass().getResource("/components/resetHeadBranch/resetHeadBranch.fxml");
                loader.setLocation(mainFXML);
                HBox root = loader.load();
                m_ResetHeadBranchController = loader.getController();
                m_ResetHeadBranchController.setMainController(this);
                Stage stage = createStage("Reset head branch", false, root);
                m_ResetHeadBranchController.setMyStage(stage);
                stage.show();
            } catch (IOException e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }

    @FXML
    void OnShowWorkingCopyStatusButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/showWCStatus/showWCStatus.fxml");
            loader.setLocation(mainFXML);
            VBox root = loader.load();
            m_ShowWCStatusController = loader.getController();
            m_ShowWCStatusController.setMainController(this);
            Stage stage = createStage("Show working copy status",true,root);
            m_ShowWCStatusController.setMyStage(stage);
            m_ShowWCStatusController.updateWorkingCopyStatusLabel();
            stage.show();
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnSwitchRepositoryButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = getClass().getResource("/components/switchRepository/switchRepository.fxml");
            loader.setLocation(mainFXML);
            HBox root = loader.load();
            m_SwitchRepositoryController = loader.getController();
            m_SwitchRepositoryController.setMainController(this);
            Stage stage = createStage("Switch repository",false,root);
            m_SwitchRepositoryController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    public Stage createStage(String i_Title, boolean i_IsResizable, Parent i_Root){
        Stage returnStage = new Stage();
        returnStage.setTitle(i_Title);
        returnStage.setScene(new Scene(i_Root));
        returnStage.setResizable(i_IsResizable);

        return returnStage;
    }

    public void changeAvailableButtonsStatusIfRepositoryLoaded(Boolean i_isDisable){
        showWorkingCopyStatusButton.setDisable(i_isDisable);
        commitButton.setDisable(i_isDisable);
        listAvailableBranchesButton.setDisable(i_isDisable);
        creatNewBranchButton.setDisable(i_isDisable);
        deleteBranchButton.setDisable(i_isDisable);
        checkoutButton.setDisable(i_isDisable);
        resetHeadBranchButton.setDisable(i_isDisable);
        mergeButton.setDisable(i_isDisable);
        fetchButton.setDisable(i_isDisable);
        pullButton.setDisable(i_isDisable);
        pushButton.setDisable(i_isDisable);
        commitInformationButton.setDisable(i_isDisable);
    }

    public void createCommitTree() {
        if(showFullCommitTreeCheckBox.isSelected()) {
            if (!m_MagitLogic.isEmptyActiveRepository()) {
                Graph tree = new Graph();
                getLogicCommitTreeOfAllCommits(tree);
                ScrollPane scrollPane = (ScrollPane) m_PrimaryScene.lookup("#scrollpaneContainer");
                PannableCanvas canvas = tree.getCanvas();
                scrollPane.setContent(canvas);

                Platform.runLater(() -> {
                    tree.getUseViewportGestures().set(false);
                    tree.getUseNodeGestures().set(false);
                });
            } else {
                // delete commitTree history
                ScrollPane scrollPane = (ScrollPane) m_PrimaryScene.lookup("#scrollpaneContainer");
                scrollPane.setContent(null);
            }
        }
        else {
            if (!m_MagitLogic.isEmptyActiveRepository()) {
                Graph tree = new Graph();
                createCommitNodes(tree);
                ScrollPane scrollPane = (ScrollPane) m_PrimaryScene.lookup("#scrollpaneContainer");
                PannableCanvas canvas = tree.getCanvas();
                scrollPane.setContent(canvas);

                Platform.runLater(() -> {
                    tree.getUseViewportGestures().set(false);
                    tree.getUseNodeGestures().set(false);
                });
            } else {
                // delete commitTree history
                ScrollPane scrollPane = (ScrollPane) m_PrimaryScene.lookup("#scrollpaneContainer");
                scrollPane.setContent(null);
            }
        }
    }

    private void createCommitNodes(Graph i_Graph) {
        m_CommitNodes.clear();

        final Model model = i_Graph.getModel();

        i_Graph.beginUpdate();
        // all branches
        CommitLogicNode commitLogicRootNode = m_MagitLogic.buildCommitLogicTree();
        CommitNode fxGraphRoot = new CommitNode(this,commitLogicRootNode.getCommit().getFirstPrecedingCommitId(),
                commitLogicRootNode.getCommit().getSecondPrecedingCommitId(), commitLogicRootNode.getCommitId(), commitLogicRootNode.getCommit().getDateOfCreation(),
                commitLogicRootNode.getCommit().getAuthor(), commitLogicRootNode.getCommit().getMessage(), commitLogicRootNode.getPointingBranch());
        model.addCell(fxGraphRoot);
        m_CommitNodes.put(commitLogicRootNode, fxGraphRoot);
        createCellsRec(fxGraphRoot, commitLogicRootNode, model);

        //update previous commit node controllers of all commit nodes
        for (Map.Entry<CommitLogicNode, CommitNode> entry : m_CommitNodes.entrySet()) {
            entry.getValue().updatePreviousCommitNodeControllersRec(entry.getKey().getFirstParent(), entry.getKey().getSecondParent());
        }

        Comparator<ICell> comparator = (o1, o2) -> {
            CommitNode firstNode = (CommitNode) o1;
            CommitNode secondNode = (CommitNode) o2;
            SimpleDateFormat general = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date firstDate = general.parse(firstNode.getTimestamp());
                Date secondDate = general.parse(secondNode.getTimestamp());
                if (firstDate.after(secondDate)) {
                    return -1;
                } else if (firstDate.before(secondDate)) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        };
        model.getAddedCells().sort(comparator);
        i_Graph.endUpdate();
        i_Graph.layout(new CommitTreeLayout());
        Platform.runLater(() -> {
            i_Graph.getUseViewportGestures().set(false);
            i_Graph.getUseNodeGestures().set(false);
        });
    }

    private void createCellsRec(CommitNode i_FxGraphRoot, CommitLogicNode i_CommitLogicNode, Model i_Model){
        if(i_CommitLogicNode.getChildrens().size() == 0){
            return;
        }
        else {
            for (CommitLogicNode child : i_CommitLogicNode.getChildrens()) {
                if(child != null) {
                    CommitNode c2 = new CommitNode(this,child.getCommit().getFirstPrecedingCommitId(),
                            child.getCommit().getSecondPrecedingCommitId(), child.getCommitId(), child.getCommit().getDateOfCreation(),
                            child.getCommit().getAuthor(), child.getCommit().getMessage(), child.getPointingBranch());
                    if(!i_Model.getAddedCells().contains(c2)) {
                        i_Model.addCell(c2);
                        m_CommitNodes.put(child, c2);
                    }
                    if(!i_Model.getAddedEdges().contains(new Edge(i_FxGraphRoot, c2))) {
                        i_Model.addEdge(new Edge(i_FxGraphRoot, c2));
                    }
                    createCellsRec(c2, child, i_Model);
                }
            }
        }
    }

    @FXML
    void OnMergeButtonClicked(ActionEvent event) {
        if (m_MagitLogic.isEmptyActiveRepository() || m_MagitLogic.getActiveRepository().getBranches().size() < 2) {
            GUIUtilities.showInformationToUser("Merge cannot be done - There are less than 2 branches in the active repository!");
        } else {
            try {
                if (!m_MagitLogic.isWCClean()) {
                    GUIUtilities.showInformationToUser("Merge cannot be done - there are open changes in the working copy!");
                } else {
                    FXMLLoader loader = new FXMLLoader();
                    URL mainFXML = getClass().getResource("/components/merge/merge.fxml");
                    loader.setLocation(mainFXML);
                    HBox root = loader.load();
                    m_MergeController = loader.getController();
                    m_MergeController.setMainController(this);
                    Stage stage = createStage("Merge", false, root);
                    m_MergeController.setMyStage(stage);
                    stage.show();
                }
            } catch (IOException e) {
                GUIUtilities.showErrorToUser(e.toString());
            }
        }
    }

    @FXML
    void OnCloneButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/collaboration/clone/collaborationClone.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_CollaborationCloneController = loader.getController();
            m_CollaborationCloneController.setMainController(this);
            Stage stage = createStage("Clone", false, root);
            m_CollaborationCloneController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void OnFetchButtonClick(ActionEvent event) {
        if(m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation() == null || m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation().isEmpty()){
            GUIUtilities.showInformationToUser("Fetch cannot be done - There is no remote repository!");
        }
        else {
            try {
                m_MagitLogic.fetchRRNewData(m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation());
                createCommitTree();
                GUIUtilities.showInformationToUser("Fetch has been made");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void OnPullButtonClick(ActionEvent event) {
        if (m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation() == null || m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation().isEmpty()) {
            GUIUtilities.showInformationToUser("Pull cannot be done - There is no remote repository!");
        } else {
            try {
                if (!m_MagitLogic.isWCClean()) {
                    GUIUtilities.showInformationToUser("You can not make pull with open changes.");
                } else if (!m_MagitLogic.getActiveRepository().getHeadBranch().getIsTracking()) {
                    GUIUtilities.showInformationToUser("Pull is possible only for Remote Tracking branches");
                }
            else {
                    m_MagitLogic.Pull();
                    createCommitTree();
                    GUIUtilities.showInformationToUser("Pull has been made");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void OnPushButtonClick(ActionEvent event) {
        try {
            if (m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation() == null || m_MagitLogic.getActiveRepository().getRemoteRepositoryLocation().isEmpty()) {
                GUIUtilities.showInformationToUser("Push cannot be done - There is no remote repository!");
            } else if (!m_MagitLogic.getActiveRepository().getHeadBranch().getIsTracking()) {
                GUIUtilities.showInformationToUser("Push is possible only for remote tracking branches!");
            } else if (!m_MagitLogic.checkIfRRHasNoOpenChanges()) {
                GUIUtilities.showInformationToUser("Push cannot be done - There are open changes no remote repository!");
            } else if (!m_MagitLogic.checkIfBranchesPointingTheSameCommits()) {
                GUIUtilities.showInformationToUser("Push cannot be done - Remote tracking branch pointed to different commit compare to this branch in Remote repository!");
            } else {
                try {
                    m_MagitLogic.Push();
                    GUIUtilities.showInformationToUser("Push has been made");
                    createCommitTree();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void OnCommitInformationClick(ActionEvent event) {
        try {
            ObservableList<String> commitList = createCommitList();
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/commitInformation/commitInformation.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            m_CommitInformationController = fxmlLoader.getController();
            m_CommitInformationController.setMainController(this);
            m_CommitInformationController.setMyStage(stage);
            m_CommitInformationController.setChooseCommitSha1(commitList);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<String> createCommitList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.removeAll();
        for (Map.Entry<String, Commit> entry : m_MagitLogic.getActiveRepository().getCommits().entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    public void createTheSelectedCommitInformationTree(String i_SelectedCommitSha1) {
        TreeView<ViewMagitFile> tree = buildTreeViewOfCommitFiles(m_MagitLogic.getActiveRepository().getCommits().get(i_SelectedCommitSha1));
        displayTreeView(tree);
    }

    private void displayTreeView(TreeView<ViewMagitFile> i_Tree) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/commitInformation/borderPaneToDisplay.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            m_CommitInformationController = fxmlLoader.getController();
            m_CommitInformationController.setMainController(this);
            m_CommitInformationController.setMyStage(stage);
            m_CommitInformationController.setCommitInformationTree(i_Tree);
            m_CommitInformationController.ShowCommitInformation();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TreeView<ViewMagitFile> buildTreeViewOfCommitFiles(Commit i_Commit) {
        TreeView<ViewMagitFile> treeView = new TreeView<>();
        Folder mainFolder = m_MagitLogic.getActiveRepository().getFolders().get(i_Commit.getRootFolderId());
        ViewMagitFile viewMagitFile;
        viewMagitFile = new ViewMagitFile(m_MagitLogic.getActiveRepository().getFolders().get(i_Commit.getRootFolderId()).toString(),
                m_MagitLogic.extractRepositoryNameFromPath(m_MagitLogic.getActiveRepository().getLocation()));
        TreeItem<ViewMagitFile> root = new TreeItem<>(viewMagitFile);
        buildTreeViewOfCommitFilesRec(mainFolder, root);
        treeView.setRoot(root);

        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
            Node node = event.getPickResult().getIntersectedNode();
            if (node instanceof TreeCell) {
                if (event.getClickCount() == 2 && treeView.getSelectionModel().getSelectedItem() != null
                        && (treeView.getSelectionModel().getSelectedItem()).isLeaf()) {
                    TextArea textArea = new TextArea(treeView.getSelectionModel().getSelectedItem().getValue().getM_Content());
                    textArea.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                    textArea.editableProperty().setValue(false);
                    models.PopUpWindowWithBtn.popUpWindow(500, 400, "O.K", (v) -> {
                    }, new Object(), textArea);
                }
            }

        };

        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);

        return treeView;
    }

    public void buildTreeViewOfCommitFilesRec(Folder folder, TreeItem<ViewMagitFile> i_TreeItem) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(25);
        imageView.setImage(FOLDER_ICON);
        i_TreeItem.setGraphic(imageView);
        List<Folder.ItemData> itemsArray = folder.getItems();
        for(Folder.ItemData itemsInFolder : itemsArray){
            if(itemsInFolder.getType().equals(Folder.ItemData.eItemType.FOLDER)){
                ViewMagitFile viewMagitFile = new ViewMagitFile(m_MagitLogic.getActiveRepository().getFolders().get(itemsInFolder.getId()).toString(),itemsInFolder.getName());
                TreeItem<ViewMagitFile> subTreeItem = new TreeItem<>(viewMagitFile);
                i_TreeItem.getChildren().add(subTreeItem);
                buildTreeViewOfCommitFilesRec(m_MagitLogic.getActiveRepository().getFolders().get(itemsInFolder.getId()), subTreeItem);
            }
            else{
                ImageView imageView2 = new ImageView();
                imageView2.setFitHeight(25);
                imageView2.setFitWidth(20);
                imageView2.setImage(TEXT_ICON);
                ViewMagitFile viewMagitFile = new ViewMagitFile(m_MagitLogic.getActiveRepository().getBlobs().get(itemsInFolder.getId()).getContent(), itemsInFolder.getName());
                TreeItem<ViewMagitFile> subTreeItem = new TreeItem<>(viewMagitFile);
                i_TreeItem.getChildren().add(subTreeItem);
                subTreeItem.setGraphic(imageView2);
            }
        }
    }

    @FXML
    void OnChangeSkinButtonClick(ActionEvent event) {
    }

    @FXML
    void OnNormalModeButtonClick(ActionEvent event) {
        changeStyle(NORMAL_STYLE);
    }


    @FXML
    void OnDraculaModeButtonClick(ActionEvent event) {
        changeStyle(DRACULA_STYLE);
    }

    @FXML
    void OnColourfulModeButton(ActionEvent event) {
        changeStyle(COLOURFUL_STYLE);
    }

    private void changeStyle(int i_StyleIndex){
        BorderPane bp = (BorderPane)m_PrimaryScene.lookup("#mainBorderPane");
        bp.getStylesheets().clear();
        bp.getStylesheets().add(m_ChangeSkin.getCurrentCss(i_StyleIndex));
    }

    public void handleConflicts(List<MergeConflict> i_Conflicts, String i_BranchToMerge) throws IOException {
        if (i_Conflicts == null) {
            createCommitTree();
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
            getMagitLogic().spanWCSolvedConflictList(i_Conflicts);
            showCommit(i_BranchToMerge);
        }
    }

    public void showCommit(String i_BranchNameToMergeWith) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL mainFXML = getClass().getResource("/components/commit/commit.fxml");
        loader.setLocation(mainFXML);
        HBox root = loader.load();
        CommitController commitController = loader.getController();
        commitController.setMainController(this);
        commitController.setSecondPrecedingCommit(getMagitLogic().getActiveRepository().getBranches().get(i_BranchNameToMergeWith).getPointedCommitId());
        Stage stage = createStage("Commit", false, root);
        commitController.setMyStage(stage);
        stage.show();
    }

    public void getLogicCommitTreeOfAllCommits(Graph i_Graph) {
        final Model model = i_Graph.getModel();
        i_Graph.beginUpdate();
        try {
            Map<String, Commit> commitMap = m_MagitLogic.getAllCommitsInFileSystem();


            Map<String, CommitNode> commitNodes = new HashMap<>();
            for (Map.Entry<String, Commit> entryCommit : commitMap.entrySet()) {
                CommitNode newCommitNode = new CommitNode(this, entryCommit.getValue().getFirstPrecedingCommitId(),
                        entryCommit.getValue().getSecondPrecedingCommitId(), entryCommit.getValue().getSha1(), entryCommit.getValue().getDateOfCreation(),
                        entryCommit.getValue().getAuthor(), entryCommit.getValue().getMessage(), m_MagitLogic.findPointingBranchNameOfCommit(entryCommit.getValue().getSha1()));
                if (!commitNodes.containsKey(entryCommit.getValue().getSha1())) {
                    commitNodes.put(entryCommit.getValue().getSha1(), newCommitNode);

                }
                if (!model.getAddedCells().contains(newCommitNode)) {
                    model.addCell(newCommitNode);
                    m_CommitNodesOfFullTree.put(entryCommit.getValue().getSha1(), newCommitNode);
                }
                if (!entryCommit.getValue().getFirstPrecedingCommitId().isEmpty() && entryCommit.getValue().getFirstPrecedingCommitId() != null) {
                    Commit childCommit = commitMap.get(entryCommit.getValue().getFirstPrecedingCommitId());
                    CommitNode childCommitNode = new CommitNode(this, childCommit.getFirstPrecedingCommitId(),
                            childCommit.getSecondPrecedingCommitId(), childCommit.getSha1(), childCommit.getDateOfCreation(),
                            childCommit.getAuthor(), childCommit.getMessage(), m_MagitLogic.findPointingBranchNameOfCommit(childCommit.getSha1()));
                    if (!commitNodes.containsKey(childCommit.getSha1())) {
                        commitNodes.put(childCommit.getSha1(), childCommitNode);
                    }
                    if (!model.getAddedCells().contains(childCommitNode)) {
                        model.addCell(childCommitNode);
                        m_CommitNodesOfFullTree.put(childCommit.getSha1(), childCommitNode);

                    }
                    if (!model.getAddedEdges().contains(new Edge(newCommitNode, childCommitNode))) {
                        model.addEdge(new Edge(newCommitNode, childCommitNode));
                    }
                    if (!entryCommit.getValue().getSecondPrecedingCommitId().isEmpty() && entryCommit.getValue().getSecondPrecedingCommitId() != null) {
                        Commit SecondChildCommit = commitMap.get(entryCommit.getValue().getSecondPrecedingCommitId());
                        CommitNode SecondChildCommitNode = new CommitNode(this, SecondChildCommit.getFirstPrecedingCommitId(),
                                SecondChildCommit.getSecondPrecedingCommitId(), SecondChildCommit.getSha1(), SecondChildCommit.getDateOfCreation(),
                                SecondChildCommit.getAuthor(), SecondChildCommit.getMessage(), m_MagitLogic.findPointingBranchNameOfCommit(SecondChildCommit.getSha1()));
                        if (!commitNodes.containsKey(SecondChildCommit.getSha1())) {
                            commitNodes.put(SecondChildCommit.getSha1(), SecondChildCommitNode);
                        }
                        if (!model.getAddedCells().contains(SecondChildCommitNode)) {
                            model.addCell(SecondChildCommitNode);
                            m_CommitNodesOfFullTree.put(SecondChildCommit.getSha1(), SecondChildCommitNode);

                        }
                        if (!model.getAddedEdges().contains(new Edge(newCommitNode, SecondChildCommitNode))) {
                            model.addEdge(new Edge(newCommitNode, SecondChildCommitNode));
                        }
                    }
                }
            }

            Comparator<ICell> comparator = (o1, o2) -> {
                CommitNode firstNode = (CommitNode) o1;
                CommitNode secondNode = (CommitNode) o2;
                SimpleDateFormat general = new SimpleDateFormat(DATE_FORMAT);
                try {
                    Date firstDate = general.parse(firstNode.getTimestamp());
                    Date secondDate = general.parse(secondNode.getTimestamp());
                    if (firstDate.after(secondDate)) {
                        return -1;
                    } else if (firstDate.before(secondDate)) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            };
            model.getAddedCells().sort(comparator);
            i_Graph.endUpdate();
            i_Graph.layout(new CommitTreeLayout());
            Platform.runLater(() -> {
                i_Graph.getUseViewportGestures().set(false);
                i_Graph.getUseNodeGestures().set(false);
            });
        } catch (IOException e) {
            GUIUtilities.showErrorToUser(e.toString());
        }
    }

    @FXML
    void OnLogoImageViewClick(MouseEvent event) {
        if(!animationCheckBox.isSelected()) {
            RotateTransition rt = new RotateTransition(Duration.millis(2000), logoImageView);
            rt.setByAngle(360);
            rt.setCycleCount(1);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
        }
    }

    @FXML
    void OnShowFullCommitTreeClick(ActionEvent event) {
        createCommitTree();
    }
}
