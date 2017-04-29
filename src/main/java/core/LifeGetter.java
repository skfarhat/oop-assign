package core;

import java.util.List;

/**
 * to expose some getter methods in Life without allowing full control
 */
public interface LifeGetter {

    List<Agent> getAgents();

    int getStepCount();

    int getMaxIterations();

    LifeGrid getGrid();

    int getGridRows();

    int getGridCols();
}
