package reactivity;

import basicDataModel.DemoNode;
import ch.hevs.cloudio.endpoint.CloudioAttribute;
import ch.hevs.cloudio.endpoint.CloudioAttributeListener;
import ch.hevs.cloudio.endpoint.CloudioEndpoint;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.math3.stat.Frequency;
import org.json.JSONObject;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

// This code send @update regularly and has a mqtt and http listener of its update and measure the delta time between
// the sending of mqtt message and the delivery of mqtt or http message.
// After sending requestsNb message, this code will compute the max, min, mean and standard deviation of those times.
public class Application {

    static CloudioEndpoint myEndpoint;
    static final String URL = "http://localhost:8081/api/v1/notifyAttributeChange/" +
            "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4.demoNode.demoObject.demoMeasure/1000";
    static final int requestsNb = 50;

    static final int classWidthMqtt = 1;
    static final int classWidthHttp = 2;

    static private Map distributionMapMqtt = new LinkedHashMap();
    static private Map distributionMapHttp = new LinkedHashMap();

    public static void main(String[] args) {

        List<Double> mqttSet = new LinkedList<>();
        List<Double> httpSet = new LinkedList<>();

        try {
            myEndpoint = new CloudioEndpoint("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4");

            try {
                myEndpoint.addNode("demoNode", DemoNode.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            DemoNode demoNode = myEndpoint.getNode("demoNode");

            demoNode.demoObject.demoMeasure.addListener(new CloudioAttributeListener() {
                @Override
                public void attributeHasChanged(CloudioAttribute attribute) {
                    double time = System.nanoTime() / 1000_000.0;
                    double delay = round(time - (double) attribute.getValue(), 3);
                    System.out.println("demoMeasure retrieved via mqtt in " + delay + "ms");
                    if (mqttSet.size() < requestsNb)
                        mqttSet.add(delay);
                }
            });


            Thread httpRequestThread = new Thread(() -> {
                while (httpSet.size() < requestsNb) {
                    OkHttpClient httpClient = new OkHttpClient();
                    String credential = Credentials.basic("root", "123456");
                    Request request = new Request.Builder().url(URL).header("Authorization", credential).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        double time = System.nanoTime() / 1000_000.0;
                        JSONObject jsonBody = new JSONObject(response.body().string());
                        double value = jsonBody.getDouble("value");
                        double delay = round(time - value, 3);
                        System.out.println("demoMeasure retrieved via http in " + delay + "ms");
                        httpSet.add(delay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            httpRequestThread.start();

            while (!(httpSet.size() == requestsNb && mqttSet.size() == requestsNb)) {
                demoNode.demoObject.demoMeasure.setValue((double) System.nanoTime() / 1000_000.0);
                Thread.sleep(1000);
            }

            Frequency frequencyMqtt = new Frequency();
            Frequency frequencyHttp = new Frequency();

            Collections.sort(httpSet);
            Collections.sort(mqttSet);

            double sumMqtt = 0;
            double sumHttp = 0;

            double minHttp = httpSet.get(0);
            double minMqtt = mqttSet.get(0);

            double maxHttp = httpSet.get(httpSet.size() - 1);
            double maxMqtt = mqttSet.get(mqttSet.size() - 1);

            double stdvHttp = 0;
            double stdvMqtt = 0;


            mqttSet.forEach(d -> frequencyMqtt.addValue(d));
            httpSet.forEach(d -> frequencyHttp.addValue(d));

            //Uncomment to  fill bins with '0' and have correct spacing
            //commented to reduce the size of graphs
            //initDistributionMap((int)minHttp, (int)maxHttp, classWidthHttp, distributionMapHttp);
            //initDistributionMap((int)minMqtt, (int)maxMqtt, classWidthMqtt, distributionMapMqtt);

            mqttSet.stream()
                    .distinct()
                    .forEach(observation -> {
                        updateDistributionMap(distributionMapMqtt, frequencyMqtt, observation, classWidthMqtt);
                    });

            httpSet.stream()
                    .distinct()
                    .forEach(observation -> {
                        updateDistributionMap(distributionMapHttp, frequencyHttp, observation, classWidthHttp);
                    });

            CategoryChart chartMQTT = chartBuilder("MQTT to MQTT time distribution");
            CategoryChart chartHTTP = chartBuilder("MQTT to HTTP time distribution");

            List yData = new ArrayList();
            yData.addAll(distributionMapMqtt.values());
            chartMQTT.addSeries("MQTT to MQTT", Arrays.asList(distributionMapMqtt.keySet().toArray()), yData);

            yData = new ArrayList();
            yData.addAll(distributionMapHttp.values());
            chartHTTP.addSeries("MQTT to HTTP", Arrays.asList(distributionMapHttp.keySet().toArray()), yData);

            new SwingWrapper<>(chartMQTT).displayChart();
            new SwingWrapper<>(chartHTTP).displayChart();

            for (int i = 0; i < mqttSet.size(); i++) {
                sumMqtt += mqttSet.get(i);
                sumHttp += httpSet.get(i);
            }
            double meanHttp = sumHttp / requestsNb;
            double meanMqtt = sumMqtt / requestsNb;
            for (int i = 0; i < mqttSet.size(); i++) {
                stdvHttp = stdvHttp + (httpSet.get(i) - meanHttp) * (httpSet.get(i) - meanHttp);
                stdvMqtt = stdvMqtt + (mqttSet.get(i) - meanMqtt) * (mqttSet.get(i) - meanMqtt);
            }
            stdvHttp = Math.sqrt(stdvHttp / requestsNb);
            stdvMqtt = Math.sqrt(stdvMqtt / requestsNb);

            System.out.println("The mean time for cloud.iO to route an MQTT publish to MQTT subscribe for " +
                    requestsNb + " requests is: " + meanMqtt + "ms" +
                    "\n min = " + minMqtt + "ms, max = " + "" + maxMqtt + "ms, standard deviation = " + stdvMqtt + "ms");
            System.out.println("The mean time for the cloud.iO to route an MQTT publish to HTTP longpoll for " +
                    requestsNb + " requests is: " + meanHttp + "ms" +
                    "\n min = " + minHttp + "ms, max = " + maxHttp + "ms, standard deviation = " + stdvHttp + "ms");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateDistributionMap(Map distributionMap, Frequency frequency, Double observation, int classWidth) {
        double observationFrequency = frequency.getCount(observation);
        int upperBoundary = (observation > classWidth)
                ? Math.multiplyExact((int) Math.ceil(observation / classWidth), classWidth)
                : classWidth;
        int lowerBoundary = (upperBoundary > classWidth)
                ? Math.subtractExact(upperBoundary, classWidth)
                : 0;
        String bin = lowerBoundary + "-" + upperBoundary;

        if (!distributionMap.containsKey(bin))
            distributionMap.put(bin, observationFrequency);
        else {
            double oldFrequency = (Double) distributionMap.get(bin);
            distributionMap.replace(bin, oldFrequency + observationFrequency);
        }
    }

    private static void initDistributionMap(int min, int max, int classWidth, Map distributionMap) {
        for (int i = min; i <= (max + classWidth); i++) {
            int upperBoundary = (i > classWidth)
                    ? Math.multiplyExact((int) Math.ceil(i / classWidth), classWidth)
                    : classWidth;
            int lowerBoundary = (upperBoundary > classWidth)
                    ? Math.subtractExact(upperBoundary, classWidth)
                    : 0;

            String bin = lowerBoundary + "-" + upperBoundary;
            if (!distributionMap.containsKey(bin))
                distributionMap.put(bin, 0d);
        }
    }

    private static CategoryChart chartBuilder(String title) {
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600)
                .title(title)
                .xAxisTitle("Time group [ms]")
                .yAxisTitle("Frequency")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(0.99);
        chart.getStyler().setOverlapped(true);
        return chart;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
