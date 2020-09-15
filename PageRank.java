import java.util.*;
import java.io.*;
public class PageRank {
    public static void main(String[] args) { //usage java PageRank inputFile outputFile
        if(args.length < 2) {
            System.out.println("[Error] Usage: java PageRank input_file output_file ");
        }
        String inputFile = args[0];
        String outputFile = args[1];
        runPageRank(inputFile, outputFile);
    }


    public static void runPageRank(String inputFile, String outputFile) {
        double vertCount = getTotalVertices(inputFile); //initializing variables
        int vertCount2 = (int) vertCount;
        double[] pageranks = new double[vertCount2 + 1];
        double[] prev_pageranks = new double[vertCount2 + 1];
        double initial_pagerank = 1 / vertCount;
        double damping = 0.85;
        List<int[]> vertexInfo = getInboundOutbound(inputFile, vertCount2);
        int[] inbound = vertexInfo.get(0);
        int[] outbound = vertexInfo.get(1);

        //Oth step, calculate initial pageranks of all vertices
        for(int i = 1; i < vertCount2 + 1; i++) {
            pageranks[i] = initial_pagerank;
        }
        //iterations
        boolean done = false;
        int iterations = 0;
        while(done!= true) { //done is true when PR_done returns true
            for(int i = 1; i < vertCount2+1; i++) {
                prev_pageranks[i] = pageranks[i]; //save prev pagerank values
                pageranks[i] = 0;
            }

            for(int vertex = 1; vertex < vertCount2 + 1; vertex++) { 
                //find all incoming links to vertex
                List<Integer> incoming = incomingVerts(vertex, inputFile);
                List<Double> incomingPR = new ArrayList<Double>();
                //for each incoming vertex, get the page rank value and divide by number of outbound
                for (Integer integer : incoming) {
                    double prev_pagerank = prev_pageranks[integer];
                    int numOutbound = outbound[integer];
                    double new_value = prev_pagerank / numOutbound;
                    incomingPR.add(new_value);
                }
                if(incoming.isEmpty()) {
                    pageranks[vertex] = prev_pageranks[vertex];//if no incoming links, keep prev page rank value
                    continue;
                }
                //mutliply prev_page rank value by each value in incomingPR and that is new pagerank for vertex
                double new_PR = 0.0;
                double prev_PR = prev_pageranks[vertex];
                for(Double d: incomingPR) {
                    new_PR += prev_PR * d;
                }
                new_PR =(1-damping) + damping* new_PR;  //applying damping factor
                pageranks[vertex] = new_PR;
            }
            if(PR_done(pageranks, prev_pageranks, Math.pow(10, -50), vertCount) == false) { //diff set to 10e-50
                iterations++; 
            } else {
                done = true;
            }
        }
        System.out.println("PageRank done! Number of iterations: " + iterations);
        // System.out.printf("Final page rank values: \n");
        // for(int j = 1; j < vertCount2 + 1; j++) {
        //     System.out.printf("Page rank of " + j + " is: \t" + prev_pageranks[j] + "\n");
        // }
        outputPageRank(outputFile, pageranks);
    }

    public static double getTotalVertices(String inputFile) {
        ArrayList<Double> distinctVerts = new ArrayList<Double>();
        try {
            BufferedReader reader  = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            while(line!= null) {
            if(line.contains("{")) { //moves past the lines not part of adj list
                line = reader.readLine();
            } else if(line.contains("}")) {
                break;
            }
           for(String s: line.split(" -> ")) {
               double vert = Double.parseDouble(s);
               if(distinctVerts.contains(vert) != true) {
                    distinctVerts.add(vert);
                }
           }
        line = reader.readLine();
        }
        reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(distinctVerts);
        System.out.println("Total number of vertices is " + distinctVerts.size());
        return distinctVerts.size();
    }


    public static List<int[]> getInboundOutbound(String inputFile, int vertCount) { //gets number of inbound and outbound links for each vertex
        List<int[]> result = new ArrayList<int[]>();
        int[] inbound = new int[vertCount+1];
        int[] outbound = new int[vertCount+1];
        try {
            BufferedReader reader  = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            while(line != null) {
                if(line.contains("{")) { 
                    line = reader.readLine();
                } else if(line.contains("}")) {
                    break;
                }
                String arr[] = line.split(" -> ");
                int a = Integer.parseInt(arr[0]);
                int b = Integer.parseInt(arr[1]);
                outbound[a] += 1;
                inbound[b] += 1;
                line = reader.readLine();
            }
            reader.close();
            result.add(inbound);
            result.add(outbound);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<Integer> incomingVerts(int vertex, String inputFile) { 
        //find all the incoming vertices to vertex ie. when vertex is after the ->  
        List<Integer> vertices = new ArrayList<Integer>();
        try {
            BufferedReader reader  = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            while(line != null) {
                if(line.contains("{")) { 
                    line = reader.readLine();
                } else if(line.contains("}")) {
                    break;
            }
            String[] arr = line.split(" -> ");
            if(Integer.parseInt(arr[1]) == vertex) {
                vertices.add(Integer.parseInt(arr[0]));
            }
            line = reader.readLine();
        }
        reader.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    // System.out.println(vertices);
    return vertices;
}


public static boolean PR_done(double[] current, double[] prev, double diff, double vertCount) { 
    //PR done when difference between current and prev page rank values less than diff
    for(int i = 1; i < current.length; i++) { //special case for no incoming links
        if(current[i] == 1 / vertCount) {
            continue;
        }
        if(Math.abs(current[i] - prev[i]) < diff) {
            return true;
        }
    }
    return false;

}


    public static LinkedHashMap<Integer, Double> sortPR(double[] pagerank) { //sorts PR values descending order
        AbstractMap<Integer, Double> map = new HashMap<Integer, Double>();
        LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for(int i = 1; i < pagerank.length; i++) {
            map.put(i, pagerank[i]);
        }
        map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        return sortedMap;
    }

    public static void outputPageRank(String outputFile, double[] pagerank) { //writes PR values to file
        try {
            PrintStream fileOut = new PrintStream(outputFile);
            System.setOut(fileOut);
            System.out.println("vertex, pagerank");
            LinkedHashMap<Integer, Double> map = sortPR(pagerank);
            for(Integer vertex: map.keySet()) {
                System.out.println(vertex + ", " + map.get(vertex));
            }
        } catch (FileNotFoundException e) {

        }
    }    
}