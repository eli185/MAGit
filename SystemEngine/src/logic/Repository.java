package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    private String m_Name;
    private String m_Location;
    private Branch m_HeadBranch = null ;
    private final Map<String, Blob> m_Blobs = new HashMap<>();
    private final Map<String, Folder> m_Folders = new HashMap<>();
    private final Map<String, Commit> m_Commits = new HashMap<>();
    private final Map<String, Branch> m_Branches = new HashMap<>();
    private String m_RemoteRepositoryLocation = null;
    private String m_RemoteRepositoryName = null;

    public Repository(String i_Name, String i_Location, String i_RemoteRepositoryLocation) {
        m_Name = i_Name;
        m_Location = i_Location;
        m_RemoteRepositoryLocation = i_RemoteRepositoryLocation;
    }

    public Repository(String i_Name, String i_Location) {
        m_Name = i_Name;
        m_Location = i_Location;
    }

    public String getRemoteRepositoryName() {
        return m_RemoteRepositoryName;
    }

    public String getRemoteRepositoryLocation() {
        return m_RemoteRepositoryLocation;
    }

    public String getName() {
        return m_Name;
    }

    public String getLocation() {
        return m_Location;
    }

    public Map<String, Blob> getBlobs() {
        return m_Blobs;
    }

    public Map<String, Folder> getFolders() {
        return m_Folders;
    }

    public Map<String, Commit> getCommits() {
        return m_Commits;
    }

    public Map<String, Branch> getBranches() {
        return m_Branches;
    }

    public Branch getHeadBranch() {
        return m_HeadBranch;
    }

    public void setRemoteRepositoryLocation(String m_RemoteRepositoryLocation) {
        this.m_RemoteRepositoryLocation = m_RemoteRepositoryLocation;
    }

    public void setRemoteRepositoryName(String m_RemoteRepositoryName) {
        this.m_RemoteRepositoryName = m_RemoteRepositoryName;
    }

    public void setName(String i_Name) {
        this.m_Name = i_Name;
    }

    public void setHeadBranch(Branch i_HeadBranch) {
        this.m_HeadBranch = i_HeadBranch;
    }

    public void setLocation(String i_Location) {
        this.m_Location = i_Location;
    }

    public Branch isSha1OfCommitThatRemoteBranchPointingOn(String i_CommitSha1){
        Branch RemoteBranchThatPointingOnThisSha1OfCommit = null;

        for (Map.Entry<String, Branch> entry : m_Branches.entrySet()) {
            if(entry.getValue().getPointedCommitId().equals(i_CommitSha1) && entry.getValue().getIsRemote()){
                RemoteBranchThatPointingOnThisSha1OfCommit = entry.getValue();
                break;
            }
        }

        return RemoteBranchThatPointingOnThisSha1OfCommit;
    }

    public void FixItemsID() {
        List<String> commitsIdList = new ArrayList();
        List<String> foldersIdList = new ArrayList();
        List<String> blobsIdList = new ArrayList();

        for (Map.Entry<String, Commit> entryCommit : m_Commits.entrySet()) {
            if(entryCommit.getKey().length() < 40){
                commitsIdList.add(entryCommit.getKey());
            }
        }
        for (String commitId : commitsIdList) {
            m_Commits.remove(commitId);
        }

        for (Map.Entry<String, Folder> entryFolder : m_Folders.entrySet()) {
            if(entryFolder.getKey().length() < 40){
                foldersIdList.add(entryFolder.getKey());
            }
        }
        for (String folderId : foldersIdList) {
            m_Folders.remove(folderId);
        }

        for (Map.Entry<String, Blob> entryBlob : m_Blobs.entrySet()) {
            if(entryBlob.getKey().length() < 40){
                blobsIdList.add(entryBlob.getKey());
            }
        }
        for (String blobId : blobsIdList) {
            m_Blobs.remove(blobId);
        }

    }
}
