package com.smarthome.views; // views package

import com.smarthome.ServiceLocator;// to get Services
import com.smarthome.service.UserService;   // userservice
import com.vaadin.flow.component.UI; //browser tab 
import com.vaadin.flow.component.button.Button; //button
import com.vaadin.flow.component.button.ButtonVariant; // button styles
import com.vaadin.flow.component.html.Anchor;// HTML <a>
import com.vaadin.flow.component.html.H1;// HTML <h1>
import com.vaadin.flow.component.notification.Notification;//popup
import com.vaadin.flow.component.notification.NotificationVariant;// notification styles
import com.vaadin.flow.component.orderedlayout.VerticalLayout;// stacks elements top-to-bottom
import com.vaadin.flow.component.textfield.PasswordField; //password input field
import com.vaadin.flow.component.textfield.TextField; // text field
import com.vaadin.flow.router.PageTitle; //browser tab title
import com.vaadin.flow.router.Route; // URL

//http://localhost:8080/register
@Route("register")
@PageTitle("Opret bruger")
public class RegisterView extends VerticalLayout {
    public RegisterView() {
        UserService userService = ServiceLocator.users(); // get UserService
        setSizeFull(); // fill browser window
        setAlignItems(Alignment.CENTER); // center
        H1 title = new H1("Opret ny konto");

        TextField usernameField = new TextField("Brugernavn");
        PasswordField passwordField = new PasswordField("Adgangskode");
        PasswordField confirmField = new PasswordField("Bekræft adgangskode");
        usernameField.setWidth("300px");
        passwordField.setWidth("300px");
        confirmField.setWidth("300px");

        Button registerButton = new Button("Opret konto"); // button to add new user
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("300px");

        registerButton.addClickListener(event -> {
            String username = usernameField.getValue().trim(); //get username but remove extra spaces
            String password = passwordField.getValue();
            String confirm  = confirmField.getValue();

            if (username.isEmpty() || password.isEmpty()) { //check that username and password are not empty
                showError("Udfyld alle felter");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Adgangskoderne matcher ikke");
                return;
            }
            if (password.length() < 4) { //min password length is 4, but in finnished app it will be more
                showError("Adgangskoden skal mindst have 4 tegn");
                return;
            }

            try {
                userService.register(username, password); //add to database
                showSuccess("Konto oprettet");
                UI.getCurrent().navigate(LoginView.class); // go to login page
            } catch (IllegalArgumentException e) {
                showError(e.getMessage()); //if username taken fx
            }
        });

        Anchor backLink = new Anchor("/", "Tilbage til log ind"); // link to go back to login page
        add(title, usernameField, passwordField, confirmField, registerButton, backLink); // add components top to bottom
    }

    private void showError(String message) { //error notification with red style
        Notification n = Notification.show(message, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) { // success notification with green style
        Notification n = Notification.show(message, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
