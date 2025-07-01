package org.llmgdfuzz.initgd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Randomgraph {
    private ArrayList<Integer> nodenum = new ArrayList<Integer>();

    private ArrayList<Map<String, Integer>> tagattribute = new ArrayList<Map<String, Integer>>();
    private Map<ArrayList<Integer>, Map<String, Integer>> relattribute = new HashMap<ArrayList<Integer>, Map<String, Integer>>();

    private ArrayList<Integer> pathln = new ArrayList<>();
    private ArrayList<Integer> reversepathln = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<Integer>>> paths = new ArrayList<>();

    public ArrayList<Integer> getNodenum() {
        return nodenum;
    }

    public ArrayList<Map<String, Integer>> getTagattribute() {
        return tagattribute;
    }

    public Map<ArrayList<Integer>, Map<String, Integer>> getRelattribute() {
        return relattribute;
    }

    public ArrayList<Integer> getpathln() { return  pathln; }

    public ArrayList<Integer> getreversepathln() { return  reversepathln; }

    public ArrayList<ArrayList<ArrayList<Integer>>> getPaths() { return paths; }

    public void setRelattribute(ArrayList<Integer> newRel){
        Random random = new Random();
        Map<String, Integer> eattr = new HashMap<String, Integer>();
        for (int im =0; im < random.nextInt(10); im++){
            eattr.put(generateRandomString(random.nextInt(17)+3),random.nextInt(3));
        }
        relattribute.put(newRel,eattr);
    }

    public void randomgen(){
        Random random = new Random();
        int numberOfTag = 5+(random.nextInt(5));
        int numberOfEdges = 7+(random.nextInt(5));

        for (int i =0; i < numberOfTag; i++){
            Map<String, Integer> nattr = new HashMap<String, Integer>();
            for (int im =0; im < random.nextInt(3)+1; im++){
                nattr.put(generateRandomString(random.nextInt(10)+3),random.nextInt(6));
            }
            tagattribute.add(nattr);
            nodenum.add(random.nextInt(6)+4);
        }

        for (int i = 0; i < numberOfEdges; i++) {
            ArrayList<Integer> newRel = new ArrayList<Integer>();
            newRel.add(random.nextInt(numberOfTag));
            newRel.add(random.nextInt(numberOfTag));
            while(relattribute.containsKey(newRel) || newRel.get(0) == newRel.get(1)){
                if (newRel.get(1) == (numberOfTag -1)){
                    newRel.set(1, random.nextInt(numberOfTag));
                    if (newRel.get(0) == (numberOfTag -1)){
                        newRel.set(0, random.nextInt(numberOfTag));
                    }
                    else {
                        newRel.set(0, newRel.get(0)+1);
                    }
                }
                else {
                    newRel.set(1, newRel.get(1)+1);
                }
            }
            Map<String, Integer> eattr = new HashMap<String, Integer>();
            for (int im =0; im < random.nextInt(3)+1; im++){
                eattr.put(generateRandomString(random.nextInt(10)+3),random.nextInt(3));
            }
            relattribute.put(newRel,eattr);
        }

        for (int i =0; i < numberOfTag; i++){
            paths.add(new ArrayList<>());
        }

        for (int i =0; i < numberOfTag; i++){
            ArrayList<Integer> nodelist = new ArrayList();
            nodelist.add(i);
            pathln.add(dfs(nodelist, 0));
            reversepathln.add(reversedfs(nodelist, 0));
        }

    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        sb.append(characters.charAt(random.nextInt(52)));
        for (int i = 0; i < length-1; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private int dfs(ArrayList<Integer> nodelist, int plong){
        int pathl = 0;

        int lnode = nodelist.get(nodelist.size() -1);
        boolean hasnext = false;
        for (ArrayList<Integer> rel : relattribute.keySet()){
            if(rel.get(0) == lnode){
                if (!nodelist.contains(rel.get(1))) {
                    nodelist.add(rel.get(1));
                    hasnext = true;
                    int nl = dfs(nodelist, 0) + 1;
                    pathl = (pathl > nl) ? pathl : nl;
                    nodelist.remove(nodelist.get(nodelist.size() - 1));
                }
            }
        }

        if (!hasnext && nodelist.size() > 1){
            paths.get(nodelist.get(0)).add(new ArrayList<>(nodelist));
        }

        return pathl;
    }

    private int reversedfs(ArrayList<Integer> nodelist, int plong){
        int pathl = 0;

        int lnode = nodelist.get(nodelist.size() -1);
        for (ArrayList<Integer> rel : relattribute.keySet()){
            if(rel.get(1) == lnode){
                if (!nodelist.contains(rel.get(0))) {
                    nodelist.add(rel.get(0));
                    int nl = dfs(nodelist, 0) + 1;
                    pathl = (pathl > nl) ? pathl : nl;
                    nodelist.remove(nodelist.get(nodelist.size() - 1));
                }
            }
        }
        return pathl;
    }

}
