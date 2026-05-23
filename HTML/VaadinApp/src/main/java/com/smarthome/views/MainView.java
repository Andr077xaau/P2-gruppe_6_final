package com.smarthome.views; // views package = all Vaadin UI pages

import com.smarthome.ServiceLocator; //access to services
import com.smarthome.model.EnergyReading; // energy reading model
import com.smarthome.model.User; //user model
import com.smarthome.service.EnergyService;//energy reading and device logic
import com.smarthome.service.UserService; // user account logic
import com.vaadin.flow.component.Component; // base class for UI components
import com.vaadin.flow.component.UI; // the browser tab
import com.vaadin.flow.component.button.Button;// button
import com.vaadin.flow.component.button.ButtonVariant;// style buttons
import com.vaadin.flow.component.grid.Grid; // data table
import com.vaadin.flow.component.html.Div; //for the bars in the bar chart
import com.vaadin.flow.component.html.H2;// heading level 2
import com.vaadin.flow.component.html.H3;// heading level 3 
import com.vaadin.flow.component.html.Paragraph; // paragraph 
import com.vaadin.flow.component.html.Span;  // inline text
import com.vaadin.flow.component.notification.Notification; // popup
import com.vaadin.flow.component.notification.NotificationVariant; // style notifications
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;  // horizontal layout
import com.vaadin.flow.component.orderedlayout.VerticalLayout;  // vertical layout
import com.vaadin.flow.component.textfield.NumberField; // input field
import com.vaadin.flow.router.BeforeEnterEvent;  // event before the view is shown
import com.vaadin.flow.router.BeforeEnterObserver; // for redirecting unauthenticated users
import com.vaadin.flow.router.PageTitle; //browser tab title
import com.vaadin.flow.router.Route;  // maps a URL to this view
import com.vaadin.flow.server.VaadinSession;  //session storage
import java.time.LocalDate; // date
import java.time.format.DateTimeFormatter; // format date as string
import java.util.Map; 
 
