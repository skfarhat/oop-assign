package core;

import core.actions.Action;
import core.actions.Consume;
import core.actions.Move;
import core.actions.Reproduce;
import core.exceptions.AgentIsDeadException;
import core.exceptions.GridCreationException;
import core.exceptions.InvalidPositionException;
import core.exceptions.LifeException;

import java.util.*;

/**
 * @class Life class created for each simulation to step. The class captures user input and configures the system parameters.
 *
 * Initially Life was designed to be a singleton class, since intuitively only one life should exist.
 * But it was later noted that we may want to simulate multiple lives at the same time concurrently, which
 * would be impractical (impossible?) with singletons. The design was then changed.
 */
public class Life implements LifeGetter {

    // ===========================================================================================
    // List of keys in the params map
    // ===========================================================================================

    public static final String KEY_MAX_ITERATIONS = "MAX_ITERATIONS";
    public static final String KEY_GRID_COLS = "GRID_COLS";
    public static final String KEY_GRID_ROWS = "GRID_ROWS";
    public static final String KEY_E_GRASS_INITIAL = "E_GRASS_INITIAL";
    public static final String KEY_E_DEER_INITIAL = "E_DEER_INITIAL";
    public static final String KEY_E_WOLF_INITIAL = "E_WOLF_INITIAL";
    public static final String KEY_E_DEER_GAIN = "E_DEER_GAIN";
    public static final String KEY_E_WOLF_GAIN = "E_WOLF_GAIN";
    public static final String KEY_E_STEP_DECREASE = "E_STEP_DECREASE";
    public static final String KEY_R_GRASS = "R_GRASS";
    public static final String KEY_R_DEER = "R_DEER";
    public static final String KEY_R_WOLF = "R_WOLF";
    public static final String KEY_I_GRASS = "I_GRASS";
    public static final String KEY_I_DEER = "I_DEER";
    public static final String KEY_I_WOLF = "I_WOLF";

    // ===========================================================================================
    // Defaults
    // ===========================================================================================

    /** @brief default maximum number of iterations, a negative value means run indefinitely */
    public static final int DEFAULT_MAX_ITERATIONS = -1;

    /** @brief default energy gained by wolf and deer when they consume other agents */
    public static final int DEFAULT_GRID_N = 10;

    /** @brief default energy gained by wolf and deer when they consume other agents */
    public static final int E_DEFAULT_GAIN = 2;

    /** @brief default energy decrease for Agents when they age */
    public static final int E_DEFAULT_DECREASE = 1;

    /** @brief default initial agent energy */
    public static final int E_DEFAULT_INITIAL = 10;

    /** @brief default random schedule frequency - how often are you likely to be selected to act by the scheduler */
    public static final double R_DEFAULT = 0.33;

    /** @brief default initial number of instance of an agent type */
    public static final int I_DEFAULT = 5;

    // ===========================================================================================
    // Params
    // ===========================================================================================

    /**
     * @brief map determining which typ of LifeAgent can consume which type,
     * e.g.  Wolf.class -->  [Deer.class, Sheep.class...]
     */
    private final Map<Class, List<Class>> CONSUME_RULES = new HashMap<Class, List<Class>>();

    public final int GRID_COLS;
    public final int GRID_ROWS;
    public final int E_GRASS_INITIAL;
    public final int E_DEER_INITIAL;
    public final int E_WOLF_INITIAL;
    public final int E_DEER_GAIN;
    public final int E_WOLF_GAIN;
    public final int E_STEP_DECREASE;
    public final double R_GRASS;
    public final double R_DEER;
    public final double R_WOLF;
    public final int I_GRASS;
    public final int I_DEER;
    public final int I_WOLF;

    /** @brief the maximum number of iterations - max number of times step is called, a negative means run indefinitely */
    public final int maxIterations;
    private int iteration;

    // ===========================================================================================
    // MEMBER VARIABLES
    // ===========================================================================================

