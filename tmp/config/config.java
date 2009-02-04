import org.openscience.cdk.qsar.*;
import java.util.*;
import java.io.*;

public class config {
    private PrintWriter out = null;

    public config() {
    }

    public void runconfig() {

        try {
            out = new PrintWriter((Writer)new FileWriter("descconfig.txt"));
        } catch (IOException ioe) {
        }
        genConfig(DescriptorEngine.MOLECULAR);
        genConfig(DescriptorEngine.ATOMIC);
        out.close();
    }

    public void genConfig(int type) {
        String tmp;

        DescriptorEngine de = new DescriptorEngine(type);
        List inst = de.getDescriptorInstances();
        List cn = de.getDescriptorClassNames();

        for (int i = 0; i < cn.size(); i++) {
            IDescriptor desc = (IDescriptor)inst.get(i);
            DescriptorSpecification spec = desc.getSpecification();
            
            String cname = (String)cn.get(i);
            String[] paramNames = desc.getParameterNames();
            Object[] params = desc.getParameters();

            if (paramNames == null) continue;

            tmp = "["+spec.getSpecificationReference()+"]"; 
            out.println(tmp);

            String[] comps = cname.split("\\.");
            tmp = "descriptor="+comps[5]+"."+comps[6];
            out.println(tmp);

            for (int j = 0; j < paramNames.length; j++) {
                Object ptype = desc.getParameterType(paramNames[j]);
                tmp = paramNames[j]+"="+params[j]+","+ptype.getClass().getName();
                out.println(tmp);
            }
            out.println("");
        }
    }

    public HashMap parseProps(String filename) {
        HashMap props = new HashMap();

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            boolean inEntry = true;
            while (true){
                line = in.readLine();
                if (line == null) break;

                if (line.startsWith("[")) {
                    String ref = line.replace("[","");
                    ref = ref.replace("]","");
                    ref = ref.trim();
                    HashMap hm = new HashMap();
                    in.readLine(); // drop the class info
                    boolean entryEnd = false;
                    while(true) {
                        String prop = in.readLine();
                        if (prop.length() == 0) {
                            break;
                        }
                        String name = getName(prop);
                        Object value = getValue(prop);
                        hm.put(name, value);
                    } 
                    if (!props.containsKey(ref)) {
                        ArrayList tmp = new ArrayList();
                        tmp.add(hm);
                        props.put(ref, tmp);
                    } else {
                        ArrayList tmp = (ArrayList)props.get(ref);
                        tmp.add(hm);
                        props.put(ref, tmp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    public String getName(String line) {
        String[] tokens = line.split("=");
        return tokens[0].trim();
    }
    public Object getValue(String line) {
        String[] tokens = line.split("=");
        String tmp = tokens[1];
        tokens = tmp.split(",");
        String value = tokens[0];
        String type = tokens[1];

        Object retval = null;
        if (type.contains("Boolean")) retval = new Boolean(value);
        else if (type.contains("Integer")) retval = new Integer(value);
        else if (type.contains("Double")) retval = new Double(value);
        else if (type.contains("String")) retval = value;

        return retval;
    }
    public static void main(String[] args) {
        config c = new config();

        // generate the config file
        c.runconfig();

        // read in the config file
        HashMap props = c.parse("descconfig.txt");
        System.out.println("Num entries = "+props.size());

        // list out the keys and entries
        Set keys = props.keySet();
        Iterator iter = keys.iterator();
        while(iter.hasNext()) {
            Object key = iter.next();
            System.out.println("\nKey: "+key);
            ArrayList entry = (ArrayList)props.get(key);
            System.out.println("\t"+entry.size()+" parameter sets");
            for (Object o : entry) {
                HashMap set = (HashMap)o;
                for (Object key2 : set.keySet()) {
                    System.out.println("\t\t"+key2+" = "+set.get(key2));
                }
                System.out.println("\t\t---");
            }
        }
    }
}
