package logic;

public class FileFullPathAndItemData {
    private String m_FullPath;
    private Folder.ItemData m_ItemData;

    public FileFullPathAndItemData(String i_FullPath, Folder.ItemData i_ItemData) {
        m_ItemData = i_ItemData;
        m_FullPath = i_FullPath;
    }

    public String getFullPath() {
        return m_FullPath;
    }

    public Folder.ItemData getItemData() {
        return m_ItemData;
    }
}
