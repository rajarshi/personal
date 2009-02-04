package net.guha.apps.anv;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

class ANVNode {
    String identifier;

    ANVNode(String identifier) {
        this.identifier = identifier;
    }

    public String toString() {
        return identifier;
    }
}

class ANVEdge {
    double weight = 0.0;

    ANVEdge(double weight) {
        this.weight = weight;
    }
}

public class AssayNetwork extends JApplet {
    Graph<ANVNode, ANVEdge> g = null;
    Layout<ANVNode, ANVEdge> layout;
    VisualizationViewer<ANVNode, ANVEdge> vv;

    // data related stuff
    HashMap<String, HashSet<String>> aidgomap;
    String[] aids;
    double[][] similarityMatrix;

    HashMap<String, ANVNode> nodeMap = new HashMap<String, ANVNode>();

    public static void main(String[] args) throws IOException {
        AssayNetwork anv = new AssayNetwork();

        anv.loadGoData();
        System.out.println("Loaded data");
        anv.generateSimilarityMatrix();
        System.out.println("Got similarity matrix");
        anv.start();

        JFrame jf = new JFrame();
        jf.getContentPane().add(anv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }

    private void loadGoData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("/home/rguha/aidgoterms.txt")));
        String line;
        aidgomap = new HashMap<String, HashSet<String>>();
        ArrayList<String> tmpaids = new ArrayList<String>();

        while ((line = reader.readLine()) != null) {
            String[] toks = line.trim().split(" ");
            String aid = toks[0];
            String[] gids = toks[1].split(",");
            HashSet<String> tmp = new HashSet<String>();
            tmp.addAll(Arrays.asList(gids));
            aidgomap.put(aid, tmp);
            tmpaids.add(aid);
        }
        aids = tmpaids.toArray(new String[]{"blah"});

        // make the list of nodes
        for (String s : aids) {
            nodeMap.put(s, new ANVNode(s));
        }
    }

    private void generateSimilarityMatrix() {
        int n = aids.length;
        similarityMatrix = new double[n][n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = i+1; j < n; j++) {
                HashSet<String> gid1 = aidgomap.get(aids[i]);
                HashSet<String> gid2 = aidgomap.get(aids[j]);
                double simvalue = getSimilarity(gid1, gid2);
                similarityMatrix[i][j] = simvalue;
                similarityMatrix[j][i] = simvalue;
            }
        }
    }

    private double getSimilarity(HashSet<String> gid1, HashSet<String> gid2) {
        HashSet<String> tmp = new HashSet<String>(gid1);
        tmp.retainAll(gid2);
        return tmp.size() / (double) gid1.size();
    }

    private Graph<ANVNode,ANVEdge> getGraph(double[][] sim, String[] aid, double cutoff) {
        Graph<ANVNode, ANVEdge> g = new UndirectedSparseGraph<ANVNode, ANVEdge>();
        for (String s : aids) g.addVertex(nodeMap.get(s));
        
        for (int i = 0; i < aids.length - 1; i++) {
            for (int j = i+1; j < aids.length; j++) {
                if (sim[i][j] > cutoff) {
                    if (i == j) continue;
                    g.addEdge(new ANVEdge(0), nodeMap.get(aid[i]), nodeMap.get(aid[j]));
                }
            }
        }
        return g;
    }

    public void start() {


        g = getGraph(similarityMatrix, aids, 0.9);
        System.out.println("g = " + g);

        layout = new KKLayout<ANVNode, ANVEdge>(g);
        vv = new VisualizationViewer<ANVNode, ANVEdge>(layout);
        vv.setBackground(Color.white);
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        vv.setGraphMouse(gm);
        Container content = getContentPane();
        content.add(new GraphZoomScrollPane(vv));

        JPanel south = new JPanel();
        JPanel grid = new JPanel(new GridLayout(2, 1));
        south.add(grid);
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        p.add(gm.getModeComboBox());
        south.add(p);
        content.add(south, BorderLayout.SOUTH);

        vv.repaint();
    }
}