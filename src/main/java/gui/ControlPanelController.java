package gui;

import core.AgentIsDeadException;
import core.GridCreationException;
import core.InvalidPositionException;
import core.Life;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Created by Sami on 04/04/2017.
 */
public class ControlPanelController implements Initializable {

    public final String START_BUTTON_TEXT1 = "Start";
    public final String START_BUTTON_TEXT2 = "Pause";

    private LifeStarter lifeStarter;

    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private TextField frequencyTextField;
    @FXML private TextField maxIterationsTextField;
    @FXML private TextField rGrassTextField;
    @FXML private TextField rDeerTextField;
    @FXML private TextField rWolfTextField;
    @FXML private TextField iGrassTextField;
    @FXML private TextField iDeerTextField;
    @FXML private TextField iWolfTextField;
    @FXML private TextField gWolfTextField;
    @FXML private TextField gDeerTextField;
    @FXML private TextField gGrassTextField;
    @FXML private TextField stepDecreaseTextField;
    @FXML private TextField colsTextField;
    @FXML private TextField rowsTextField;
    @FXML private Slider frequencySlider;

    public ControlPanelController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        frequencySlider.valueChangingProperty().addListener(event -> {
            frequencySliderDone();
        });
    }

    public void startButtonPressed(ActionEvent actionEvent) {
        try {
            Map<String, Number> options = getOptions();

            // user wants to start a new simulation
            if (State.STOPPED == lifeStarter.getState()) {
                Life life = new Life(options);

                if (lifeStarter.start(life))
                    setStartedState();
            }
            // user wants to continue the paused simulation
            else if (State.PAUSED == lifeStarter.getState()) {
                lifeStarter.unpause();
                setStartedState();
            }
            // user wants to pause the current simulation
            else if (State.STARTED == lifeStarter.getState()) {
                if (lifeStarter.pause())
                    setPausedState();
            }
        }
        // TODO(sami): provide UIAlerts for the user s
        catch(NumberFormatException exc) {
            System.out.println("exception mate: " + exc.getMessage());
            exc.printStackTrace();
        } catch (GridCreationException e) {
            e.printStackTrace();
        } catch (InvalidPositionException e) {
            e.printStackTrace();
        } catch (AgentIsDeadException e) {
            e.printStackTrace();
        }
    }

    public void stopButtonPressed(ActionEvent actionEvent) {
        if (lifeStarter.stop())
            setStoppedState();
    }

    /** @brief change the name (and other properties?) appearing on the start button */
    private void changeButtonsState(State state) {
        switch(state) {
            case STOPPED:
                stopButton.setDisable(true);
            case PAUSED:
                startButton.setText(START_BUTTON_TEXT1);
                break;
            case STARTED:
                startButton.setText(START_BUTTON_TEXT2);
                stopButton.setDisable(false);
            default:
                break;
        }
    }

    private void setPausedState() { changeButtonsState(lifeStarter.getState()); }
    private void setStartedState() {
        changeButtonsState(lifeStarter.getState());
    }
    private void setStoppedState() { changeButtonsState(lifeStarter.getState()); }

    /**
     * @brief alert the user about the unparsable string
     * @param txt that is expected to be a number
     * @return the parsed number
     */
    private int alertIfNotInteger(String txt) {
        try { return Integer.parseInt(txt); }
        catch(NumberFormatException exc) {
            // show alert here
            throw exc;
        }
    }
    /**
     * @brief alert the user about the unparsable string
     * @param txt that is expected to be a number
     * @return the parsed number
     */
    private double alertIfNotDouble(String txt) {
        try { return Double.parseDouble(txt); }
        catch(NumberFormatException exc) {
            // show alert here
            throw exc;
        }
    }
    /**
     * @param val that must be non-negative
     * @throws IllegalArgumentException if val is negative
     * @return the passed number
     */
    private double exceptionIfNegative(double val) throws IllegalArgumentException {
        if (val < 0) throw new IllegalArgumentException();
        return val;
    }
    /**
     * @param val that must be non-negative
     * @throws IllegalArgumentException if val is negative
     * @return the passed number
     */
    private int exceptionIfNegative(int val) throws IllegalArgumentException {
        if (val < 0) throw new IllegalArgumentException();
        return val;
    }

    /**
     * @param val that must be in 0-1 range
     * @throws IllegalArgumentException if val is out of the range
     * @return the passed number
     */
    private double exceptionIfDoubleOutOfRange(double val) throws IllegalArgumentException {
        if (val < 0 || val > 1)
            throw new IllegalArgumentException("Double values must be between 0 and 1: " + val + " given.");
        return val;
    }

    public Map<String, Number> getOptions() throws NumberFormatException {
        Map<String, Number> options = new HashMap<>();

        try {
            lifeStarter.setFrequency(exceptionIfNegative(Double.parseDouble(frequencyTextField.getText())));

            // general params
            int cols = exceptionIfNegative(alertIfNotInteger(colsTextField.getText()));
            int rows = exceptionIfNegative(alertIfNotInteger(rowsTextField.getText()));
            int maxIterations = exceptionIfNegative(alertIfNotInteger(maxIterationsTextField.getText()));
            int stepDecrease = exceptionIfNegative(alertIfNotInteger(stepDecreaseTextField.getText()));
            options.put(Life.KEY_GRID_COLS, cols);
            options.put(Life.KEY_GRID_ROWS, rows);
            options.put(Life.KEY_E_STEP_DECREASE, stepDecrease);

            // initial count params
            int iWolf = exceptionIfNegative(alertIfNotInteger(iWolfTextField.getText()));
            int iDeer = exceptionIfNegative(alertIfNotInteger(iDeerTextField.getText()));
            int iGrass = exceptionIfNegative(alertIfNotInteger(iGrassTextField.getText()));
            options.put(Life.KEY_I_WOLF, iWolf);
            options.put(Life.KEY_I_DEER, iDeer);
            options.put(Life.KEY_I_GRASS, iGrass);

            // reproduction params
            double rWolf = exceptionIfDoubleOutOfRange(alertIfNotDouble(rWolfTextField.getText()));
            double rDeer = exceptionIfDoubleOutOfRange(alertIfNotDouble((rDeerTextField.getText())));
            double rGrass = exceptionIfDoubleOutOfRange(alertIfNotDouble((rGrassTextField.getText())));
            options.put(Life.KEY_R_WOLF, rWolf);
            options.put(Life.KEY_R_DEER, rDeer);
            options.put(Life.KEY_R_GRASS, rGrass);

            // gain params
            int gDeer = exceptionIfNegative(alertIfNotInteger(gDeerTextField.getText()));
            int gWolf = exceptionIfNegative(alertIfNotInteger(gWolfTextField.getText()));
            // int gGrass = alertIfNotInteger(gGrassTextField.getText()); // disabled
            options.put(Life.KEY_E_DEER_GAIN, gDeer);
            options.put(Life.KEY_E_WOLF_GAIN, gWolf);

            return options;
        } catch (NumberFormatException exc) {
            // let's run over the test fields and highlight the ones that are mis-formatted
//            return null;
            System.out.println(exc.getMessage());
            throw exc;
        }
    }

    public void setLifeStarter(LifeStarter lifeStarter) {
        this.lifeStarter = lifeStarter;
    }

    public void frequencySliderDone() {
//        if (frequencySlider == null) // guard
//            return;

        System.out.println(frequencySlider.getValue());
        // change the frequency using the interface LifeStarter
        double val = frequencySlider.getValue()/100.0f;

        // set the frequency as percent
        lifeStarter.setFrequency(val);

        // get the actual value of the frequency
        frequencyTextField.setText(Double.toString(lifeStarter.getFrequency()));
    }
}
