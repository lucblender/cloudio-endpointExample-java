package nodeInterfaceObjectClass;

import ch.hevs.cloudio.endpoint.CloudioEndpoint;

public class Application {

    static CloudioEndpoint myEndpoint;

    public static void main(String[] args) {

        try {
            myEndpoint = new CloudioEndpoint("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4");

            try {
                myEndpoint.addNode("DemoNodeMultipleInterfaces", DemoNodeMultipleInterfaces.class);
                myEndpoint.addNode("DemoNodeOneInterface", DemoNodeOneInterface.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
