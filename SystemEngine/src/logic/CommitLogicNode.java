package logic;

import java.util.ArrayList;
import java.util.List;

public class CommitLogicNode {
    private Commit m_Commit;
    private String m_CommitId;
    private String m_pointingBranch;
    private CommitLogicNode m_FirstParent;
    private CommitLogicNode m_SecondParent;
    private List<CommitLogicNode> m_Childrens = new ArrayList<>();

    public CommitLogicNode(Commit i_Commit, String i_CommitId, String i_pointingBranch){
        m_Commit = i_Commit;
        m_CommitId = i_CommitId;
        m_pointingBranch = i_pointingBranch ;
    }

    public Commit getCommit() {
        return m_Commit;
    }

    public String getPointingBranch() {
        return m_pointingBranch;
    }

    public String getCommitId() {
        return m_CommitId;
    }

    public CommitLogicNode getFirstParent() {
        return m_FirstParent;
    }

    public CommitLogicNode getSecondParent() {
        return m_SecondParent;
    }

    public List<CommitLogicNode> getChildrens() {
        return m_Childrens;
    }

    public void setFirstParent(CommitLogicNode m_FirstParent) {
        this.m_FirstParent = m_FirstParent;
    }

    public void setSecondParent(CommitLogicNode m_SecondParent) {
        this.m_SecondParent = m_SecondParent;
    }
}
