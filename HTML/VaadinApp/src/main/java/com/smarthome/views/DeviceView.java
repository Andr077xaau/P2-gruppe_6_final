package com.smarthome.views; // views package

import com.smarthome.ServiceLocator; // to get services
import com.smarthome.model.Device; // Device model
import com.smarthome.model.User; // User model
import com.smarthome.service.EnergyService;  // energy service
import com.vaadin.flow.component.UI; //browser tab 
import com.vaadin.flow.component.button.Button; // button
import com.vaadin.flow.component.button.ButtonVariant; // button styles
import com.vaadin.flow.component.dialog.Dialog; // overlay dialog (for add/log forms)
import com.vaadin.flow.component.grid.Grid; // data table 
import com.vaadin.flow.component.html.*;  // H2 heading, Paragraph etc.
import com.vaadin.flow.component.notification.Notification;// popup
import com.vaadin.flow.component.notification.NotificationVariant; // notification styles
import com.vaadin.flow.component.orderedlayout.*;  // VerticalLayout, HorizontalLayout
import com.vaadin.flow.component.textfield.NumberField;//input field
import com.vaadin.flow.component.textfield.TextField;  // input field
import com.vaadin.flow.router.BeforeEnterEvent; // event before the view is shown
import com.vaadin.flow.router.BeforeEnterObserver; // for redirecting unauthenticated users
import com.vaadin.flow.router.PageTitle;  // browser tab title
import com.vaadin.flow.router.Route; //URL
import com.vaadin.flow.server.VaadinSession; //session storage

//  http://localhost:8080/devices
@Route("devices")
@PageTitle("Mine enheder")
public class DeviceView extends VerticalLayout implements BeforeEnterObserver {

    private EnergyService energyService; // service for device and energy reading
    private User currentUser;  // the logged-in user
    private final Grid<Device> grid = new Grid<>(Device.class, false); // data table

    @Override
    public void beforeEnter(BeforeEnterEvent event) {//runs before the view is shown to check if the user is logged in
        if (VaadinSession.getCurrent().getAttribute("user") == null) {
            event.forwardTo(LoginView.class); // if not logged in, send to login page
        }
    }

    public DeviceView() { //device management page with a table of devices and buttons to add/log/delete
        this.energyService = ServiceLocator.energy(); // get the EnergyService
        currentUser = (User) VaadinSession.getCurrent().getAttribute("user");
        if (currentUser == null) return; // if not logged in, send to login page

        setSizeFull(); // view fills browser window
        setPadding(true); // space around the edges
        setSpacing(true); // space between components

        add(buildTopBar(), buildGrid()); // add header and table
        refreshGrid();   // load devices from the database
    }


    private HorizontalLayout buildTopBar() { //page header with title and buttons to mannage device and go back to main view
        H2 title = new H2("Mine enheder");

        Button addBtn = new Button("add enhed");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openAddDialog()); // open a dialog to add a new device

        Button backBtn = new Button("Overblik");
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate(MainView.class)); // go back to main view

        HorizontalLayout bar = new HorizontalLayout(title, addBtn, backBtn); // layout for the header
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        bar.expand(title); 
        return bar;
    }


    private Grid<Device> buildGrid() { // table of devices with columns for name, power, and buttons
        grid.addColumn(Device::getName)
                .setHeader("Navn").setAutoWidth(true).setSortable(true);  // column for device name
        grid.addColumn(d -> String.format("%.0f W", d.getPowerWatts()))
                .setHeader("Effekt").setAutoWidth(true).setSortable(true); // column for device power

        grid.addComponentColumn(device -> {
            Button logBtn = new Button("Log forbrug");
            logBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logBtn.addClickListener(e -> openLogDialog(device)); // open a dialog to log usage

            Button delBtn = new Button("Slet");
            delBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delBtn.addClickListener(e -> {
                energyService.deleteDevice(device); // deletes device
                refreshGrid();
                notify("Enhed slettet", NotificationVariant.LUMO_CONTRAST);
            });

            return new HorizontalLayout(logBtn, delBtn);
        }).setHeader("Handlinger").setAutoWidth(true); // column with buttons 

        grid.setWidthFull();
        return grid;
    }


    private void refreshGrid() { //refresh table with devices from the database
        grid.setItems(energyService.getDevicesForUser(currentUser));
    }


    private void openAddDialog() { //dialog to add device
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tilføj ny enhed");

        TextField nameField = new TextField("Navn af enhed"); // input for device name
        nameField.setWidthFull();

        NumberField powerField = new NumberField("Effekt (Watt)"); // input for device power
        powerField.setMin(1);
        powerField.setWidthFull();

        VerticalLayout content = new VerticalLayout(nameField, powerField); // layout for input fields
        content.setPadding(false);
        dialog.add(content); // add input fields to dialog

        Button saveBtn = new Button("Tilføj", e -> {
            if (nameField.getValue().isBlank() || powerField.getValue() == null) { 
                notify("Udfyld alle felter", NotificationVariant.LUMO_ERROR); // error if fields are empty
                return;
            }
            energyService.addDevice(currentUser, nameField.getValue().trim(), powerField.getValue()); // add device to database
            dialog.close();
            refreshGrid();
            notify("Enhed tilføjet", NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY); 

        dialog.getFooter().add(new Button("Annuller", e -> dialog.close()), saveBtn); //cancel and save buttons in the footer of the dialog
        dialog.open();
    }


    private void openLogDialog(Device device) { // dialog to log energy usage
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Log forbrug: " + device.getName());

        Paragraph info = new Paragraph(String.format("Effekt: %.0f W", device.getPowerWatts())); // shows the power of the device 
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");

        NumberField hoursField = new NumberField("Timer brugt");
        hoursField.setMin(0.1);
        hoursField.setStep(0.5);
        hoursField.setValue(1.0);
        hoursField.setWidthFull();

        VerticalLayout content = new VerticalLayout(info, hoursField); // layout for info and input field
        content.setPadding(false);
        dialog.add(content);

        Button saveBtn = new Button("Registrér", e -> { //for logging the energy usage for the device
            Double hours = hoursField.getValue();
            if (hours == null || hours <= 0) {
                notify("Angiv antal timer", NotificationVariant.LUMO_ERROR);
                return;
            }
            energyService.logReading(currentUser, device, hours); // INSERT into energy_readings
            dialog.close();
            notify("Forbrug registreret!", NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(new Button("Annuller", e -> dialog.close()), saveBtn); //cancel
        dialog.open();
    }

    private void notify(String msg, NotificationVariant variant) { //show a notification
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_START); //3 seconds
        n.addThemeVariants(variant);
    }
}
