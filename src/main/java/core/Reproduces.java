package core;

import core.exceptions.AgentIsDeadException;

/**
 * Created by Sami on 28/03/2017.
 */
public interface Reproduces {

    LifeAgent reproduce() throws AgentIsDeadException;
}
