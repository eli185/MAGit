package logic;

public class MergeConflict {
    private String m_Path;
    private String m_TheirsContent;
    private String m_AncestorContent;
    private String m_OurContent;
    private String m_ResolveContent = null;


    public MergeConflict(String i_Path, String i_OurContent,String i_TheirsContent, String i_AncestorContent){
        this.m_Path = i_Path;
        this.m_AncestorContent = i_AncestorContent;
        this.m_OurContent = i_OurContent;
        this.m_TheirsContent = i_TheirsContent;
    }

    public String getPath() {
        return m_Path;
    }

    public String getTheirsContent() {
        return m_TheirsContent;
    }

    public String getAncestorContent() {
        return m_AncestorContent;
    }

    public String getOurContent() {
        return m_OurContent;
    }

    public String getResolveContent() {
        return m_ResolveContent;
    }

    public void setResolveContent(String m_ResolveContent) {
        this.m_ResolveContent = m_ResolveContent;
    }
}
