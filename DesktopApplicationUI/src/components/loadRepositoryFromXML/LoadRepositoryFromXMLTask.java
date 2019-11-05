package components.loadRepositoryFromXML;
import javafx.concurrent.Task;
import logic.Magit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LoadRepositoryFromXMLTask extends Task<Void> {
    private final long SLEEP_TIME = 500;
    private String m_XmlFileName;
    private Runnable m_OnSuccess;
    private Consumer<List<String>> m_OnFailed;
    private Magit m_Magit;

    public LoadRepositoryFromXMLTask(Magit i_Magit, String i_XmlFileName, Runnable i_OnSuccess, Consumer<List<String>> i_OnFailed) {
        this.m_XmlFileName = i_XmlFileName;
        this.m_OnSuccess = i_OnSuccess;
        this.m_OnFailed = i_OnFailed;
        this.m_Magit = i_Magit;
    }

    private Void returnErrors(List<String> i_Errors) {
        this.m_OnFailed.accept(i_Errors);
        return null;
    }

    @Override
    protected Void call() throws Exception {
        boolean isXmlValid;
        List<String> errors = new ArrayList<>();

        updateMessage("Fetching file...");
        updateProgress(0, 1);
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) {
        }

        updateMessage("Checking validation...");
        updateProgress(0.33, 1);
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) {
        }
        isXmlValid = m_Magit.isXMLValid(m_XmlFileName, errors);

        if (!isXmlValid) {
            return returnErrors(errors);
        }

        updateMessage("Generating repository...");
        updateProgress(0.66, 1);
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) {
        }

        m_OnSuccess.run();
        while(m_Magit.getTaskFlag()){
        }

        updateMessage("Done...");
        updateProgress(1, 1);
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) {
        }

        m_Magit.setTaskFlag(true);

        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Cancelled!");
    }
}