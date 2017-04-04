/**
 * Created by Sami on 28/03/2017.
 */

package core;
import java.util.List;

/**
 * @classs Wolf
 */
public class Wolf extends LifeAgent implements Ages, Consumes {

    /** @@brief default constructor */
    public Wolf() throws AgentIsDeadException {
        super();
    }
    public Wolf(int initialEnergy) throws AgentIsDeadException {
        super(initialEnergy);
    }

    public LifeAgent reproduce() throws AgentIsDeadException {
        Wolf babyWolf = new Wolf();
        return babyWolf;
    }

    /** @brief method from interface Ages, LifeAgent loses energy equivalent to LifeAgent.ENERGY_DECREMENT_VAL */
    public void ageBy(int val) throws AgentIsDeadException {
        decreaseEnergyBy(val);
    }

    @Override
    public void consume(Consumable consumable) throws AgentIsDeadException {
        consumable.die();
    }

    @Override
    public void consumeAll(List<Consumable> consumables) throws AgentIsDeadException {
        for (Consumable consumable : consumables)
            consume(consumable);
    }

    @Override
    public String toString() {
        return "Wolf (" + getEnergy() + ")";
    }
}