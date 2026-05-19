package com.smarthome; // main package of program

import com.smarthome.repository.DeviceRepository;// class for devices table
import com.smarthome.repository.EnergyReadingRepository;//class for energy_readings table
import com.smarthome.repository.UserRepository;//class for users table
import com.smarthome.service.EnergyService;//logic for divces and forbrug
import com.smarthome.service.UserService;// logic for user user accounts
import com.vaadin.flow.server.VaadinServlet;// Vaadin
import org.eclipse.jetty.ee10.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee10.servlet.ServletHolder; 
import org.eclipse.jetty.ee10.webapp.FragmentConfiguration;
import org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext; 
import org.eclipse.jetty.ee10.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.server.Server;
import java.nio.file.Path; // used to find path to folder

// application.java is starting point af program
//   1. Open the SQLite database
//   2. Create repository objects (JDBC)
//   3. Create service objects and wire them to the repositories
//   4. Put services in ServiceLocator so Vaadin views can reach them
//   5. Seed demo data if the database is empty
//   6. Start Jetty on port 8080 and wait for requests
public class Application {
    public static void main(String[] args) throws Exception {

        Database db = new Database("energi.db"); // make database(from database.java)
        db.createTables();

        UserRepository userRepo = new UserRepository(db); //make repository objects (from ---repository.java)
        DeviceRepository deviceRepo = new DeviceRepository(db);
        EnergyReadingRepository readingRepo = new EnergyReadingRepository(db, deviceRepo);

        UserService userService   = new UserService(userRepo);  //makes services (from ---service.java)
        EnergyService energyService = new EnergyService(deviceRepo, readingRepo);

        ServiceLocator.init(userService, energyService); //makes services available with service locator

        new DataInitializer(userService, energyService).run(); //makes data for demo

        // here we use Jetty to handle web server
        Server server = new Server(8080); // listen on http://localhost:8080
        WebAppContext context = new WebAppContext(); //jetty main class for web program
        context.setContextPath("/"); //everything can be reached at main url

        context.setBaseResourceAsPath(
                Path.of("src/main/webapp").toAbsolutePath()); //show to jetty where files for program are

        context.setConfigurationClasses(new String[]{ //configuration of jetty, so it works with vaadin
                WebInfConfiguration.class.getName(), //next configurations need this to work
                MetaInfConfiguration.class.getName(), //get vaadin components
                FragmentConfiguration.class.getName(), //get vaadin components
                AnnotationConfiguration.class.getName(), //get vaadin annotations (fx:@Route)
        });

        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*"); //scan all jars to find needed vaadin files
        context.setExtraClasspath(Path.of("target/classes").toAbsolutePath().toString()); //find compiled classes so jetty can find vaadin views
        context.setParentLoaderPriority(true); //for it to not conflict with vaadin
        context.setDefaultsDescriptor(""); //for it to not conflict with vaadin

        ServletHolder vaadinHolder = new ServletHolder("vaadin", VaadinServlet.class); //so vaadin can handle requests to the server
        vaadinHolder.setAsyncSupported(true); //for vaadin
        context.addServlet(vaadinHolder, "/*"); //all requests go to vaadin, which then routes to correct view with @Route

        server.setHandler(context); //set jetty to use our configuration
        server.start(); // start server

        System.out.println("started at http://localhost:8080");
        server.join(); // stop until stopped server with Ctrl+C
    }
}
