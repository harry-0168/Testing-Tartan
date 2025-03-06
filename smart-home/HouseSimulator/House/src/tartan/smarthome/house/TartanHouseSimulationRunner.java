package tartan.smarthome.house;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class TartanHouseSimulationRunner {

    private List<TartanHouseSimulator> simulators;
    private LocalDateTime simulationTime;
    private Random random;

    public TartanHouseSimulationRunner() {
        this.simulators = new ArrayList<>();
        // Pick a simulated start date/time
        this.simulationTime = LocalDateTime.of(2025, 3, 1, 0, 0);
        this.random = new Random();
    }

    /**
     * Initialize N simulators, each on a different port.
     * Adjust the base port (5050) if needed.
     */
    public void initSimulators(int count) {
        int basePort = 5050; 
        for (int i = 0; i < count; i++) {
            int port = basePort + i;
            TartanHouseSimulator simulator = new TartanHouseSimulator(port);
            // Start listening on each port (accepts connections from your Tartan system)
            simulator.runSimulator();
            simulators.add(simulator);
        }
    }

    /**
     * Simulate 30 days in hourly steps: 30 days * 24 hours = 720 iterations.
     */
    public void runSimulationOneMonth() {
        int totalDays = 30;
        for (int day = 0; day < totalDays; day++) {
            for (int hour = 0; hour < 24; hour++) {
                simulationTime = simulationTime.plusHours(1);

                // For each house, do occupant actions
                for (TartanHouseSimulator sim : simulators) {
                    System.out.println("Simulating for house on port " + sim.getPort());
                    simulateOneHourStep(sim);
                }
            }
        }
        System.out.println("Simulation complete for one month!");
    }

    /**
     * Decide occupant behavior for a single hour, then call setState on the simulator.
     */
    private void simulateOneHourStep(TartanHouseSimulator sim) {
        Hashtable<String, Object> newState = new Hashtable<>();

        // e.g. occupant randomly arrives or leaves
        boolean occupantArrives = (random.nextDouble() < 0.1); // 10% chance each hour
        boolean occupantLeaves = (random.nextDouble() < 0.1);  // 10% chance each hour

        // You could track occupantHome in a map or inside TartanHouseSimulator. 
        // For simplicity, let's just say occupant toggles arrival or leave at random:

        // // If occupant arrives, set door open and ARRIVING_PROXIMITY_STATE = true
        // if (occupantArrives) {
        //     newState.put(sim.ARRIVING_PROXIMITY_STATE, true);
        //     newState.put(sim.DOOR_STATE, true);
        // }

        // // If occupant leaves, door is closed, ARRIVING_PROXIMITY_STATE = false
        // if (occupantLeaves) {
        //     newState.put(sim.ARRIVING_PROXIMITY_STATE, false);
        //     newState.put(sim.DOOR_STATE, false);
        // }

        // Maybe we turn on the light at night if occupant is home
        int currentHour = simulationTime.getHour();
        boolean isNight = (currentHour >= 20 || currentHour < 6);
        // If occupant is home, random chance of lights on at night
        // (You could store occupant state to determine "home or not")
        if (isNight && occupantArrives) {
            newState.put(sim.getLightState(), true);
        } else if (!isNight && occupantArrives) {
            // occupant might leave lights off in daytime?
            newState.put(sim.getLightState(), false);
        }

        // If occupant leaves, lights off
        if (occupantLeaves) {
            newState.put(sim.getLightState(), false);
        }

        // Additional logic for lock, intruder sensors, etc. 
        // Or pass "currentTime" if your system needs it.

        if (!newState.isEmpty()) {
            sim.setState(newState);
            // Optionally read back the updated state
            Hashtable<String, Object> confirmed = sim.getState();
            // Log or track the "confirmed" if desired
        }
    }

    public static void main(String[] args) {
        TartanHouseSimulationRunner runner = new TartanHouseSimulationRunner();
        // Example: run 5 houses in simulation, or 100 if you prefer
        System.out.println("Starting simulation for houses...");
        runner.initSimulators(2);
        System.out.println("Running simulation for one month...");
        runner.runSimulationOneMonth();
    }
}
