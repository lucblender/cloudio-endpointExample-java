package basic;

import basicDataModel.DemoNode;
import ch.hevs.cloudio.endpoint.CloudioAttribute;
import ch.hevs.cloudio.endpoint.CloudioAttributeListener;
import ch.hevs.cloudio.endpoint.CloudioEndpoint;

import java.util.Random;

public class Application {

    static CloudioEndpoint myEndpoint;

    public static void main(String[] args) {

        try {
            myEndpoint = new CloudioEndpoint("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4");

            try {
                myEndpoint.addNode("demoNode", DemoNode.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            DemoNode demoNode = myEndpoint.getNode("demoNode");

            //add listener for attributes, will react on @set for parameter and setpoint
            //and will react on @update for status and measure
            //in this example only @set attributes are shown since they will react to
            //external MQTT messages
            demoNode.demoObject.demoParameter.addListener(new CloudioAttributeListener() {
                @Override
                public void attributeHasChanged(CloudioAttribute attribute) {
                    System.out.println("demoParameter changed: " + attribute.getValue());
                }
            });

            demoNode.demoObject.demoSetpoint.addListener(new CloudioAttributeListener() {
                @Override
                public void attributeHasChanged(CloudioAttribute attribute) {
                    System.out.println("demoSetpoint changed: " + attribute.getValue());
                }
            });

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
