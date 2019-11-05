package logic;
import org.apache.commons.codec.digest.DigestUtils;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Commit implements CommitRepresentative {
    private String m_RootFolderId;
    private String m_FirstPrecedingCommitId;
    private String m_SecondPrecedingCommitId = "";
    private String m_Message;
    private String m_Author;
    private String m_DateOfCreation;

    public Commit(String i_RootFolderId, String i_PrecedingCommitId1, String i_Message, String i_Author){
        m_RootFolderId = i_RootFolderId;
        if(i_PrecedingCommitId1 == null || i_PrecedingCommitId1.equals("null")){
            m_FirstPrecedingCommitId = "";
        }
        else{
            m_FirstPrecedingCommitId = i_PrecedingCommitId1;
        }

        m_Message = i_Message;
        m_Author = i_Author;
        m_DateOfCreation = (new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS").format(new Date()));
    }

    public Commit(String i_RootFolderId, String i_PrecedingCommitId1, String i_Message, String i_Author, String i_DateOfCreation){
        m_RootFolderId = i_RootFolderId;
        if(i_PrecedingCommitId1 == null || i_PrecedingCommitId1.equals("null")){
            m_FirstPrecedingCommitId = "";
        }
        else{
            m_FirstPrecedingCommitId = i_PrecedingCommitId1;
        }

        m_Message = i_Message;
        m_Author = i_Author;
        m_DateOfCreation = i_DateOfCreation;
    }

    public Commit(String i_RootFolderId, String i_PrecedingCommitId1, String i_PrecedingCommitId2, String i_Message, String i_Author, String i_DateOfCreation){
        m_RootFolderId = i_RootFolderId;
        if(i_PrecedingCommitId1 == null || i_PrecedingCommitId1.equals("null")){
            m_FirstPrecedingCommitId = "";
        }
        else{
            m_FirstPrecedingCommitId = i_PrecedingCommitId1;
        }

        if(i_PrecedingCommitId2 == null || i_PrecedingCommitId2.equals("null")){
            m_SecondPrecedingCommitId = "";
        }
        else{
            m_SecondPrecedingCommitId = i_PrecedingCommitId2;
        }

        m_Message = i_Message;
        m_Author = i_Author;

        if(i_DateOfCreation != null) {
            m_DateOfCreation = i_DateOfCreation;
        }
    }

    public String getRootFolderId() {
        return m_RootFolderId;
    }

    public String getFirstPrecedingCommitId() {
        return m_FirstPrecedingCommitId;
    }

    public String getSecondPrecedingCommitId() {
        return m_SecondPrecedingCommitId;
    }

    public String getMessage() {
        return m_Message;
    }

    public String getAuthor() {
        return m_Author;
    }

    public String getDateOfCreation() {
        return m_DateOfCreation;
    }

    public void setRootFolderId(String i_RootFolderId) {
        this.m_RootFolderId = i_RootFolderId;
    }

    public void setFirstPrecedingCommitId(String i_FirstPrecedingCommitId) {
        this.m_FirstPrecedingCommitId = i_FirstPrecedingCommitId;
    }

    public void setSecondPrecedingCommitId(String i_SecondPrecedingCommitId) {
        this.m_SecondPrecedingCommitId = i_SecondPrecedingCommitId;
    }

    public String toString() {
        return String.format("%s;%s;%s;%s;%s;%s", m_RootFolderId , m_FirstPrecedingCommitId, m_SecondPrecedingCommitId, m_Message, m_Author, m_DateOfCreation);
    }

    public String toStringForSha1() {
        return String.format("%s;%s;%s;%s", m_RootFolderId , m_FirstPrecedingCommitId, m_SecondPrecedingCommitId, m_Message);
    }

    public String toStringForConsole() {
        return String.format(" %s, %s, %s", "Message: " + m_Message, "Date of creation: " + m_DateOfCreation, "Author: " + m_Author + "\n");
    }

    @Override
    public String getSha1() {
        return DigestUtils.sha1Hex(this.toStringForSha1());
    }

    @Override
    public String getFirstPrecedingSha1() {
        return m_FirstPrecedingCommitId;
    }

    @Override
    public String getSecondPrecedingSha1() {
        return m_SecondPrecedingCommitId;
    }
}
