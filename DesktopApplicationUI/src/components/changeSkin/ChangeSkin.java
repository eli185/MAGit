package components.changeSkin;

import java.util.ArrayList;
import java.util.List;

public class ChangeSkin {
    private List<String> cssFiles;

    public ChangeSkin(){
        cssFiles=new ArrayList<>();
    }

    public void addCssSheet(String i_Path){
        cssFiles.add(i_Path);
    }

    public String getCurrentCss(int i_Index) {
        return cssFiles.get(i_Index);
    }
}
