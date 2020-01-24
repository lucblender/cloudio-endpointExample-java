package reactivity;

import basicDataModel.DemoNode;
import ch.hevs.cloudio.endpoint.CloudioAttribute;
import ch.hevs.cloudio.endpoint.CloudioAttributeListener;
import ch.hevs.cloudio.endpoint.CloudioEndpoint;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

// This code send @update regularly and has a mqtt and http listener of its update and measure the delta time between
// the sending of mqtt message and the delivery of mqtt or http message.
// After sending requestsNb message, this code will compute the max, min, mean and standard deviation of those times.
public class Application {

    static CloudioEndpoint myEndpoint;
    static final String URL = "http://localhost:8081/api/v1/notifyAttributeChange/" +
            "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4.demoNode.demoObject.demoMeasure/1000";
    static final int requestsNb = 500;

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
}
