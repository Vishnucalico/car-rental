package com.example.application.views.customer;

import com.example.application.data.entity.Customer;
import com.example.application.data.service.CustomerService;
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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Customer")
@Route(value = "Customer/:customerID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class CustomerView extends Div implements BeforeEnterObserver {

    private final String CUSTOMER_ID = "customerID";
    private final String CUSTOMER_EDIT_ROUTE_TEMPLATE = "Customer/%s/edit";

    private Grid<Customer> grid = new Grid<>(Customer.class, false);

    CollaborationAvatarGroup avatarGroup;

    private TextField customerName;
    private TextField customerType;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<Customer> binder;

    private Customer customer;

    private final CustomerService customerService;

    @Autowired
    public CustomerView(CustomerService customerService) {
        this.customerService = customerService;
        addClassNames("customer-view");

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
        grid.addColumn("customerName").setAutoWidth(true);
        grid.addColumn("customerType").setAutoWidth(true);
        grid.setItems(query -> customerService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(CUSTOMER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CustomerView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(Customer.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.customer == null) {
                    this.customer = new Customer();
                }
                binder.writeBean(this.customer);

                customerService.update(this.customer);
                clearForm();
                refreshGrid();
                Notification.show("Customer details stored.");
                UI.getCurrent().navigate(CustomerView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the customer details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> customerId = event.getRouteParameters().get(CUSTOMER_ID).map(UUID::fromString);
        if (customerId.isPresent()) {
            Optional<Customer> customerFromBackend = customerService.get(customerId.get());
            if (customerFromBackend.isPresent()) {
                populateForm(customerFromBackend.get());
            } else {
                Notification.show(String.format("The requested customer was not found, ID = %d", customerId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CustomerView.class);
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
        customerName = new TextField("Customer Name");
        customerType = new TextField("Customer Type");
        Component[] fields = new Component[]{customerName, customerType};

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

    private void populateForm(Customer value) {
        this.customer = value;
        String topic = null;
        if (this.customer != null && this.customer.getId() != null) {
            topic = "customer/" + this.customer.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.customer);
        avatarGroup.setTopic(topic);

    }
}