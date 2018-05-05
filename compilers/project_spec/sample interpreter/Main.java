import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String[] readLines(String filename) {
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            return lines.toArray(new String[lines.size()]);

        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            System.exit(1);
        }
        return null;
    }

    public static String getFileExtension(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Expected .ir file as arg");
            System.exit(1);
        } else if (!getFileExtension(args[0]).equals("ir")) {
            System.out.println("Expected .ir file as arg");
            System.exit(1);
        }

        String[] IR = readLines(args[0]);
        Interpreter interpreter = new Interpreter(IR);
        interpreter.run();
    }
}
