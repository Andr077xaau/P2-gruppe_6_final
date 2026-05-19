package com.smarthome.views; //views package 

import com.smarthome.ServiceLocator; // to get services
import com.smarthome.model.User; // User model
import com.smarthome.service.UserService; // user service 
import com.vaadin.flow.component.UI; // browser tab
import com.vaadin.flow.component.html.Anchor; // HTML <a> 
import com.vaadin.flow.component.html.H1;// HTML <h1> 
import com.vaadin.flow.component.login.LoginForm; // Vaadins username/password form
import com.vaadin.flow.component.orderedlayout.VerticalLayout; // stacks elements top-to-bottom
import com.vaadin.flow.router.PageTitle; // browser tab title
import com.vaadin.flow.router.Route;// URL
import com.vaadin.flow.server.VaadinSession; //session storage 
import java.util.Optional; //  to stop NullPointerException when searching for a user

//http://localhost:8080/
@Route("")
@PageTitle("Log ind")
public class LoginView extends VerticalLayout { //login page
    public LoginView() {
        UserService userService = ServiceLocator.users(); // get UserService

        setSizeFull(); // fill the full browser window
        setAlignItems(Alignment.CENTER);  // center elements horizontally
        H1 title = new H1("Energistyring"); // heading text

        LoginForm loginForm = new LoginForm(); //vaadin's built-in username/password form
        

        loginForm.addLoginListener(event -> { //login button
            String username = event.getUsername(); //username field
            String password = event.getPassword(); //password field

            Optional<User> result = userService.findByUsername(username); //searvh for a user with the given username in database

            if (result.isPresent() && userService.checkPassword(result.get(), password)) { //if user exists and password is correct
                VaadinSession.getCurrent().setAttribute("user", result.get()); //store this user in session storage
                UI.getCurrent().navigate(MainView.class); // go to main view
            } else {
                loginForm.setError(true); // show error message on the form
            }
        });

        Anchor registerLink = new Anchor("/register", "Opret ny bruger"); // link to the registration page

        add(title, loginForm, registerLink); // add components top to bottom
    }
}