    /** @brief the grid containing all cells on which the agents will be placed */
    private final Grid<LifeCell> grid;

    /** @brief list of all of the agents in Life */
    private final List<Agent> agents;

    // ===========================================================================================
    // METHODS
    // ===========================================================================================

    /** @brief default constructor, calls other constructor and initialises fields to their defaults */
    public Life() throws LifeException, IllegalArgumentException { this(null);}

    // AgentIsDeadException --> invalid initial energy
    // InvalidPositionException --> distribution of the agents failed.
    /** @brief constructor taking in a params map specifying the input parameters */
    public Life(Map<String, Number> params) throws LifeException, IllegalArgumentException {

        // Consume Rules: dictate who is allowed to consume whom
        CONSUME_RULES.put(Wolf.class, new ArrayList<Class>(){{add(Deer.class );}}); // Wolf eats Deer
        CONSUME_RULES.put(Deer.class, new ArrayList<Class>(){{add(Grass.class);}}); // Deer eats Grass

        // if the params map passed is null, we assume the user has no input parameters, we create an empty map
        // and the below code will step and set all fields to their defaults
        if (params == null) params= new HashMap<>();

        maxIterations = params.containsKey(KEY_MAX_ITERATIONS)? params.get(KEY_MAX_ITERATIONS).intValue() : DEFAULT_MAX_ITERATIONS;
        exceptionIfNegative(GRID_COLS = params.containsKey(KEY_GRID_COLS)? params.get(KEY_GRID_COLS).intValue() : DEFAULT_GRID_N);
        exceptionIfNegative(GRID_ROWS = params.containsKey(KEY_GRID_ROWS)? params.get(KEY_GRID_ROWS).intValue() : DEFAULT_GRID_N);
        exceptionIfNegative(E_GRASS_INITIAL = params.containsKey(KEY_E_GRASS_INITIAL)? params.get(KEY_E_GRASS_INITIAL).intValue() : E_DEFAULT_INITIAL);
        exceptionIfNegative(E_DEER_INITIAL = params.containsKey(KEY_E_DEER_INITIAL)? params.get(KEY_E_DEER_INITIAL).intValue() : E_DEFAULT_INITIAL);
        exceptionIfNegative(E_WOLF_INITIAL = params.containsKey(KEY_E_WOLF_INITIAL)? params.get(KEY_E_WOLF_INITIAL).intValue() : E_DEFAULT_INITIAL);
        exceptionIfNegative(E_DEER_GAIN = params.containsKey(KEY_E_DEER_GAIN)? params.get(KEY_E_DEER_GAIN).intValue() : E_DEFAULT_GAIN);
        exceptionIfNegative(E_WOLF_GAIN = params.containsKey(KEY_E_WOLF_GAIN)? params.get(KEY_E_WOLF_GAIN).intValue() : E_DEFAULT_GAIN);
        exceptionIfNegative(E_STEP_DECREASE = params.containsKey(KEY_E_STEP_DECREASE)? params.get(KEY_E_STEP_DECREASE).intValue() : E_DEFAULT_DECREASE);
        exceptionIfNegative(I_GRASS = params.containsKey(KEY_I_GRASS)? params.get(KEY_I_GRASS).intValue() : I_DEFAULT);
        exceptionIfNegative(I_DEER = params.containsKey(KEY_I_DEER)? params.get(KEY_I_DEER).intValue() : I_DEFAULT);
        exceptionIfNegative(I_WOLF = params.containsKey(KEY_I_WOLF)? params.get(KEY_I_WOLF).intValue() : I_DEFAULT);

        // the doubles must be in range 0-1 - we use exceptionIfOutOfRange
        exceptionIfOutOfRange(R_GRASS = params.containsKey(KEY_R_GRASS)? params.get(KEY_R_GRASS).doubleValue() : R_DEFAULT);
        exceptionIfOutOfRange(R_DEER = params.containsKey(KEY_R_DEER)? params.get(KEY_R_DEER).doubleValue() : R_DEFAULT);
        exceptionIfOutOfRange(R_WOLF = params.containsKey(KEY_R_WOLF)? params.get(KEY_R_WOLF).doubleValue() : R_DEFAULT);


        // Create Life in 7 days
        // ---------------------

        // create grid
        grid = GridLifeCellFactory.createGridCell(this.GRID_ROWS, this.GRID_COLS); // create a square Grid

        // create all agents and distribute
        agents = new ArrayList<Agent>(I_DEER+I_WOLF+I_GRASS);
        for (int i = 0; i < I_DEER; i++) agents.add(new Deer(E_DEER_INITIAL));
        for (int i = 0; i < I_WOLF; i++) agents.add(new Wolf(E_WOLF_INITIAL));
        for (int i = 0; i < I_GRASS; i++) agents.add(new Grass(E_GRASS_INITIAL));
        uniformlyDistribute(agents);
    }

