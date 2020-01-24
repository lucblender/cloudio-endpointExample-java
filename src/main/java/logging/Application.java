package logging;

import basicDataModel.DemoNode;
import ch.hevs.cloudio.endpoint.CloudioEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    static CloudioEndpoint myEndpoint;

    //Default log level is ERROR, a other log appender is configured in resources\log4j2.xml
    //this additional logger will print log in the console
    static Logger applicationLogger = LogManager.getLogger(Application.class);
    static Logger rootLogger = LogManager.getRootLogger();

    public static void main(String[] args) {

        try {
            myEndpoint = new CloudioEndpoint("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4");

            try {
                myEndpoint.addNode("demoNode", DemoNode.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            while (true) {
                Thread.sleep(1000);
                applicationLogger.fatal("this is fatal log");
                applicationLogger.error("this is error log");
                applicationLogger.debug("this is debug log");
                applicationLogger.warn("this is warn log");
                applicationLogger.info("this is info log");
                applicationLogger.trace("this is trace log");

                rootLogger.trace("this is a trace log from root logger");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
