import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import com.simba.sonarcloudclient.SonarCloudClient;
import com.simba.sonarcloudclient.SonarCloudClient.Hotspot;
import com.simba.sonarcloudclient.SonarCloudClient.Measure;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    ArgumentParser parser = ArgumentParsers.newFor("SonarCloud Client").build()
        .defaultHelp(true)
        .description("A SonarCloud client for retrieving reportable data.");
    parser.addArgument("-t", "--token")
        .required(true)
        .help("The access token for the SonarCloud API.");
    parser.addArgument("-p", "--project", "-c", "--component")
        .required(true)
        .help("The project/component from which to retrieve the SonarCloud scan results.");
    parser.addArgument("-b", "--branch")
        .required(true)
        .help("The branch of the project/component from which to retrieve the SonarCloud scan"
            + " results.");

    Namespace mainNamespace = null;
    try {
      mainNamespace = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    String token = mainNamespace.getString("token");
    String project = mainNamespace.getString("project");
    String branch = mainNamespace.getString("branch");

    SonarCloudClient client = new SonarCloudClient(token);
    List<Measure> measures =
        (branch.isEmpty()
            ? client.GetComponentMetrics(project)
            : client.GetComponentMetrics(project, branch));
    StringBuilder outputBuilder = new StringBuilder();
    if (!measures.isEmpty()) {
      outputBuilder.append("Measures: [\n");
      Iterator<Measure> measureIterator = measures.iterator();
      while (measureIterator.hasNext()) {
        Measure currMeasure = measureIterator.next();
        outputBuilder.append("  ");
        outputBuilder.append(currMeasure.metric).append(": ").append(currMeasure.value);

        if (measureIterator.hasNext()) {
          outputBuilder.append(", ");
        }
      }
      outputBuilder.append("\n]");
    } else {
      outputBuilder.append("No metrics found for project `").append(project).append("`");
      if (!branch.isEmpty()) {
        outputBuilder.append(" (`").append(branch).append("` branch)");
      }
      outputBuilder.append(".");
    }

    System.out.println(outputBuilder);
    System.out.println();
    outputBuilder.setLength(0);

    List<Hotspot> hotspots = client.GetProjectHotspots(project);
    if (!hotspots.isEmpty()){
      outputBuilder.append("Security Hotspots: [");
      Iterator<Hotspot> hotspotIterator = hotspots.iterator();
      while (hotspotIterator.hasNext()) {
        outputBuilder.append("\n  {\n");

        Hotspot currHotspot = hotspotIterator.next();
        outputBuilder.append("    Component: ").append(currHotspot.component).append(",\n");
        outputBuilder.append("    Key: ").append(currHotspot.key).append(",\n");
        outputBuilder.append("    Project: ").append(currHotspot.project).append(",\n");
        outputBuilder.append("    Security category: ").append(currHotspot.securityCategory)
            .append(",\n");
        outputBuilder.append("    Vulnerability probability: ")
            .append(currHotspot.vulnerabilityProbability)
            .append(",\n");
        outputBuilder.append("    Status: ").append(currHotspot.status).append(",\n");
        outputBuilder.append("    Line: ").append(currHotspot.line).append(",\n");
        outputBuilder.append("    Message: ").append(currHotspot.message);
        outputBuilder.append("\n  }");

        if (hotspotIterator.hasNext()) {
          outputBuilder.append(",");
        }
      }
      outputBuilder.append("]");
    } else {
      outputBuilder.append("No hotspots found for project `").append(project).append("`.");
    }

    System.out.println(outputBuilder);
    System.out.println();
  }
}
