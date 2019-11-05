package logic;

public class Branch {
    private String m_Name;
    private String m_PointedCommitId;
    private boolean m_IsHead = false;
    private String m_TrackingAfter = null;
    private boolean m_IsRemote = false;
    private boolean m_Tracking = false;


    public Branch(String i_Name, String i_PointedCommitId, String i_TrackingAfter, boolean i_IsRemote, boolean i_Tracking){
        m_Name = i_Name;
        m_PointedCommitId = i_PointedCommitId;
        m_TrackingAfter = i_TrackingAfter;
        m_IsRemote = i_IsRemote;
        m_Tracking = i_Tracking;
    }

    public Branch(String i_Name, String i_PointedCommitId, boolean i_IsHead){
        m_Name = i_Name;
        m_PointedCommitId = i_PointedCommitId;
        m_IsHead = i_IsHead;
    }

    public Branch(String i_Name, String i_PointedCommitId){
        m_Name = i_Name;
        m_PointedCommitId = i_PointedCommitId;
    }

    public String getName() {
        return m_Name;
    }

    public String getPointedCommitId() {
        return m_PointedCommitId;
    }

    public boolean getIsHead() {
        return m_IsHead;
    }

    public String getTrackingAfter() {
        return m_TrackingAfter;
    }

    public boolean getIsRemote() {
        return m_IsRemote;
    }

    public boolean getIsTracking() {
        return m_Tracking;
    }

    public void setTrackingAfter(String i_TrackingAfter) {
        this.m_TrackingAfter = i_TrackingAfter;
    }

    public void setIsRemote(boolean i_IsRemote) {
        this.m_IsRemote = i_IsRemote;
    }

    public void setTracking(boolean i_Tracking) {
        this.m_Tracking = i_Tracking;
    }

    public void setIsHead(boolean i_IsHead) {
        this.m_IsHead = i_IsHead;
    }

    public void setPointedCommitId(String i_PointedCommitId) {
        this.m_PointedCommitId = i_PointedCommitId;
    }

    public void setName(String i_Name) {
        this.m_Name = i_Name;
    }
}
