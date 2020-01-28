import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception{
        FileReader fr = new FileReader(args[0] + "/.gitlab-ci.yml");

        BufferedReader reader = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line = line = reader.readLine();
        List<String> skippables = Arrays.asList("image", "variables", "stages", "cache", "before_script", ".aws_dependencies");
        List<String> Vars = new ArrayList<>();

        int go = 0;
        //0 -> dont go
        //1 -> in job
        //2 -> in except:
        //3 -> in variables:
        String jobName = "";

        while (line != null) {

            String line2 = line;
            if (skippables.stream().anyMatch(s -> line2.startsWith(s))) {
                System.out.println(line);
            }
            else if (line.matches("\\S*:")) { // Start of a real job
                if (go != 0) {//didnt finish
                    doPrint(go, jobName);
                }
                go = 1;
                jobName = line.substring(0,line.length() - 1).toUpperCase().replace("-", "_");
                Vars.add("SKIP_" + jobName);
                System.out.println(line);
            }
            else if (line.startsWith(" ") && line.matches(".*\\S*.*")) { //Random config line
                if (go == 1 && line.contains("except:")) {
                    go = 2;
                }
                else if (go == 2 && line.contains("variables:")) {
                    go = 3;
                }
                System.out.println(line);
            }
            else if (go == 2) {
                doPrint(go, jobName);
                go = 0;
            }
            else if (go == 3) {
                doPrint(go, jobName);
                go = 0;
            }
            else if (line.matches("[\\s]*") && go == 1)
            { //found end of job
                doPrint(go, jobName);
                go = 0;
            }
            else {
                System.out.println("\n");
            }
            line = reader
                .readLine();
        }
        if (go == 1) {//ended in job
            doPrint(go, jobName);
            go = 0;
        }

        System.out.println("----------------------------------\n\n\n\n");
        Vars.forEach(s -> System.out.println(s));
    }

    static void doPrint(int arg, String jobName) {
        System.out.println(
            (arg <= 1 ? "  except:\n" : "") +
                (arg <=2 ? "    variables:\n" : "") +
                "      - '$SKIP_" + jobName + " == \"true\"'\n");
    }
}
