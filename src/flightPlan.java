import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.util.Stack;

public class flightPlan {
    public static void main(String[] args) throws FileNotFoundException {
        FlightPathsGraph graph = createGraph(args[0]);
        //graph.displayGraph();
        displayBestPaths(graph, args[1]);

    }

    private static FlightPathsGraph createGraph(String fileName) throws FileNotFoundException {
        FlightPathsGraph graph = new FlightPathsGraph();
        String[] inputData;
        String startCityName;
        String destinationCityName;
        double cost;
        int time;
        Scanner file = new Scanner(new File(fileName));
        int rowsRemaining = file.nextInt();
        file.nextLine();

        while(file.hasNextLine() && rowsRemaining > 0) {
            inputData = file.nextLine().split("\\|");
            startCityName = inputData[0];
            destinationCityName = inputData[1];
            cost = Double.parseDouble(inputData[2]);
            time = Integer.parseInt(inputData[3]);
            graph.createConnection(startCityName, destinationCityName, cost, time);
            rowsRemaining--;
        }

        file.close();
        return graph;
    }

    private static void displayBestPaths(FlightPathsGraph graph, String fileName) throws FileNotFoundException {
        String[] inputData;
        String startCityName;
        String destinationCityName;
        String sortType;
        int flightCount = 1;
        Scanner file = new Scanner(new File(fileName));
        int rowsRemaining = file.nextInt();
        file.nextLine();

        while(file.hasNextLine() && rowsRemaining > 0) {
            inputData = file.nextLine().split("\\|");
            startCityName = inputData[0];
            destinationCityName = inputData[1];
            sortType = inputData[2];
            System.out.print("Flight " + flightCount + ": " + startCityName + ", " + destinationCityName
                    + " ");

            if(sortType.equalsIgnoreCase("T")) {
                System.out.print("(Time)\n");
            }
            else{
                System.out.print("(Cost)\n");

            }

            FlightPath[] sortedPaths = sortPaths(getAllPaths(graph, startCityName, destinationCityName), sortType);
            //ArrayList<FlightPath> paths = getAllPaths(graph, startCityName, destinationCityName);
            /*for(FlightPath path: paths) {
                path.displayPath();
            }*/

            if(sortedPaths.length == 0) {
                System.out.println("Error: No possible path to get from "
                        + startCityName + " to " + destinationCityName);
            }
            else {
                int i = 1;
                while (i < 4 && i < sortedPaths.length) {
                    System.out.print("Path " + i + ": ");
                    sortedPaths[i].displayPath();
                    i++;
                }
            }
            flightCount++;
            rowsRemaining--;
        }
    }

    private static ArrayList<FlightPath> getAllPaths(FlightPathsGraph graph,
                                            String startCityName, String destinationCityName) {
        ArrayList<FlightPath> paths = new ArrayList<>();
        City currentCity = graph.getCity(startCityName);
        Stack<City> stack = new Stack<>();
        boolean firstIteration = true;
        FlightPath path = new FlightPath();

        while(!stack.isEmpty() || firstIteration){
            firstIteration = false;
            if(currentCity.getName().equals(destinationCityName)) {
                path.addCity(currentCity);
                FlightPath copy = path.createCopy();
                paths.add(copy);
                path.removeCity(currentCity);
                currentCity = stack.peek();
                continue;
            }
            else if(!stack.contains(currentCity)) {
                stack.push(currentCity);
                if(!path.contains(currentCity))
                    path.addCity(currentCity);
                if(currentCity.getLastConnectionVisited() + 1 < currentCity.getNumConnections()) {
                    currentCity.incrementLastConnectionVisited();
                    currentCity = currentCity.getConnections()
                            .get(currentCity.getLastConnectionVisited()).getDestinationCity();
                }
                else {
                    currentCity.resetVisited();
                    path.removeCity(currentCity);
                    stack.pop();
                    if(!stack.isEmpty()) {
                        currentCity = stack.peek();
                    }
                }
                continue;
            }

            if(stack.contains(currentCity)) {
                currentCity = stack.pop();
                if(currentCity.getName().equalsIgnoreCase(startCityName) && stack.isEmpty())
                    firstIteration = true;
            }
        }

        graph.resetAllVisited();
        return paths;
    }

    private static FlightPath[] sortPaths(ArrayList<FlightPath> paths, String sortType) {
        FlightPath[] sortedPaths = new FlightPath[paths.size()];

        for(int i = 0; i < paths.size(); i++) {
            FlightPath currentPath = paths.get(i);
            int j = i - 1;


            if(sortType.equalsIgnoreCase("T")) {
                while(j >= 0 && paths.get(j).getTotalTime() > currentPath.getTotalTime()) {
                    sortedPaths[j + 1] = paths.get(j);
                    j--;
                }
                sortedPaths[j + 1] = currentPath;
            }
            else if(sortType.equalsIgnoreCase("C")) {
                while(j >= 0 && paths.get(j).getTotalCost() > currentPath.getTotalCost()) {
                    sortedPaths[j + 1] = paths.get(j);
                    j--;
                }
                sortedPaths[j + 1] = currentPath;
            }
            else {
                System.out.println("Error: Invalid sort type input. Sorting aborted");
                return sortedPaths;
            }
        }
        return sortedPaths;
    }
}

