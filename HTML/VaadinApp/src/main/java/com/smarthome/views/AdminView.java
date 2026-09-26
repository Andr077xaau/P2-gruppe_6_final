package com.smarthome.views; // views package

import com.smarthome.ServiceLocator; // to get services
import com.smarthome.model.User; // User model
import com.smarthome.service.EnergyService; // energy service
import com.smarthome.service.UserService; // user service
import com.vaadin.flow.component.UI; //browser tab
import com.vaadin.flow.component.button.Button; // button
import com.vaadin.flow.component.button.ButtonVariant; // button styles
import com.vaadin.flow.component.dialog.Dialog; // overlay dialog (for add form)
import com.vaadin.flow.component.grid.Grid; // data table
import com.vaadin.flow.component.html.H2; // heading
import com.vaadin.flow.component.notification.Notification;// popup
import com.vaadin.flow.component.notification.NotificationVariant; // notification styles
import com.vaadin.flow.component.orderedlayout.*;  // VerticalLayout, HorizontalLayout
import com.vaadin.flow.component.textfield.PasswordField; //password input field
import com.vaadin.flow.component.textfield.TextField;  // input field
import com.vaadin.flow.router.BeforeEnterEvent; // event before the view is shown
import com.vaadin.flow.router.BeforeEnterObserver; // for redirecting users that are not admin
import com.vaadin.flow.router.PageTitle;  // browser tab title
import com.vaadin.flow.router.Route; //URL
import com.vaadin.flow.server.VaadinSession; //session storage

//  http://localhost:8080/admin
@Route("admin")
@PageTitle("Brugere")
public class AdminView extends VerticalLayout implements BeforeEnterObserver { // page where admin manages users under him

    private UserService userService; // service for users
    private EnergyService energyService; // service for usage of users
    private User admin; // the logged-in admin
    private final Grid<User> grid = new Grid<>(User.class, false); // data table

    @Override
    public void beforeEnter(BeforeEnterEvent event) { //runs before the view is shown to check if the user is admin
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        if (user == null) {
            event.forwardTo(LoginView.class); // if not logged in, send to login page
        } else if (!user.isAdmin()) {
            event.forwardTo(MainView.class); // if not admin, send to main page
        }
    }

    public AdminView() {
        this.userService = ServiceLocator.users(); // get the UserService
        this.energyService = ServiceLocator.energy(); // get the EnergyService
        admin = (User) VaadinSession.getCurrent().getAttribute("user");
        if (admin == null || !admin.isAdmin()) return; // only admin can see this page

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Mine brugere");

        Button addBtn = new Button("Tilføj bruger");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openAddDialog()); // open a dialog to add a new user

        Button backBtn = new Button("Overblik");
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate(MainView.class)); // go back to main view

        HorizontalLayout bar = new HorizontalLayout(title, addBtn, backBtn); // header
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        bar.expand(title);

        grid.addColumn(User::getUsername).setHeader("Brugernavn").setAutoWidth(true).setSortable(true); // column for username
        grid.addColumn(u -> String.format("%.2f kWh", energyService.getTodayKWh(u))).setHeader("Forbrug i dag").setAutoWidth(true); // users usage today
        grid.addColumn(u -> String.format("%.2f kWh", energyService.getMonthKWh(u))).setHeader("Forbrug denne måned").setAutoWidth(true); // users usage this month
        grid.addColumn(u -> String.format("%.2f DKK", energyService.getMonthCost(u))).setHeader("Pris denne måned").setAutoWidth(true); // users cost this month
        grid.addComponentColumn(user -> {
            Button delBtn = new Button("Slet");
            delBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delBtn.addClickListener(e -> {
                userService.deleteUser(admin, user); // delete user with devices and readings
                refreshGrid();
                notify("Bruger slettet", NotificationVariant.LUMO_CONTRAST);
            });
            return delBtn;
        }).setHeader("Handlinger").setAutoWidth(true); // column with delete button
        grid.setWidthFull();

        add(bar, grid);
        refreshGrid(); // load users from database
    }


    private void refreshGrid() { // refresh table with users from database
        grid.setItems(userService.getUsersUnder(admin));
    }


    private void openAddDialog() { // dialog to add user
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tilføj ny bruger");

        TextField usernameField = new TextField("Brugernavn");
        PasswordField passwordField = new PasswordField("Adgangskode");
        usernameField.setWidthFull();
        passwordField.setWidthFull();

        VerticalLayout content = new VerticalLayout(usernameField, passwordField);
        content.setPadding(false);
        dialog.add(content);

        Button saveBtn = new Button("Tilføj", e -> {
            String username = usernameField.getValue().trim();
            String password = passwordField.getValue();
            if (username.isEmpty() || password.length() < 4) {
                notify("Udfyld brugernavn og adgangskode (mindst 4 tegn)", NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                userService.createUserUnder(admin, username, password); // add user under this admin
                dialog.close();
                refreshGrid();
                notify("Bruger tilføjet", NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException ex) {
                notify(ex.getMessage(), NotificationVariant.LUMO_ERROR); // if username taken fx
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(new Button("Annuller", e -> dialog.close()), saveBtn); // cancel and save buttons
        dialog.open();
    }

    private void notify(String msg, NotificationVariant variant) { //show a notification
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_START); //3 seconds
        n.addThemeVariants(variant);
    }
}