//http://localhost:8080/main
@Route("main")
@PageTitle("Energioverblik")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm");


    @Override
    public void beforeEnter(BeforeEnterEvent event) { //runs before the view is shown to check if the user is logged in
        if (VaadinSession.getCurrent().getAttribute("user") == null) {
            event.forwardTo(LoginView.class); // if not logged in, send to login page
        }
    }

    public MainView() { //main view with dashboard and navigation to other views
        EnergyService energyService = ServiceLocator.energy(); // get the EnergyService
        UserService   userService   = ServiceLocator.users();  // get the UserService

        User user = (User) VaadinSession.getCurrent().getAttribute("user"); // logged-in user
        if (user == null) return; //if user is null stop loading the view
        setPadding(true);
        setSpacing(true);


        //Top bar
        H2 title = new H2("Energioverblik");

        Button devicesButton = new Button("Mine enheder"); // go to device management view
        devicesButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        devicesButton.addClickListener(e -> UI.getCurrent().navigate(DeviceView.class));

        Button logoutButton = new Button("Log ud"); // logout button
        logoutButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("user", null); // clear session = logout
            UI.getCurrent().navigate(LoginView.class);
        });

        HorizontalLayout topBar = new HorizontalLayout(title, devicesButton, logoutButton); // put title and buttons in a horizontal row
        topBar.setWidthFull(); 
        topBar.setDefaultVerticalComponentAlignment(Alignment.CENTER); 
        topBar.expand(title); 


        //Electricity price row
        NumberField priceField = new NumberField("Din pris per kWh i dkk"); // input field for the users price
        priceField.setValue(user.getPricePerKWh()); //current price
        priceField.setMin(0.01); 
        priceField.setStep(0.01);

        Button savePriceButton = new Button("Gem pris"); 
        savePriceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        savePriceButton.addClickListener(e -> {
            Double newPrice = priceField.getValue();

            if (newPrice == null || newPrice <= 0) {
                Notification n = Notification.show("ikke gyldig pris", 3000, Notification.Position.MIDDLE); // show error if price is invalid
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            user.setPricePerKWh(newPrice); // update price for user
            userService.save(user); // save user with the new price
            UI.getCurrent().getPage().reload(); // reload page 
        });

        HorizontalLayout priceRow = new HorizontalLayout(priceField, savePriceButton); // put price field and button in a horizontal row
       priceRow.setDefaultVerticalComponentAlignment(Alignment.END); // align button with field 


        // Statistics
        Paragraph todayUsage = new Paragraph( // kWh for today
                "Forbrug i dag: " + String.format("%.2f kWh", energyService.getTodayKWh(user)));
        Paragraph monthUsage = new Paragraph( // kWh for this month
                "Forbrug denne måned: " + String.format("%.2f kWh", energyService.getMonthKWh(user)));
        Paragraph monthCost = new Paragraph( // cost for this month
                "Estimeret måneds kost: " + String.format("%.2f DKK", energyService.getMonthCost(user)));
        Paragraph maxDayUsage = new Paragraph( // highest one-day kWh this month
                "Maksimalt dagsforbrug denne måned: " + String.format("%.2f kWh", energyService.getMaxDayKWhThisMonth(user)));


        // 7-day bar chart
        H3 chartTitle = new H3("Forbrug på de seneste 7 dage"); //chart
        Component weekChart = buildWeekChart(energyService.getLast7DaysKWh(user));


        //Recent readings table
        H3 tableTitle = new H3("Seneste aflæsninger");
        Grid<EnergyReading> readingsTable = new Grid<>(EnergyReading.class, false); // make a table to show recent readings (false so it doesnt generate columns automatically)
        readingsTable.addColumn(r -> r.getDevice().getName()).setHeader("Enhed");
        readingsTable.addColumn(r -> String.format("%.2f kWh", r.getKWh())).setHeader("Forbrug");
        readingsTable.addColumn(r -> String.format("%.2f DKK", r.getKWh() * user.getPricePerKWh())).setHeader("Pris");
        readingsTable.addColumn(r -> r.getRecordedAt().format(DATE_FORMAT)).setHeader("Dato");
        readingsTable.setItems(energyService.getRecentReadings(user));
        readingsTable.setWidthFull();
        readingsTable.setHeight("350px");

        add(topBar, priceRow, todayUsage, monthUsage, monthCost, maxDayUsage, chartTitle, weekChart, tableTitle, readingsTable); // add all components to the view in a vertical layout
    }



    private Component buildWeekChart(Map<LocalDate, Double> dailyKWh) { // build a bar chart from the daily kWh map
        VerticalLayout chart = new VerticalLayout();
        chart.setPadding(false);
        chart.setSpacing(false); 
        chart.setWidthFull();

        double max = dailyKWh.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0); // Max kwh value to scale bars, if Max is 0 use 1 instead to not divide by zero
        //if (max == 0) max = 1.0; 

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM"); // format date as "23/04"  

        for (Map.Entry<LocalDate, Double> entry : dailyKWh.entrySet()) { // for each day and kWh value in the map
            double kwh = entry.getValue();
            double pct = (kwh / max) * 100.0; // bar width as % of biggest bar

            Span dayLabel = new Span(entry.getKey().format(fmt)); 
            dayLabel.getStyle()
                .set("min-width", "55px")
                .set("font-size", "var(--lumo-font-size-s)"); 

            Div bar = new Div();
            bar.getStyle()
                .set("height", "20px")
                .set("width", Math.max(pct, 1) + "%") // at least 1% so every bar is visible
                .set("background", "var(--lumo-primary-color)")
                .set("border-radius", "3px");

            Div barWrapper = new Div(bar);
            barWrapper.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("align-items", "center");

            Span valueLabel = new Span(String.format("%.2f kWh", kwh));
            valueLabel.getStyle()
                .set("min-width", "75px")
                .set("text-align", "right")
                .set("font-size", "var(--lumo-font-size-s)");

            HorizontalLayout row = new HorizontalLayout(dayLabel, barWrapper, valueLabel);
            row.setWidthFull();
            row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            row.setPadding(false);
            row.getStyle().set("margin-bottom", "6px");

            chart.add(row);
        }

        return chart;
    }
}