    /**
     *
     * @param list list to filter from
     * @param agent agent which consumes
     * @return a list of all consumable Agents by Agent @param a
     */
    private List<Consumable> filterConsumablesForAgent(List<Consumable> list, Agent agent) {
        // list of classes that the chosen type can consume
        final List<Class> consumables =  CONSUME_RULES.get(agent.getClass());
        list.removeIf(a -> !consumables.contains(a.getClass()));
        return list;
    }

    private List<Consumable> filterConsumablesForAgent(Iterator<Consumable> it, Agent agent) {
        final List<Class> consumableClasses = CONSUME_RULES.get(agent.getClass()); // list of classes consumable by agent
        List<Consumable> consumables = new ArrayList<>();
        while(it.hasNext()){
            Consumable c = it.next();
            if (consumableClasses.contains(c.getClass()))
                consumables.add(c);
        }
        return consumables;
    }

    // TODO(sami): create the equivalent removeAgent() method and remove ticket from Trello
    private void addAgent(Agent agent) throws InvalidPositionException {
        // TODO(sami): check that the agent doesn't already exist there
        grid.get(agent.getPos()).addAgent(agent);
        agents.add(agent);
    }

    /**
     * @brief choose an agent at random to act
     * @throws InvalidPositionException
     * @throws AgentIsDeadException
     * @return the iteration index or -1 if there was nothing to do
     */
    public List<Action> step() throws InvalidPositionException, AgentIsDeadException {

        List<Action> actions = new ArrayList<>();

        // guard - nothing to do
        if (agents.size() < 1) {
            return actions;
        }

        // choose an agent at random
        int randI = Utils.randomPositiveInteger(agents.size());
        LifeAgent chosen = (LifeAgent) agents.get(randI);
        if (!chosen.isAlive())
            System.out.println("We chose a non-alive agent!"); // TODO(sami): throw an exception

        // Wolves and Deers
        if ((chosen instanceof Wolf) || (chosen instanceof Deer)) {

            // -------
            // Move
            // -------
            Point2D srcPoint = new Point2D(chosen.getPos()); // make a new copy of the src point
            Point2D nextPoint = findAdjacentPointInGrid(chosen.getPos());
            Cell nextCell = grid.get(nextPoint);
            Action move = new Move(chosen, srcPoint, nextPoint);
            actions.add(move);

            // -------
            // Consume
            // -------

            List<Consumable> consumableAgents = filterConsumablesForAgent(nextCell.getAgents(), chosen);

            // choose one at random to consume
            if (consumableAgents.size() > 0) {
                int index = Utils.randomPositiveInteger(consumableAgents.size());
                Consumable agentToConsume = consumableAgents.get(index);
                Action consume = new Consume(chosen, agentToConsume);
                actions.add(consume);
            }

            // ---------
            // Reproduce
            // ---------

            double rAgent = (chosen instanceof Wolf)? R_WOLF : R_DEER;
            boolean willReproduce = Utils.getRand().nextDouble() < rAgent;
            if (willReproduce) {
                Action reproduce = new Reproduce(chosen, chosen.reproduce());
                actions.add(reproduce);
            }

            // ---------
            // Age
            // ---------

            // decrease energy
            chosen.decreaseEnergyBy(E_STEP_DECREASE);

            processActions(actions);


            // only in the dst cell can someone die - this will remove the agents from the cell's list
            List<LifeAgent> deadAgents = ((LifeCell)nextCell).recycleDeadAgents();

            // remove the agents from the life 'agents' array
            agents.removeAll(deadAgents);

        }

        else if (chosen instanceof Grass) {
            // find an adjacent cell but don't moves
            Point2D nextPoint = findAdjacentPointInGrid(chosen.getPos());
            LifeCell nextCell = (LifeCell) grid.get(nextPoint);
            if (!nextCell.isContainsGrass()) {
                // System.out.println(chosen + " is reproducing.");
                LifeAgent newBaby = chosen.reproduce();
                nextCell.addAgent(newBaby);
                agents.add(newBaby);
            }

            processActions(actions);
        }

        iteration++;
        return actions;
    }

