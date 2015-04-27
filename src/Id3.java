
import com.gaurav.tree.ArrayListTree;
import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.log;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.list;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Id3 {

    public static int rows = 0, cols = 0, location = 0, w = 0;
    public static double EntropyS = 0;
    public static String temp1 = null, temp2 = null, TARGETATTR = null, fileinput = "";
    public static Tree<String> tree = new ArrayListTree<String>();
    LinkedHashSet rule = new LinkedHashSet();

    public static void main(String[] args) throws FileNotFoundException, NodeNotFoundException, NullPointerException, IOException {

        Scanner s1 = new Scanner(System.in);
        Scanner s2 = null;
        System.out.println("What is the name of the file containing your data?");
        String name = s1.next();
        File f1 = new File(name);
        try {
            s2 = new Scanner(f1);
        } catch (FileNotFoundException ex) {
            System.out.println("File not Found");
            System.exit(0);
        }

        rows = row(f1);
        System.out.println(rows);
        cols = col(f1);
        System.out.println(cols);

        String[][] data = new String[rows][cols];
        String[][] database = database(data, f1, rows, cols);
        String[] AllAttr = AllAttr(f1);
        String[] PossibleTargetAttr = TargetAttr(database);

        System.out.println("Choose Target Attribute");
        for (int i = 0; i < PossibleTargetAttr.length; i++) {
            System.out.println(i + ". " + PossibleTargetAttr[i]);
        }
        int choice = s1.nextInt();

        for (int i = 0; i < AllAttr.length; i++) {
            if (PossibleTargetAttr[choice].contentEquals(AllAttr[i])) {
                location = i;
            }
        }
        System.out.println("Target Attribute is " + PossibleTargetAttr[choice] + " " + location);
        double EntropyS = EntropyS(database, PossibleTargetAttr[choice], location);
        TARGETATTR = PossibleTargetAttr[choice];
        System.out.println("Entropy(S) = " + EntropyS);
        String rootnode = rootnode(database, PossibleTargetAttr[choice], location, AllAttr);
        System.out.println("rootnode = " + rootnode);
        int positionOfAttr = positionOfAttr(database, rootnode);
        System.out.println("positionOfAttr = " + positionOfAttr);
        maketree(database, rootnode, f1);//creates the tree with rootnode
        String nodenames = tree.children(rootnode).toString();
        String replaceAll = nodenames.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");
        System.out.println("\n");
        for (int i = 0; i < split.length; i++) {
            System.out.println("if  " + rootnode + " is " + split[i]);
            fileinput += "if  " + rootnode + " is " + split[i] + "\n";

            id3(database, split[i], (positionOfAttr), f1);

        }

        File f = new File("Rule.txt");
        if (f.exists()) {
            f.delete();
        }
        boolean createNewFile = f.createNewFile();
        BufferedWriter output = new BufferedWriter(new FileWriter(f, true));
        output.write(fileinput);
        output.close();
        //    UNduplicate("Rule.txt");
        System.out.println("\n\nOutput is in Rule.txt");

    }//main ends

    //--------- removes duplicate data from files
    private static void UNduplicate(String filename) throws IOException {

        BufferedReader r = new BufferedReader(new FileReader(filename));
        Set<String> lines = new LinkedHashSet<String>(50); // maybe should be bigger
        String l;

        //clean it up
        while ((l = r.readLine()) != null) {
            lines.add(l);
        }
        r.close();
        //write back
        BufferedWriter w = new BufferedWriter(new FileWriter(filename));
        for (String u : lines) {
            w.write(u);
            w.newLine();
        }
        w.close();

    }

    //--------- the ID3/checks if more nodes are possible/creates sub datasets/calls gain/creates tree
    private static void id3(String[][] database, String arc, int indexoftarget, File f1) throws NodeNotFoundException, FileNotFoundException, NullPointerException {

        // System.out.println("-----arc ="+ arc+ "----- rootnode position =" +indexoftarget);
        int counter = 1;
        for (int i = 0; i < database.length; i++) {
            if (database[i][indexoftarget].equals(arc)) {
                counter++;
            }
        }
        //--------------creating subset database--------------  
        String[][] subData = new String[(counter)][database[0].length];
        if (database.length == rows) {
            for (int j = 0; j < subData[1].length; j++) {
                subData[0][j] = database[0][j];
            }
            int k = 1;
            for (int i = 0; i < database.length; i++) {
                if (database[i][indexoftarget].equals(arc) && k < counter) {
                    for (int j = 0; j < cols; j++) {
                        subData[k][j] = database[i][j];
                    }
                    k++;
                }
            }
        } else {
            int k = 0;
            for (int i = 0; i < database.length; i++) {
                if (database[i][indexoftarget].equals(arc) && k < counter) {
                    for (int j = 0; j < database[0].length; j++) {
                        subData[k][j] = database[i][j];
                    }
                    k++;
                }
            }
        }

        // just printing-------------------
        // for(int i =0;i<subData.length;i++){for(int j=0;j<subData[0].length;j++){System.out.print(subData[i][j]+" . " );} System.out.println();} 
        String morenode = morenode(subData);// check if classes are same or not

        if (morenode.equals("yes")) {
            String[][] subDatabase = subDatabase(subData, indexoftarget, subData.length, cols);
            // System.out.println("gain starts");
            String gain = gain(subDatabase);
            if (gain != null) {
                tree.add(arc, gain);
            }

            /*  for(int i=0;i<subDatabase.length;i++){
             for(int j=0;j<subDatabase[1].length;j++){
             System.out.print(subDatabase[i][j]+" ");
             }      System.out.println();
             }*/
            int positionOfAttr = positionOfAttr(subDatabase, gain);
            //System.out.println(positionOfAttr);
            maketree2(subDatabase, gain, f1, (positionOfAttr));
            String nodenames = tree.children(gain).toString();
            String replaceAll = nodenames.toString().replaceAll("\\[|\\]", "");
            String replaceAll1 = replaceAll.replaceAll(" ", "");
            String[] split = replaceAll1.split(",");
        //            System.out.println( "more is yet to come"+split[0] +" "+ split[1]  );

            if (!tree.leaves().equals(temp1) && !tree.leaves().equals(temp2)) {
                for (int i = 0; i < split.length; i++) {

                    System.out.println("      if " + gain + " is " + split[i]);
                    fileinput += "      if  " + gain + " is " + split[i] + "\n";
                    id3(subDatabase, split[i], (positionOfAttr), f1);

                                    //System.out.println("        then "+TARGETATTR+" is "+ tree.children(split[i]));
                    // fileinput +="        then "+TARGETATTR+" is "+ tree.children(split[i])+"\n";
                }
            }

        } else {

            // System.out.println("inside else pasrt= "+arc +" "+ morenode+w);
            if (!tree.contains(morenode)) {

                tree.add(arc, morenode);
                Collection<String> children = tree.children(arc);
                String toString = children.toString().replaceAll("\\[|\\]", "");
                String[] split1 = toString.split("\\.");
                System.out.println("        then " + TARGETATTR + " is " + split1[0]);
                fileinput += "        then " + TARGETATTR + " is " + split1[0] + "\n";

            } else if (tree.contains(morenode)) {
                w++;
                morenode = morenode + "." + w;
                tree.add(arc, morenode);
                Collection<String> children = tree.children(arc);
                String toString = children.toString().replaceAll("\\[|\\]", "");
                String[] split1 = toString.split("\\.");
                System.out.println("        then " + TARGETATTR + " is " + split1[0]);
                fileinput += "        then " + TARGETATTR + " is " + split1[0] + "\n";
            }

        }

    }//method ends

    //--------- to calc gain 
    private static String gain(String[][] subData) {
        int l = 0;
        Map<String, Double> myp = new HashMap<String, Double>();
 //     System.out.println("================== ");
    /*   for(int i=0;i<subData.length;i++){
         for(int j=0;j<subData[0].length;j++){
         System.out.print(subData[i][j]+" ");
         }   
         System.out.println();
         }*/

        String[] attr = new String[subData[0].length];
        for (int j = 0; j < subData[0].length; j++) {
            if (subData[0][j] != null) {
                attr[j] = subData[0][j];
            }

            if (attr[j] != null && attr[j].equals(TARGETATTR)) {
                l = j;
            }
        }

        String garrt = "";
        for (int ind = 0; ind < subData[0].length; ind++) {
            if (ind != l) {
                garrt = subData[0][ind];
                //  System.out.println("TARGETATTR "+garrt);
                LinkedHashSet a1 = new LinkedHashSet();
                ArrayList<String> b = new ArrayList<String>();
                for (int i = 1; i < subData.length; i++) {
                    //  System.out.println(subData[i][ind]);
                    a1.add(subData[i][ind]);
                    b.add(subData[i][ind]);
                }

                // System.out.println(a1);
                String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
                String replaceAll1 = replaceAll.replaceAll(" ", "");
                String[] split = replaceAll1.split(",");
                double pos = 0, neg = 0, xy = 0, value = 0;
                int[] c = new int[split.length];
                for (int k = 0; k < split.length; k++) {

                    String x = split[k];
                    c[k] = rootcount(b, x);
      //  System.out.println(split[k]+" "+c[k]);

                }

                for (int i = 0; i < split.length; i++) {

                    pos = 0;
                    neg = 0;
                    for (int j = 0; j < subData.length; j++) {
                        //System.out.println(database[j][ind] + split[i]);
                        if (subData[j][ind] != null && subData[j][ind].equals(split[i]) && subData[j][l].equals(temp1)) {
                            pos++;
                        }
                        if (subData[j][ind] != null && subData[j][ind].equals(split[i]) && subData[j][l].equals(temp2)) {
                            neg++;
                        }
                    }

                    if (pos != 0 && neg != 0) {

                        double x = (double) pos / (double) (pos + neg);
                        double xx = log(x) / log(2);
                        double y = (double) neg / (double) (pos + neg);
                        double yy = log(y) / log(2);
                        //System.out.print(x);System.out.print(xx);
                        xy = -(x * xx) - (y * yy);
                        //     System.out.println(xy);
                    } else if (pos == 0 && neg != 0) {

                        double x = (double) pos / (double) (pos + neg);
                        double xx = log(x) / log(2);
                        double y = (double) neg / (double) (pos + neg);
                        double yy = log(y) / log(2);
                        //System.out.print(x);System.out.print(xx);
                        xy = -(0) - (y * yy);
                        //        System.out.println(xy);
                    } else if (pos != 0 && neg == 0) {
                        double x = (double) pos / (double) (pos + neg);
                        double xx = log(x) / log(2);
                        double y = (double) neg / (double) (pos + neg);
                        double yy = log(y) / log(2);
                        //System.out.print(x);System.out.print(xx);
                        xy = -(x * xx) - (0);
                        //        System.out.println(xy);
                    }
         // System.out.println("--------------"+temp1+" "+ pos +" "+temp2+" "+neg+" "+ c[i]);
                    //System.out.println("value = "+ c[i] +"/"+ (database.length - 1) +"*"+xy);
                    // System.out.println( (   (double)c[i] /(double) (database.length-1)) * xy );
                    value -= (((double) c[i] / (double) (subData.length - 1)) * xy);

                }

                // System.out.println( garrt +" "+(EntropyS+value));   
                myp.put(garrt, (EntropyS + value));

            }//if ends
        }// for ends//String  lol = " "+(EntropyS+"  "+value);   

    //System.out.println( myp); 
        String a = "";
        Double maxMap = (Collections.max(myp.values()));
        for (Entry<String, Double> entry : myp.entrySet()) {
            if (entry.getValue() == maxMap) {
                a = entry.getKey();
            }
        }
        // System.out.println( a); 
        //String  lol = " "+(EntropyS+"  "+value);   
        return a;
    }

    //--------- to check if more nodes can be created
    private static String morenode(String[][] subData) throws NullPointerException {

        int colsize = 0;
        int hasboth1 = 0, hasboth2 = 0;
        for (int i = 0; i < subData.length; i++) {
            for (colsize = 0; colsize < subData[0].length; colsize++) {

                if (subData[i][colsize] == null) {
                } else if (subData[i][colsize].equals(temp1)) {
                    hasboth1++;
                } else if (subData[i][colsize].equals(temp2)) {
                    hasboth2++;
                }
                  // System.out.print(subData[i][colsize]+ " ");   

            } // System.out.println();   
        }
        if (hasboth1 != 0 && hasboth2 != 0) {
            return "yes";
        }

        if (hasboth1 == 0) {
            return temp2;
        } else {
            return temp1;
        }

    }

    //--------- returns position of the attribute
    private static int positionOfAttr(String[][] database, String Attr) {
        int counter = 0;
        for (int i = 0; i < database[0].length; i++) {
            if (database[0][i].equals(Attr)) {
                // System.out.println(database[i][0]);
                counter = i;
            }
        }
        return counter;
    }

    //--------- create subset of database
    private static String[][] subDatabase(String[][] subData, int indexoftarget, int rowsize, int colsize) {

        int counter = 0;//System.out.println("colsize"+ colsize);
        String[][] subData2 = new String[rowsize][colsize - 1];
        for (int i = 0; i < subData2.length; i++) {
            int k = 0;
            for (int j = 0; j < subData[0].length; j++) {
                //System.out.println(j+" "+k);
                if (j != indexoftarget) {
                    //System.out.println(j+" "+k);
                    subData2[i][k] = subData[i][j];
                    k++;
                }
            }
        }
        return subData2;
    }

    //--------- to find rootnode
    private static String rootnode(String[][] database, String targetAttr, int index, String[] AllAttr) {
        String rootgain = "", rootnode = "";
        Map<String, Double> myp = new HashMap<String, Double>();

        for (int i = 0; i < AllAttr.length; i++) {
            if (i != index) {
                rootgain = rootgain(database, AllAttr[i], i, index);
            }
            //   System.out.println(AllAttr.length);
            if (i < AllAttr.length) {                                         // this was -1
                String[] spli = rootgain.split(" ");
                myp.put(spli[0], Double.parseDouble(spli[1]));
            }
        }
        // System.out.println(myp);
        Double maxMap = (Collections.max(myp.values()));
        for (Entry<String, Double> entry : myp.entrySet()) {
            if (entry.getValue() == maxMap) {
                rootnode = entry.getKey();
            }
        }
        return rootnode;
    }

    //--------- to find gain
    private static String rootgain(String[][] database, String AllAttr, int ind, int attrpos) {

        ArrayList<String> b = new ArrayList<String>();
        LinkedHashSet a1 = new LinkedHashSet();
        //   System.out.println("------gain "+  AllAttr+" "+ind+"------");
        for (int i = 1; i < rows; i++) {
            // System.out.println(database[i][ind]);
            a1.add(database[i][ind]);
            b.add(database[i][ind]);
        }
        String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");
        double pos = 0, neg = 0, xy = 0, value = 0;
        int[] c = new int[split.length];
        for (int k = 0; k < split.length; k++) {

            String x = split[k];
            c[k] = rootcount(b, x);
        //System.out.println(split[k]+" "+c[k]);

        }
  //  System.out.println("----------");

        //System.out.println(temp1+ " "+temp2);
        for (int i = 0; i < split.length; i++) {
            pos = 0;
            neg = 0;
            for (int j = 0; j < database.length; j++) {
                // System.out.println(database[j][ind] + split[i]);
                if (database[j][ind].equals(split[i]) && database[j][attrpos].equals(temp1)) {
                    pos++;
                } else if (database[j][ind].equals(split[i]) && database[j][attrpos].equals(temp2)) {
                    neg++;
                }
            }
        // System.out.println("--------------"+temp1+" "+ pos +" "+temp2+" "+neg+" "+ c[i]);

            if (pos != 0 && neg != 0) {

                double x = (double) pos / (double) (pos + neg);
                double xx = log(x) / log(2);
                double y = (double) neg / (double) (pos + neg);
                double yy = log(y) / log(2);
                //System.out.print(x);System.out.print(xx);
                xy = -(x * xx) - (y * yy);
                //     System.out.println(xy);
            } else if (pos == 0 && neg != 0) {

                double x = (double) pos / (double) (pos + neg);
                double xx = log(x) / log(2);
                double y = (double) neg / (double) (pos + neg);
                double yy = log(y) / log(2);
                //System.out.print(x);System.out.print(xx);
                xy = -(0) - (y * yy);
                //        System.out.println(xy);
            } else if (pos != 0 && neg == 0) {
                double x = (double) pos / (double) (pos + neg);
                double xx = log(x) / log(2);
                double y = (double) neg / (double) (pos + neg);
                double yy = log(y) / log(2);
                //System.out.print(x);System.out.print(xx);
                xy = -(x * xx) - (0);
                //        System.out.println(xy);
            }

       //  System.out.println("value = "+ c[i] +"/"+ (database.length - 1) +"*"+xy);
            //  System.out.println( (   (double)c[i] /(double) (database.length-1)) * xy );
            value -= (((double) c[i] / (double) (database.length - 1)) * xy);

        }

    // System.out.println(AllAttr+""+(EntropyS+value));   
        String lol = AllAttr + " " + (EntropyS + value);

        return lol;

    }

    //--------- root node position in dataset
    private static int rootcount(ArrayList itemList, String itemToCheck) {
        int count = 0;
        for (int i = 0; i < itemList.size(); i++) {

            if (itemList.get(i).equals(itemToCheck)) {
                count++;
            }
        }
        return count;
    }

    //--------- dataset to array
    private static String[][] database(String[][] data, File f1, int rows, int cols) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        while (s1.hasNextLine()) {

            for (int i = 0; i < rows; i++) {
                String line = s1.nextLine();
                String[] parts = line.split(" ");
                for (int j = 0; j < cols; j++) {
                    data[i][j] = parts[j];
                }
            }
        }

        return data;
    }

    //--------- row count
    private static int row(File f1) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        int i = 0;
        if (f1.exists()) {
            while (s1.hasNextLine()) {
                s1.nextLine();
                i++;
            }
        }
        return i;
    }

    //--------- column count
    private static int col(File f1) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        int i = 0;
        if (f1.exists()) {
            if (s1.hasNextLine()) {

                String acl = s1.nextLine();
                String[] parts = acl.split(" ");
                i = parts.length;
            }
        }
        return i;
    }

    //--------- gets all attr from dataset
    private static String[] AllAttr(File f1) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        String acl = s1.nextLine();
        String[] parts = acl.split(" ");
        return parts;
    }

    //--------- get possible target attr
    private static String[] TargetAttr(String[][] data) {
        LinkedHashSet a = new LinkedHashSet();
        LinkedHashSet b = new LinkedHashSet();
        for (int i = 0; i < cols; i++) {
            a.clear();
            for (int j = 0; j < rows; j++) {
                a.add(data[j][i]);
            }
            if (a.size() == 3) {
                b.add(data[0][i]);
            }
        }
        //  System.out.println(b);
        String replaceAll = b.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");
        return split;
    }

    //--------- calc entropy for target attr
    private static double EntropyS(String[][] database, String targetAttr, int index) {
        String temp = null;
        String[] co = new String[database.length - 1];

        for (int i = 1; i < database.length - 1; i++) {
            co[i] = database[i][index];
            if (temp1 == null) {
                temp1 = co[i];
            }
            if (!temp1.equals(co[i])) {
                temp2 = co[i];
            }
        }

        List<String> resultList = Arrays.asList(co);
        int freq1 = Collections.frequency(resultList, temp1);
        int freq2 = Collections.frequency(resultList, temp2);
        if (freq1 < freq2) {
            int t = freq1;
            freq1 = freq2;
            freq2 = t;
            String t1 = temp1;
            temp1 = temp2;
            temp2 = t1;
        }

        double a = (double) freq1 / (double) (database.length - 1);
        double aa = log(a) / log(2);
        double b = (double) freq2 / (double) (database.length - 1);
        double bb = log(b) / log(2);
        double x = (-a * aa) - (b * bb);
        EntropyS = x;
        return x;
    //-(9/14)log2(9/14) -(5/14)log2(5/14) = 0.940  

    }

    //--------- Construct Tree for root node
    private static void maketree(String[][] database, String rootnode, File f1) throws FileNotFoundException, NodeNotFoundException {

        tree.add(rootnode);
        //  System.out.println(tree+"maketree");
        int attrpos = 0;
        LinkedHashSet a1 = new LinkedHashSet();
        Scanner s11 = new Scanner(f1);
        String acl = s11.nextLine();
        String[] parts = acl.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(rootnode)) {
                attrpos = i;
            }
        }
        for (int i = 1; i < database.length; i++) {
            a1.add(database[i][attrpos]);
        }
        String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");
        for (int i = 0; i < split.length; i++) {
            tree.add(rootnode, split[i]);
        }
    }

    //--------- Construct Tree for other nodes
    private static void maketree2(String[][] database, String rootnode, File f1, int positionOfAttr) throws FileNotFoundException, NodeNotFoundException {

        tree.add(rootnode);
        //   System.out.println(tree+"maketree"+positionOfAttr);
        int attrpos = positionOfAttr;
        LinkedHashSet a1 = new LinkedHashSet();

        for (int i = 1; i < database.length; i++) {
            a1.add(database[i][attrpos]);
        }
        String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");
        for (int i = 0; i < split.length; i++) {
            tree.add(rootnode, split[i]);
        }
    }

}//class ends
