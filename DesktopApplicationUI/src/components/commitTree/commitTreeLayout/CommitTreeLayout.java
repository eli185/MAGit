package components.commitTree.commitTreeLayout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import components.commitTree.commitNode.CommitNode;
import java.util.List;
import java.util.Random;

import javafx.animation.RotateTransition;
import javafx.util.Duration;

public class CommitTreeLayout implements Layout {
    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 0;
        int every4rdNode = 1;
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
            if (every4rdNode % 4 == 0) {
                //graph.getGraphic(c).relocate(startX + 50, startY);
                startX=0;
            } else {
                //graph.getGraphic(c).relocate(startX, startY);
                startX=startX+new Random().nextInt(50);
            }
            graph.getGraphic(c).relocate(startX, startY);
            startY += 50;
            every4rdNode++;
        }
    }
}
