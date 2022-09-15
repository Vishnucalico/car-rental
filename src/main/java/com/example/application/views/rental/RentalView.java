package com.example.application.views.rental;

import com.example.application.data.entity.Car;
import com.example.application.data.service.CarService;
import com.example.application.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Rental")
@Route(value = "Rental/:carID?/:action?(edit)", layout = MainLayout.class)
@AnonymousAllowed
public class RentalView extends Div implements BeforeEnterObserver {

    private final String CAR_ID = "carID";
    private final String CAR_EDIT_ROUTE_TEMPLATE = "Rental/%s/edit";

    private Grid<Car> grid = new Grid<>(Car.class, false);

    CollaborationAvatarGroup avatarGroup;

    private TextField carName;
    private TextField carType;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<Car> binder;

    private Car car;

    private final CarService carService;

    @Autowired
    public RentalView(CarService carService) {
        this.carService = carService;
        addClassNames("rental-view");

        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set("visibility", "hidden");

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("carName").setAutoWidth(true);
        grid.addColumn("carType").setAutoWidth(true);
        grid.setItems(query -> carService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(CAR_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(RentalView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(Car.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.car == null) {
                    this.car = new Car();
                }
                binder.writeBean(this.car);

                carService.update(this.car);
                clearForm();
                refreshGrid();
                Notification.show("Car details stored.");
                UI.getCurrent().navigate(RentalView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the car details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> carId = event.getRouteParameters().get(CAR_ID).map(UUID::fromString);
        if (carId.isPresent()) {
            Optional<Car> carFromBackend = carService.get(carId.get());
            if (carFromBackend.isPresent()) {
                populateForm(carFromBackend.get());
            } else {
                Notification.show(String.format("The requested car was not found, ID = %d", carId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(RentalView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        carName = new TextField("Car Name");
        carType = new TextField("Car Type");
        Component[] fields = new Component[]{carName, carType};

        formLayout.add(fields);
        editorDiv.add(avatarGroup, formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Car value) {
        this.car = value;
        String topic = null;
        if (this.car != null && this.car.getId() != null) {
            topic = "car/" + this.car.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.car);
        avatarGroup.setTopic(topic);

    }
}