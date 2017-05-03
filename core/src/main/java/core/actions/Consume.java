package core.actions;

import core.LifeAgent;
import core.interfaces.Consumable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Consume extends Action<LifeAgent> {

    List<Consumable> consumables;

    public Consume(LifeAgent lifeAgent, List<Consumable> consumables) {
        super(lifeAgent);
        this.consumables =  consumables;
    }

    public Consume(LifeAgent lifeAgent, Consumable consumable) {
        super(lifeAgent);
        consumables = new ArrayList<>();
        consumables.add(consumable);
    }

    public Iterator<Consumable> getConsumables() {
        return consumables.iterator();
    }

    @Override
    public String toString() {
        return String.format("[Consume(%s): %s", getAgent(), consumables);
    }
}