class Connection {
    private final City destinationCity;
    private final double cost;
    private final int time;

    public Connection(City destinationCity, double cost, int time) {
        this.destinationCity = destinationCity;
        this.cost = cost;
        this.time = time;
    }

    public City getDestinationCity() {
        return destinationCity;
    }

    public int getTime() {
        return time;
    }

    public double getCost() {
        return cost;
    }
}

class City {
    private final String name;
    private final LinkedList<Connection> connections;
    private int lastConnectionVisited;

    public City(String name) {
        this.name = name;
        connections = new LinkedList<Connection>();
        lastConnectionVisited = -1;
    }

    public void addConnection(City destination, double cost, int time) {
        connections.add(new Connection(destination, cost, time));
    }

    public LinkedList<Connection> getConnections() {
        return connections;
    }

    public String getName() {
        return name;
    }

    public int getNumConnections() {
        return connections.size();
    }

    public void incrementLastConnectionVisited() {
        lastConnectionVisited++;
    }

    public int getLastConnectionVisited() {
        return lastConnectionVisited;
    }

    public void resetVisited() {
        lastConnectionVisited = -1;
    }
}

class FlightPathsGraph {
    LinkedList<City> graph;

    public FlightPathsGraph() {
        graph = new LinkedList<>();
    }

    public City getCity(String name) {
        for (City city : graph) {
            if (city.getName().equals(name)) {
                return city;
            }
        }

        return null;
    }

    public void createConnection(String startCityName, String destinationCityName, double cost, int time) {
        City startCity = getCity(startCityName);
        City destinationCity = getCity(destinationCityName);

        if(startCity == null) {
            startCity = new City(startCityName);
            graph.add(startCity);
        }
        if(destinationCity == null) {
            destinationCity = new City(destinationCityName);
            graph.add(destinationCity);
        }

        startCity.addConnection(destinationCity, cost, time);
        destinationCity.addConnection(startCity, cost, time);
    }

    public void displayGraph() {
        for (City city : graph) {
            LinkedList<Connection> connections = city.getConnections();
            System.out.print(city.getName() + " -> ");

            for (int i = 0; i < connections.size(); i++) {
                Connection current = connections.get(i);
                System.out.print(current.getDestinationCity().getName() + "|" +
                        current.getCost() + "|" + current.getTime());

                if (i != connections.size() - 1) {
                    System.out.print(" -> ");
                }
            }

            System.out.println();
        }
    }

    public void resetAllVisited() {
        for(City city: graph) {
            city.resetVisited();
        }
    }
}

class FlightPath {
    private final LinkedList<City> path;

    public FlightPath() {
        path = new LinkedList<>();
    }

    public void addCity(City city) {
        path.add(city);
    }

    public FlightPath createCopy() {
        FlightPath copy = new FlightPath();
        for(City city: path) {
            copy.addCity(city);
        }
        return copy;
    }

    public void removeCity(City city) { path.remove(city); }

    public boolean contains(City targetCity) {
        for(City currentCity: path) {
            if(currentCity.getName().equalsIgnoreCase(targetCity.getName()))
                return true;
        }
        return false;
    }

    public void displayPath() {
        if(path.size() == 0){
            System.out.println("Error, there is no possible path for the requested flight.");
        }
        for(int i = 0; i < path.size(); i++) {
            if(i < path.size() - 1) {
                System.out.print(path.get(i).getName() + " -> ");
            }
            else{
                System.out.print(path.get(i).getName() + ". Time: "
                        + this.getTotalTime() + " Cost: ");
                System.out.printf("%.2f", this.getTotalCost());
                System.out.println();
            }
        }
    }

    public double getTotalCost() {
        double totalCost = 0;

        for(int i = 0; i < path.size() - 1; i++) {
            City currentCity = path.get(i);
            City nextCity = path.get(i + 1);
            for(int j = 0; j < currentCity.getConnections().size(); j++) {
                Connection connection = currentCity.getConnections().get(j);
                if(connection.getDestinationCity() == nextCity) {
                    totalCost += connection.getCost();
                }
            }
        }

        return totalCost;
    }

    public int getTotalTime() {
        int totalTime = 0;

        for(int i = 0; i < path.size() - 1; i++) {
            City currentCity = path.get(i);
            City nextCity = path.get(i + 1);
            for(int j = 0; j < currentCity.getConnections().size(); j++) {
                Connection connection = currentCity.getConnections().get(j);
                if(connection.getDestinationCity() == nextCity) {
                    totalTime += connection.getTime();
                }
            }
        }

        return totalTime;
    }


}