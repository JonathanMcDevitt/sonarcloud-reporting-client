package com.simba.sonarcloudclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.LinkedList;
import java.util.List;

/**
 * A client interface for the SonarCloud API.
 */
public class SonarCloudClient {

  /**
   * The base URI for the SonarCloud service.
   */
  private static final String SONARCLOUD_API_BASE_URI = "https://sonarcloud.io";

  /**
   * The API path for the measures/component API.
   */
  private static final String MEASURES_COMPONENT_PATH = "/api/measures/component";

  /**
   * The metrics keys to request when targeting the measures/component API.
   */
  private static final String COMPONENT_METRICS_KEYS =
      "coverage,reliability_rating,security_rating,sqale_rating,security_review_rating";

  /**
   * The API path for the hotspots/search API.
   */
  private static final String HOTSPOTS_SEARCH_PATH = "/api/hotspots/search";

  /**
   * The access token used by the client to authorize requests to the SonarCloud API.
   */
  private final String accessToken;

  /**
   * The HTTP client which executes HTTP requests.
   */
  private final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).build();

  /**
   * Constructor.
   * @param accessToken The access token used to authorize requests.
   */
  public SonarCloudClient(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Model representation of a measure, as reported by the measures/components API.
   */
  public static class Measure {
    public String metric;
    public String value;
    public boolean bestValue;
  }

  /**
   * Get the metrics for a given component on its master branch.
   * @param component The component from which to get the metrics.
   * @return The list of measures for the given component.
   * @throws IOException If the HTTP request to the service fails.
   * @throws InterruptedException If the HTTP request to the service is interrupted.
   */
  public List<Measure> GetComponentMetrics(String component)
      throws IOException, InterruptedException {
    return GetComponentMetrics(component, "master");
  }

  /**
   * Get the metrics for a given component on the specified branch.
   * @param component The component from which to get the metrics.
   * @param branch The branch for the given component.
   * @return The list of measures for the given component and its branch.
   * @throws IOException If the HTTP request to the service fails.
   * @throws InterruptedException If the HTTP request to the service is interrupted.
   */
  public List<Measure> GetComponentMetrics(String component, String branch)
      throws IOException, InterruptedException {
    String uriParams = "?"
        + "component=" + component + "&"
        + "branch=" + branch + "&"
        + "metricKeys=" + COMPONENT_METRICS_KEYS;
    HttpRequest componentMetricsRequest =
        HttpRequest.newBuilder(
                URI.create(SONARCLOUD_API_BASE_URI + MEASURES_COMPONENT_PATH + uriParams))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

    HttpResponse<String> response =
        client.send(componentMetricsRequest, BodyHandlers.ofString());
    // TODO: Error handling

    // Parse the response
    JsonNode responseJson = new ObjectMapper().readTree(response.body());
    JsonNode measuresArr = responseJson.get("component").get("measures");
    if (!measuresArr.isArray()) {
      // TODO: Throw
    }

    List<Measure> measures = new LinkedList<>();
    for (JsonNode measureObj : measuresArr) {
      Measure measure = new Measure();
      measure.metric = measureObj.get("metric").textValue();
      measure.value = measureObj.get("value").textValue();
      measure.bestValue = measureObj.get("bestValue").booleanValue();

      measures.add(measure);
    }

    return measures;
  }

  /**
   * Model representation of a security hotspot.
   */
  public static class Hotspot {
    public String key;
    public String component;
    public String project;
    public String securityCategory;
    public String vulnerabilityProbability;
    public String status;
    public long line;
    public String message;
  }

  /**
   * Get the security hotspots for the given project.
   * @param project The project to scan for hotspots.
   * @return The list of security hotspots for the given project.
   * @throws IOException If the HTTP request to the service fails.
   * @throws InterruptedException If the HTTP request to the service is interrupted.
   */
  public List<Hotspot> GetProjectHotspots(String project) throws IOException, InterruptedException {
    String uriParams = "?"
        + "projectKey=" + project;
    HttpRequest componentMetricsRequest =
        HttpRequest.newBuilder(
                URI.create(SONARCLOUD_API_BASE_URI + HOTSPOTS_SEARCH_PATH + uriParams))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

    HttpResponse<String> response =
        client.send(componentMetricsRequest, BodyHandlers.ofString());
    // TODO: Error handling

    // Parse the response
    JsonNode responseJson = new ObjectMapper().readTree(response.body());
    JsonNode hotspotsArr = responseJson.get("hotspots");
    if (!hotspotsArr.isArray()) {
      // TODO: Throw
    }

    List<Hotspot> hotspots = new LinkedList<>();
    for (JsonNode hotspotObj : hotspotsArr) {
      Hotspot hotspot = new Hotspot();
      hotspot.key = hotspotObj.get("key").textValue();
      hotspot.component = hotspotObj.get("component").textValue();
      hotspot.project = hotspotObj.get("project").textValue();
      hotspot.securityCategory = hotspotObj.get("securityCategory").textValue();
      hotspot.vulnerabilityProbability = hotspotObj.get("vulnerabilityProbability").textValue();
      hotspot.line = hotspotObj.get("line").longValue();
      hotspot.message = hotspotObj.get("message").textValue();
      hotspot.status = hotspotObj.get("status").textValue();

      hotspots.add(hotspot);
    }

    return hotspots;
  }
}
