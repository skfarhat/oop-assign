import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Sami on 28/03/2017.
 */
public class WolfTest {
    @Test
    public void testWolfIsAlive() throws AgentIsDeadException {
        Wolf wolf = new Wolf();
        assertTrue(wolf.isAlive());
    }



}