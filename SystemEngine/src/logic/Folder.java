package logic;

import java.text.SimpleDateFormat;
import java.util.*;

public class Folder{
    private final List<ItemData> m_Items = new ArrayList<>();
    private boolean m_IsRoot;

    public Folder(boolean i_IsRoot) {
        m_IsRoot = i_IsRoot;
    }

    public List<ItemData> getItems() {
        return m_Items;
    }

    public boolean getIsRoot() {
        return m_IsRoot;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> itemsDataAsStrings = new ArrayList<>();

        for (ItemData item : m_Items) {
            itemsDataAsStrings.add(item.toString());
        }

        Collections.sort(itemsDataAsStrings);

        for (String itemDataAsString : itemsDataAsStrings) {
            sb.append(itemDataAsString);
            sb.append(System.lineSeparator());
        }

        int last = sb.lastIndexOf("\r\n");
        if (last >= 0) {
            sb.delete(last, sb.length());
        }

        return sb.toString();
    }

    public static class ItemData {
        private String m_Name;
        private String m_Id;
        private eItemType m_Type;
        private String m_LastUpdater;
        private String m_LastUpdateDate;

        public enum eItemType{
            BLOB, FOLDER
        }

        public ItemData(){}

        public ItemData(String i_Name, String i_Id, eItemType i_Type, String i_LastUpdater, String i_LastUpdateDate){
            m_Name = i_Name;
            m_Id = i_Id;
            m_Type = i_Type;
            m_LastUpdater = i_LastUpdater;
            m_LastUpdateDate = i_LastUpdateDate;
        }

        public ItemData(String i_Name, String i_Id, eItemType i_Type, String i_LastUpdater){
            m_Name = i_Name;
            m_Id = i_Id;
            m_Type = i_Type;
            m_LastUpdater = i_LastUpdater;
            m_LastUpdateDate = (new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS").format(new Date()));
        }

        public String getName() {
            return m_Name;
        }

        public void setName(String m_Name) {
            this.m_Name = m_Name;
        }

        public String getId() {
            return m_Id;
        }

        public void setId(String m_Id) {
            this.m_Id = m_Id;
        }

        public eItemType getType() {
            return m_Type;
        }

        public void setType(eItemType m_Type) {
            this.m_Type = m_Type;
        }

        public String getLastUpdater() {
            return m_LastUpdater;
        }

        public void setLastUpdater(String lastUpdater) {
            m_LastUpdater = lastUpdater;
        }

        public String getLastUpdateDate() {
            return m_LastUpdateDate;
        }

        public void setLastUpdateDate(String lastUpdateDate) {
            m_LastUpdateDate = lastUpdateDate;
        }

        public String toString() {
            return String.format("%s;%s;%s;%s;%s", m_Name, m_Id, m_Type.toString().toLowerCase(), m_LastUpdater, m_LastUpdateDate);
        }

        public String toStringForConsole() {
            return String.format("%s, %s, %s, %s, %s", m_Name, "SHA-1: " + m_Id, "Type: " + m_Type.toString().toLowerCase(), "Last updater: " + m_LastUpdater, "Last update date: " + m_LastUpdateDate + "\n");
        }
    }
}