    private void processActions(List<Action> actions) throws InvalidPositionException, AgentIsDeadException {
        for (Action action: actions) {
            if (action instanceof Consume) {
                processConsume((Consume) action);
            }
            else if (action instanceof Reproduce) {
                processReproduce((Reproduce) action);
            }
            else if (action instanceof  Move) {
                processMoveAction((Move) action);
            }
        }
    }

    private void processMoveAction(Move action) throws InvalidPositionException {
        Cell nextCell = grid.get(action.getTo());
        grid.moveAgentToCell(action.getAgent(), nextCell);
    }

    private void processReproduce(Reproduce action) throws InvalidPositionException {
        Iterator<LifeAgent> babies = action.getBabies();
        while(babies.hasNext()) {
            LifeAgent baby = babies.next();
            addAgent(baby);
        }
    }

    private void processConsume(Consume action) throws AgentIsDeadException {
        Consumable consumable = action.getConsumables().next();
        ((Consumes) action.getAgent()).consume(consumable);
    }

    /** @brief uniformly distribute the agents on the grid */
    private void uniformlyDistribute(List<Agent> agents) throws InvalidPositionException {
        for (Agent a : agents) {
            Point2D p = Utils.randomPoint(this.GRID_ROWS, this.GRID_COLS);
            grid.get(p.getX(), p.getY()).addAgent(a);
        }
    }

    private Point2D findAdjacentPointInGrid(Point2D p) throws InvalidPositionException {
        return grid.randomAdjacentPoint(p);
    }

    /**
     * @param val that must be non-negative
     * @throws IllegalArgumentException if val is negative
     */
    private void exceptionIfNegative(int val) throws IllegalArgumentException {
        if (val < 0) throw new IllegalArgumentException();
    }

    /**
     * @param val that must be in 0-1 range
     * @throws IllegalArgumentException if val is out of the range
     */
    private void exceptionIfOutOfRange(double val) throws IllegalArgumentException {
        if (val < 0 || val > 1)
            throw new IllegalArgumentException("Double values must be between 0 and 1: " + val + " given.");
    }

    /**
     * @return
     */
    @Override
    public List<Agent> getAgents() {
        return agents;
    }

    /** @return the current iteration */
    @Override
    public int getIteration() {
        return iteration;
    }

    /** @return the maximum number of iterations that this simulation should run - this is not enforced in this class
     * but the member variable is set when parsing the options */
    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public Grid<LifeCell> getGrid() {
        return grid;
    }

    /**
     * @return number of rows in the grid
     */
    @Override
    public int getGridRows() { return grid.getRows(); }

    /**
     * @return number of columns in the grid
     */
    @Override
    public int getGridCols() { return grid.getCols(); }

    public static void main(String []args) throws LifeException {
        Life life = new Life();

        while(life.agents.size() > 0){
            life.step();
        }
    }
}
