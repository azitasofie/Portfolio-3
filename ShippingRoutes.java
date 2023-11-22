package Portfolio3;
// Import statements to include necessary Java classes for handling file input and collections.
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// This data structure is working for tracking elements divided into disjoint (non-overlapping) sets.
class DisjointSet {
    // A HashMap to store the parent of each element. 
    private Map<String, String> parent;

    public DisjointSet() {
        parent = new HashMap<>();
    }

    // This is used to identify which set a particular element is part of.
    public String find(String item) {
        // add it to the map, initially pointing to itself
        if (!parent.containsKey(item)) {
            parent.put(item, item);
        }

        // If the item's parent is not itself, recursively find and update the item's
        // parent to its root.
        // This speeds up future 'find' operations.
        if (!item.equals(parent.get(item))) {
            parent.put(item, find(parent.get(item)));
        }
        return parent.get(item);
    }

    // Method to unify or merge two sets.
    public void union(String set1, String set2) {
        // Find the roots of the sets for set1 and set2.
        String root1 = find(set1);
        String root2 = find(set2);

        // Merge the two sets by updating the parent of one root to be the other root, linking the two trees.
        parent.put(root1, root2);
    }
}

// Class to represent the graph. This class uses an adjacency list to store the
// graph,
// which is a great way to represent a graph
class Graph {
    // Define an adjacency list using a HashMap
    Map<String, Map<String, Integer>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // Method to add an edge to the graph.
    public void addEdge(String source, String destination, int length) {
        // Make sure that the source node exists in the adjacency list. If not, add it with a new empty HashMap.
        adjacencyList.putIfAbsent(source, new HashMap<>());
        // Add the destination node and its length to the source node's neighbor list.
        adjacencyList.get(source).put(destination, length);

        // Since the graph is bidirectional, add the reverse edge from destination to the source as well. This makes sure that the graph represents an undirected graph.
        adjacencyList.putIfAbsent(destination, new HashMap<>());
        adjacencyList.get(destination).put(source, length);
    }

    // Print the graph's structure. 
    public void printGraph() {
        // Iterate over each node in the adjacency list.
        for (String source : adjacencyList.keySet()) {
            System.out.print(source + " -> ");
            // For each node, print its connected neighbors and the lengths of the edges.
            for (Map.Entry<String, Integer> entry : adjacencyList.get(source).entrySet()) {
                System.out.print(entry.getKey() + "(" + entry.getValue() + ") ");
            }
            System.out.println(); // Move to a new line after printing all neighbors of a node.
        }
    }

    // Private method for performing a Depth-First Search (DFS) from a given node.
    // DFS is a technique used to explore nodes and edges of a graph.
    private void dfs(String node, Set<String> visited) {
        // Mark the current node as visited. 
        visited.add(node);

        // Recursively visit all unvisited neighbors of the current node.
        for (String neighbor : adjacencyList.get(node).keySet()) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, visited); // Recursive call to explore further.
            }
        }
    }

    // Public method to check if the graph is connected.
    public boolean isConnected() {
        // Create a set to keep track of visited nodes during DFS.
        Set<String> visited = new HashSet<>();
        // Start DFS from the first node in the adjacency list. This, if the graph isn't empty. 
        if (!adjacencyList.isEmpty()) {
            String startNode = adjacencyList.keySet().iterator().next();
            dfs(startNode, visited);
        }

        // If the size of the visited set equals the number of nodes in the graph, the graph is connected.
        return visited.size() == adjacencyList.size();
    }

    // Construct a Minimum Spanning Tree (MST) using Kruskal's algorithm.
    // This method returns the total length of the MST which is wanted in Part 3 of the assignment.
    public int constructMST() {
        // Create a priority queue to store and sort the edges of the graph by their length.
        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>((a, b) -> a.length - b.length);

        // Add all edges of the graph to the priority queue.
        for (String source : adjacencyList.keySet()) {
            for (Map.Entry<String, Integer> entry : adjacencyList.get(source).entrySet()) {
                edgeQueue.offer(new Edge(source, entry.getKey(), entry.getValue()));
            }
        }

        // Create a disjoint set to keep track of which nodes are in which components of the MST.
        DisjointSet disjointSet = new DisjointSet();

        // Initialize a variable to track the total length of the MST.
        int totallength = 0;
        // Process edges from the queue until it is empty or MST is finished.
        while (!edgeQueue.isEmpty()) {
            // Retrieve and remove the edge with the smallest length, to make sure thar the MST is minimal.
            Edge edge = edgeQueue.poll();

            // This is used to determine if adding this edge will create a cycle.
            String root1 = disjointSet.find(edge.source);
            String root2 = disjointSet.find(edge.destination);

            // If the roots are different, adding this edge won't form a cycle.
            if (!root1.equals(root2)) {
                // Add the edge to the MST and merge the two sets in the disjoint set.
                disjointSet.union(root1, root2);
                totallength += edge.length; // Update the total length of the MST.
            }
        }

        // Return the total length of the MST after processing all edges.
        return totallength;
    }

    // Helper class to represent an edge in the graph.
    private static class Edge {
        String source; // The source node of the edge.
        String destination; // The destination node of the edge.
        int length; // The length of the edge, used in determining the MST.

        // Constructor to create an Edge instance. It initializes the source,
        // destination, and length of the edge.
        public Edge(String src, String dest, int w) {
            source = src;
            destination = dest;
            length = w;
        }
    }
}

// The main class of the program
public class ShippingRoutes {
    public static void main(String[] args) {
        Graph graph = new Graph();

        // Define the file name from which to read the graph data. This is the network.txt file which has been attached with the portfolio 3 description. 
        String fileName ="network.txt";

        // BufferedReader is used for efficient reading of text files.
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Read the file line by line until the end of the file is reached.
            while ((line = reader.readLine()) != null) {
                // Split each line into parts using a comma as the delimiter.
                // Each line represents an edge in the graph.
                String[] parts = line.split(",");
                // Make sure that each line has three parts: source, destination, and length.
                if (parts.length == 3) {
                    // Trim whitespace and parse the data.
                    String source = parts[0].trim();
                    String destination = parts[1].trim();
                    int length = Integer.parseInt(parts[2].trim());
                    // Add an edge to the graph using the parsed data.
                    graph.addEdge(source, destination, length);
                }
            }
        } catch (IOException e) {
            // Catch and handle IOExceptions that may occur during file reading.
            e.printStackTrace();
        }

        // Print the graph's structure to the console
        graph.printGraph();

        // Check if the graph is connected using the isConnected method.
        boolean isConnected = graph.isConnected();
        // Print the result of the connectivity check
        System.out.println("Is the graph connected? " + isConnected);

        // Construct the Minimum Spanning Tree (MST) 
        // The constructMST method calculates and returns the total length of the MST.
        int totallength = graph.constructMST();
        // Print the total length of the MST to the console.
        System.out.println("Total length of the MST: " + totallength);
    }
}
