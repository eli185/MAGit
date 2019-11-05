package components.commitTree.commitNode;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import logic.Branch;
import logic.CommitLogicNode;
import logic.Repository;
import main.GUIUtilities;
import main.MainAppFormController;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommitNode extends AbstractCell {
    private String m_FirstPrecedingCommit;
    private String m_SecondPrecedingCommit;
    private String m_Sha1;
    private String m_Timestamp;
    private String m_Committer;
    private String m_Message;
    private String m_PointingBranch;
    private List<CommitNodeController> m_PreviousCommitNodeControllers = new ArrayList<>();
    private CommitNodeController m_CommitNodeController;
    private GridPane m_Root;
    private MainAppFormController m_MainAppFormController;


    public CommitNode(MainAppFormController i_MainAppFormController, String  i_FirstPrecedingCommit, String i_SecondPrecedingCommit,
                      String i_Sha1, String i_timestamp, String i_committer, String i_message, String i_pointingBranch) {
        m_Sha1 = i_Sha1;
        m_Timestamp = i_timestamp;
        m_Committer = i_committer;
        m_Message = i_message;
        m_FirstPrecedingCommit = i_FirstPrecedingCommit;
        m_SecondPrecedingCommit = i_SecondPrecedingCommit;
        m_MainAppFormController = i_MainAppFormController;

        if(i_pointingBranch.equals("")){
            m_PointingBranch = "No branch";
        }
        else {
            m_PointingBranch = i_pointingBranch;
        }

        loadFXML();
    }

    public String getTimestamp(){
        return m_Timestamp;
    }

    private void loadFXML() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/commitTree/commitNode/commitNode.fxml");
            fxmlLoader.setLocation(url);
            m_Root = fxmlLoader.load(url.openStream());
            ContextMenuCommitNode contextMenuCommitNode = new ContextMenuCommitNode();

            chainResetHeadBranch(contextMenuCommitNode);
            chainCreateNewBranch(contextMenuCommitNode);
            chainMerge(contextMenuCommitNode);
            chainDeleteBranch(contextMenuCommitNode);
            EventHandler<MouseEvent> mouseRightClickEventHandler = (MouseEvent event) -> {
                if(event.getButton() == MouseButton.SECONDARY ) {
                    contextMenuCommitNode.getContextMenu().show(m_Root,event.getScreenX(),event.getScreenY());
                }
            };
            m_Root.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseRightClickEventHandler);

            m_CommitNodeController = fxmlLoader.getController();
            m_CommitNodeController.setMainController(m_MainAppFormController);
            m_CommitNodeController.setCommitMessage(m_Message);
            m_CommitNodeController.setCommitter(m_Committer);
            m_CommitNodeController.setCommitTimeStamp(m_Timestamp);
            m_CommitNodeController.setPointingBranch(m_PointingBranch);
            m_CommitNodeController.setSha1(m_Sha1);
            m_CommitNodeController.setFirstPrecedingCommit(m_FirstPrecedingCommit);
            m_CommitNodeController.setSecondPrecedingCommit(m_SecondPrecedingCommit);
            m_CommitNodeController.setPreviousCommitNodeControllers(m_PreviousCommitNodeControllers);
            if(m_SecondPrecedingCommit.isEmpty()) {
                String differenceBetweenLastCommit = "";

                differenceBetweenLastCommit += "Difference between first preceding commit:\n";
                differenceBetweenLastCommit += m_MainAppFormController.getMagitLogic().getDifferenceBetweenTwoCommits(m_Sha1, m_FirstPrecedingCommit);
                m_CommitNodeController.setDifferenceBetweenLastCommit(differenceBetweenLastCommit);
            }
            else {
                String differenceBetweenLastCommit = "";

                differenceBetweenLastCommit += "Difference between first preceding commit:\n";
                differenceBetweenLastCommit += m_MainAppFormController.getMagitLogic().getDifferenceBetweenTwoCommits(m_Sha1, m_FirstPrecedingCommit);
                differenceBetweenLastCommit += "Difference between second preceding commit:\n";
                differenceBetweenLastCommit += m_MainAppFormController.getMagitLogic().getDifferenceBetweenTwoCommits(m_Sha1, m_SecondPrecedingCommit);
                m_CommitNodeController.setDifferenceBetweenLastCommit(differenceBetweenLastCommit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Region getGraphic(Graph graph) {
        return m_Root;
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(m_CommitNodeController.getCircleRadius());
    }

    public CommitNodeController getCommitNodeController(){
        return m_CommitNodeController;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual;

        if(this == o) {
            isEqual = true;
        }
        else if(!(o instanceof CommitNode)) {
            isEqual = false;
        }
        else {
            CommitNode nodeToCompare = (CommitNode) o;
            isEqual = m_Sha1.equals(nodeToCompare.GetSha1());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return m_Timestamp != null ? m_Timestamp.hashCode() : 0;
    }

    public String GetSha1() { return m_Sha1; }

    public void updatePreviousCommitNodeControllersRec(CommitLogicNode i_FirstParent, CommitLogicNode i_SecondParent) {
        if (i_FirstParent != null) {
            m_PreviousCommitNodeControllers.add(m_MainAppFormController.getCommitNodes().get(i_FirstParent).getCommitNodeController());
            updatePreviousCommitNodeControllersRec(i_FirstParent.getFirstParent(), i_FirstParent.getSecondParent());
        }
        if (i_SecondParent != null) {
            m_PreviousCommitNodeControllers.add(m_MainAppFormController.getCommitNodes().get(i_SecondParent).getCommitNodeController());
            updatePreviousCommitNodeControllersRec(i_SecondParent.getFirstParent(), i_SecondParent.getSecondParent());
        }
    }

    private void chainResetHeadBranch(ContextMenuCommitNode i_ContextMenuCommitNode) {
        MenuItem commitFilesItem = i_ContextMenuCommitNode.createMenuItem("Reset head branch to here", (v, w) -> {
            try {
                m_MainAppFormController.getMagitLogic().resetHeadBranch(m_Sha1);
                m_MainAppFormController.createCommitTree();
                GUIUtilities.showInformationToUser("Head branch was changed successfully");
            } catch (IOException e) {
               GUIUtilities.showErrorToUser(e.toString());
            }
        }, null, null);

        i_ContextMenuCommitNode.mainMenuCreator(commitFilesItem);
    }

    private void chainCreateNewBranch(ContextMenuCommitNode i_ContextMenuCommitNode) {
        Menu newBranchMenu = new Menu("Create new branch here");
        Menu createRTBbranch = new Menu("Create remote tracking branch");
        newBranchMenu.getItems().add(createRTBbranch);
        Repository currentRepo = m_MainAppFormController.getMagitLogic().getActiveRepository();

        //////////////////////////////////////////////////////////////////////////////////
        /*create new rtb branch*/
        //////////////////////////////////////////////////////////////////////////////////
        List<Branch> RBonCommit = currentRepo.getBranches().values().stream().filter(v -> v.getPointedCommitId().equals(m_Sha1) && v.getIsRemote()).collect(Collectors.toList());

        if(RBonCommit.isEmpty()){
            createRTBbranch.setDisable(true);
        }

        for (Branch RB : RBonCommit) {
            List<String> remoteBranchesNamesList = new ArrayList<>();
            remoteBranchesNamesList.add(RB.getName());
            MenuItem RBMenuItem = new MenuItem(RB.getName());
            String[] rbSpliter = RB.getName().split("\\\\");
            String rtbName = rbSpliter[rbSpliter.length - 1];
            if(currentRepo.getBranches().containsKey(rtbName)){
                RBMenuItem.setDisable(true);
            }
            RBMenuItem.setOnAction((e) -> {
                try {
                    m_MainAppFormController.getMagitLogic().createRemoteTrackingBranchInFileSystemAndInOurObjects(rtbName, RB.getPointedCommitId(), RB.getName());
                    GUIUtilities.showInformationToUser("The branch added successfully");
                    m_MainAppFormController.createCommitTree();
                } catch (IOException ex) {
                   GUIUtilities.showErrorToUser(ex.toString());
                }
            });
            createRTBbranch.getItems().add(RBMenuItem);
        }

        //////////////////////////////////////////////////////////////////////////////////
        /*create new local branch*/
        //////////////////////////////////////////////////////////////////////////////////
        MenuItem createLocalBranch = i_ContextMenuCommitNode.createMenuItem("Create new local branch here", (v, w) -> {
            String branchName = GUIUtilities.getTextInput("Create new branch", "Please insert branch name:", "Name: ", "");

            if (branchName.isEmpty() || branchName == null) {
                GUIUtilities.showErrorToUser("Invalid input! You can't enter empty name");
            } else if (m_MainAppFormController.getMagitLogic().isBranchExistsInFileSystem(branchName)) {
                GUIUtilities.showErrorToUser("This branch already exists");
            } else{
                try {
                    m_MainAppFormController.getMagitLogic().createNewBranchInFileSystemAndInOurObjects(branchName, m_Sha1);
                    GUIUtilities.showInformationToUser("The branch added successfully");
                    m_MainAppFormController.createCommitTree();
                } catch(Exception e){
                    GUIUtilities.showErrorToUser(e.toString());
                }
            }
        }, null, null);
        newBranchMenu.getItems().add(createLocalBranch);
        i_ContextMenuCommitNode.mainMenuCreator(newBranchMenu);
    }

    private void chainMerge(ContextMenuCommitNode i_ContextMenuCommitNode) {
        Menu merge = new Menu("Merge with Head");
        Repository currentRepo = m_MainAppFormController.getMagitLogic().getActiveRepository();
        List<Branch> allBranchesOnCommit = currentRepo.getBranches().values().stream().filter(v -> v.getPointedCommitId().equals(m_Sha1) && !(v.getIsRemote()) && v != currentRepo.getHeadBranch()).collect(Collectors.toList());

        if(allBranchesOnCommit.isEmpty()){
            merge.setDisable(true);
        }

        for (Branch branch : allBranchesOnCommit) {
            MenuItem branchToAdd = new MenuItem(branch.getName());
            branchToAdd.setOnAction((e) -> {
                try {
                   m_MainAppFormController.handleConflicts(m_MainAppFormController.getMagitLogic().merge(branch), branch.getName());
                   m_MainAppFormController.createCommitTree();
                } catch(IOException ex){
                    System.out.println(ex.toString());
                }
            });
            merge.getItems().add(branchToAdd);
        }
        i_ContextMenuCommitNode.mainMenuCreator(merge);
    }

    private void chainDeleteBranch(ContextMenuCommitNode i_ContextMenuCommitNode) {
        Menu deleteBranch = new Menu("Delete branch");
        Repository currentRepo = m_MainAppFormController.getMagitLogic().getActiveRepository();
        List<Branch> allBranchesOnCommit = currentRepo.getBranches().values().stream().filter(v -> v.getPointedCommitId().equals(m_Sha1) && v != currentRepo.getHeadBranch() && !(v.getIsRemote())).collect(Collectors.toList());

        if(allBranchesOnCommit.isEmpty()){
            deleteBranch.setDisable(true);
        }

        for (Branch branch : allBranchesOnCommit) {
            MenuItem branchToDelete = new MenuItem(branch.getName());
            branchToDelete.setOnAction((e) -> {
                try {
                    m_MainAppFormController.getMagitLogic().deleteBranch(branch.getName());
                    GUIUtilities.showInformationToUser("The branch deleted successfully");
                    m_MainAppFormController.createCommitTree();
                } catch (IOException ex) {
                    GUIUtilities.showErrorToUser(ex.toString());
                }
            });

            deleteBranch.getItems().add(branchToDelete);
        }
        i_ContextMenuCommitNode.mainMenuCreator(deleteBranch);
    }
}