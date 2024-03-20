import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.simba.sonarcloudclient.SonarCloudClient;

class SonarCloudClientTest {
  protected static final String ACCESS_TOKEN = "ad8905a8ec8a2c3c850817889bb27f0557e78f0f";

  @Test
  void getComponentMetrics() throws IOException, InterruptedException {
    List<SonarCloudClient.Measure> measures =
        new SonarCloudClient(ACCESS_TOKEN).GetComponentMetrics("simba-technologies_bigquery-odbc");

    assertFalse(measures.isEmpty());
  }

  @Test
  void GetComponentMetricsWithBranch() throws IOException, InterruptedException {
    List<SonarCloudClient.Measure> measures =
        new SonarCloudClient(ACCESS_TOKEN).GetComponentMetrics(
            "simba-technologies_bigquery-odbc",
            "master");

    assertFalse(measures.isEmpty());
  }

  @Test
  void getProjectHotspots() throws IOException, InterruptedException {
    List<SonarCloudClient.Hotspot> hotspots =
        new SonarCloudClient(ACCESS_TOKEN).GetProjectHotspots("simba-technologies_bigquery-odbc");
    assertFalse(hotspots.isEmpty());
  }
}
