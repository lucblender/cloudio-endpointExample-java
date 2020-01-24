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

import java.util.*;

// This code send @update regularly and has a mqtt and http listener of its update and measure the delta time between
// the sending of mqtt message and the delivery of mqtt or http message.
// After sending requestsNb message, this code will compute the max, min, mean and standard deviation of those times.
public class Application {

    static CloudioEndpoint myEndpoint;
    static final String URL = "http://localhost:8081/api/v1/notifyAttributeChange/" +
            "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4.demoNode.demoObject.demoMeasure/1000";
    static final int requestsNb = 500;

    static final int classWidthMqtt = 1;
    static final int classWidthHttp = 2;

    static private Map distributionMapMqtt = new TreeMap();
    static private Map distributionMapHttp = new TreeMap();

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
                    double time = System.currentTimeMillis();
                    System.out.println("demoMeasure retrieved via mqtt in " +
                            (time - (double) attribute.getValue()) + "ms");
                    if (mqttSet.size() < requestsNb)
                        mqttSet.add((time - (double) attribute.getValue()));
                }
            });

            Thread httpRequestThread = new Thread(() -> {
                while (true) {
                    OkHttpClient httpClient = new OkHttpClient();
                    String credential = Credentials.basic("root", "123456");
                    Request request = new Request.Builder().url(URL).header("Authorization", credential).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        double time = System.currentTimeMillis();
                        JSONObject jsonBody = new JSONObject(response.body().string());
                        double value = jsonBody.getDouble("value");
                        System.out.println("demoMeasure retrieved via http in " +
                                (time - value) + "ms");
                        if (httpSet.size() < requestsNb)
                            httpSet.add((time - value));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            httpRequestThread.start();

            while (!(httpSet.size() == requestsNb && mqttSet.size() == requestsNb)) {
                demoNode.demoObject.demoMeasure.setValue((double) System.currentTimeMillis());
                Thread.sleep(1000);
            }

            double sumMqtt = 0;
            double sumHttp = 0;

            double maxHttp = 0;
            double maxMqtt = 0;

            double minHttp = 0;
            double minMqtt = 0;

            double stdvHttp = 0;
            double stdvMqtt = 0;

            Frequency frequencyMqtt = new Frequency();
            Frequency frequencyHttp = new Frequency();

            mqttSet.forEach(d -> frequencyMqtt.addValue(d));
            httpSet.forEach(d -> frequencyHttp.addValue(d));

            mqttSet.stream()
                    .map(d -> Double.parseDouble(d.toString()))
                    .distinct()
                    .forEach(observation -> {
                        long observationFrequency = frequencyMqtt.getCount(observation);
                        int upperBoundary = (observation > classWidthMqtt)
                                ? Math.multiplyExact( (int) Math.ceil(observation / classWidthMqtt), classWidthMqtt)
                                : classWidthMqtt;
                        int lowerBoundary = (upperBoundary > classWidthMqtt)
                                ? Math.subtractExact(upperBoundary, classWidthMqtt)
                                : 0;
                        String bin = String.format("%03d", lowerBoundary) + "-" + String.format("%03d", upperBoundary);

                        updateDistributionMap(distributionMapMqtt, lowerBoundary, bin, observationFrequency, classWidthMqtt);
                    });

            httpSet.stream()
                    .map(d -> Double.parseDouble(d.toString()))
                    .distinct()
                    .forEach(observation -> {
                        long observationFrequency = frequencyHttp.getCount(observation);
                        int upperBoundary = (observation > classWidthHttp)
                                ? Math.multiplyExact( (int) Math.ceil(observation / classWidthHttp), classWidthHttp)
                                : classWidthHttp;
                        int lowerBoundary = (upperBoundary > classWidthHttp)
                                ? Math.subtractExact(upperBoundary, classWidthHttp)
                                : 0;
                        String bin = String.format("%03d", lowerBoundary) + "-" + String.format("%03d", upperBoundary);

                        updateDistributionMap(distributionMapHttp, lowerBoundary, bin, observationFrequency, classWidthHttp);
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
                if(mqttSet.get(i) >maxMqtt)
                    maxMqtt = mqttSet.get(i);
                if(httpSet.get(i) >maxHttp)
                    maxHttp = httpSet.get(i);
                if(mqttSet.get(i) <minMqtt)
                    minMqtt = mqttSet.get(i);
                if(httpSet.get(i) <minHttp)
                    minHttp = httpSet.get(i);
            }
            double meanHttp = sumHttp / requestsNb;
            double meanMqtt = sumMqtt/ requestsNb;
            for (int i = 0; i < mqttSet.size(); i++) {
                stdvHttp = stdvHttp+(httpSet.get(i)-meanHttp)*(httpSet.get(i)-meanHttp);
                stdvMqtt = stdvMqtt+(mqttSet.get(i)-meanMqtt)*(mqttSet.get(i)-meanMqtt);
            }
            stdvHttp = Math.sqrt(stdvHttp/requestsNb);
            stdvMqtt = Math.sqrt(stdvMqtt/requestsNb);

            System.out.println("The mean time for cloud.iO to route an MQTT publish to MQTT subscribe for " +
                    requestsNb + " requests is: " + meanMqtt + "ms" +
                    "\n min = "+minMqtt+"ms, max = "+""+maxMqtt+"ms, standard deviation = "+ stdvMqtt+"ms");
            System.out.println("The mean time for the cloud.iO to route an MQTT publish to HTTP longpoll for " +
                    requestsNb + " requests is: " + meanHttp + "ms" +
                    "\n min = "+minHttp+"ms, max = "+maxHttp+"ms, standard deviation = "+ stdvHttp+"ms");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateDistributionMap(Map distributionMap, int lowerBoundary, String bin, long observationFrequency, int classWidth) {
        int prevLowerBoundary = (lowerBoundary > classWidth) ? lowerBoundary - classWidth : 0;
        String prevBin = String.format("%03d", prevLowerBoundary) + "-" + String.format("%03d", lowerBoundary) ;
        if(!distributionMap.containsKey(prevBin))
            distributionMap.put(prevBin, 0);

        if(!distributionMap.containsKey(bin)) {
            distributionMap.put(bin, observationFrequency);
        }
        else {
            long oldFrequency = Long.parseLong(distributionMap.get(bin).toString());
            distributionMap.replace(bin, oldFrequency + observationFrequency);
        }
    }

    private static CategoryChart chartBuilder(String title){
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
}
