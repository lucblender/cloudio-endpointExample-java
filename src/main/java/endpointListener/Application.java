package endpointListener;

import basicDataModel.DemoNode;
import ch.hevs.cloudio.endpoint.CloudioEndpoint;
import ch.hevs.cloudio.endpoint.CloudioEndpointListener;

import java.util.Random;

public class Application {

    static CloudioEndpoint myEndpoint;

    public static void main(String[] args) {

        try {
            myEndpoint = new CloudioEndpoint("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4");
            myEndpoint.addEndpointListener(new CloudioEndpointListener() {
                @Override
                public void endpointIsOnline(CloudioEndpoint endpoint) {
                    System.out.println("Endpoint is online!");
                }

                @Override
                public void endpointIsOffline(CloudioEndpoint endpoint) {
                    System.out.println("Endpoint is offline...");
                }
            });

            try {
                myEndpoint.addNode("demoNode", DemoNode.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            DemoNode demoNode = myEndpoint.getNode("demoNode");

            //send random point on a status and measure
            Random rand = new Random();
            while (true) {
                demoNode.demoObject.demoStatus.setValue(rand.nextInt());
                demoNode.demoObject.demoMeasure.setValue(rand.nextDouble());
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
