package org.jboss.examples.ticketmonster.rest;

import org.ff4j.FF4j;
import org.jboss.examples.ticketmonster.model.*;
import org.jboss.examples.ticketmonster.orders.OrdersRequestDTO;
import org.jboss.examples.ticketmonster.service.AllocatedSeats;
import org.jboss.examples.ticketmonster.service.SeatAllocationService;
import org.jboss.examples.ticketmonster.util.qualifier.Cancelled;
import org.jboss.examples.ticketmonster.util.qualifier.Created;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * A JAX-RS endpoint for handling {@link Booking}s. Inherits the GET
 * methods from {@link BaseEntityService}, and implements additional REST methods.
 *
 * @author Marius Bogoevici
 * @author Pete Muir
 */
@Path("/bookings")
@Stateless
public class BookingService extends BaseEntityService<Booking> {

    @Inject
    SeatAllocationService seatAllocationService;

    @Inject
    FF4j ff;

    @Inject @Cancelled
    private Event<Booking> cancelledBookingEvent;

    @Inject @Created
    private Event<Booking> newBookingEvent;

    private String ordersServiceUri = "http://orders-service/rest/bookings";
    
    public BookingService() {
        super(Booking.class);
    }
    
    @DELETE
    public Response deleteAllBookings() {
    	List<Booking> bookings = getAll(new MultivaluedHashMap<String, String>());
    	for (Booking booking : bookings) {
    		deleteBooking(booking.getId());
    	}
        return Response.noContent().build();
    }

     /**
     * A method for retrieving individual entity instances.
     * @param id entity id
     * @return
     */
    @Override
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Booking getSingleInstance(@PathParam("id") Long id) {
        Booking booking = null;

        if (ff.check("orders-internal")) {
            booking =  super.getSingleInstance(id);
        }

        if (ff.check("orders-service")) {
            booking = getBookingOrdersService(id);
        }

        return booking;
    }

    private Booking getBookingOrdersService(Long id) {
        try {
            System.out.println("Calling GET method from endpoint: " + ordersServiceUri);
            Response response = buildClient()
                    .target(this.ordersServiceUri +"/"+ id)
                    .request()
                    .get();
            
            String entity = response.readEntity(String.class);
            System.out.println(">>> "+entity);
            return mapJSONStringToBooking(entity);

        } catch (Exception e) {
            System.out.println("Caught an exception here: "+ e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Booking mapJSONStringToBooking(String entity) {
        Booking booking = new Booking();
            
        JSONObject jsonObj = new JSONObject(entity);
        booking.setId(jsonObj.getLong("id"));
        booking.setContactEmail(jsonObj.getString("contactEmail"));

        Performance performance = getEntityManager().find(Performance.class, jsonObj.getJSONObject("performanceId").getLong("id"));
        booking.setPerformance(performance);

        Set<Ticket> ticketSet = new HashSet<Ticket>();
        JSONArray tickets = jsonObj.getJSONArray("tickets");
        for (int i = 0; i< tickets.length(); i++){
            Ticket ticket = new Ticket();

            JSONObject jsonTicket = tickets.getJSONObject(i);
            ticket.setId(jsonTicket.getLong("id"));
            ticket.setPrice(jsonTicket.getFloat("price"));

            TicketCategory category = new TicketCategory();
            category.setId(jsonTicket.getJSONObject("ticketCategory").getLong("id"));
            category.setDescription(jsonTicket.getJSONObject("ticketCategory").getString("description"));
            ticket.setTicketCategory(category);

            Seat seat = new Seat();
            JSONObject jsonSeat = jsonTicket.getJSONObject("seat");
            seat.setRowNumber(jsonSeat.getInt("rowNumber"));
            seat.setNumber(jsonSeat.getInt("number"));

            Section section = new Section();
            JSONObject jsonSection = jsonSeat.getJSONObject("section");
            section.setId(jsonSection.getLong("id"));
            section.setName(jsonSection.getString("name"));
            section.setDescription(jsonSection.getString("description"));
            section.setNumberOfRows(jsonSection.getInt("numberOfRows"));
            section.setRowCapacity(jsonSection.getInt("rowCapacity"));
            seat.setSection(section);

            ticket.setSeat(seat);

            ticketSet.add(ticket);
        }

        booking.setTickets(ticketSet);

        return booking;
	}

	/**
     * Delete a booking by id
     * @param id
     * @return
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteBooking(@PathParam("id") Long id) {
        Booking booking = getEntityManager().find(Booking.class, id);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        getEntityManager().remove(booking);

        Map<Section, List<Seat>> seatsBySection = new TreeMap<Section, java.util.List<Seat>>(SectionComparator.instance());
        for (Ticket ticket : booking.getTickets()) {
            List<Seat> seats = seatsBySection.get(ticket.getSeat().getSection());
            if (seats == null) {
                seats = new ArrayList<Seat>();
                seatsBySection.put(ticket.getSeat().getSection(), seats);
            }
            seats.add(ticket.getSeat());
        }

        for (Map.Entry<Section, List<Seat>> sectionListEntry : seatsBySection.entrySet()) {
            seatAllocationService.deallocateSeats( sectionListEntry.getKey(),
                    booking.getPerformance(), sectionListEntry.getValue());
        }
        cancelledBookingEvent.fire(booking);
        return Response.noContent().build();
    }

    /**
     * Create a booking. Data is contained in the bookingRequest object
     * @param bookingRequest
     * @return
     */
    @POST
    /**
     * Data is received in JSON format. For easy handling, it will be unmarshalled in the support
     * {@link BookingRequest} class.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBooking(BookingRequest bookingRequest) {
        Response response = null;

        if (ff.check("orders-internal")) {
            response = createBookingInternal(bookingRequest);
            System.out.println("Created internal booking");
        }

        if (ff.check("orders-service")) {
            if (ff.check("orders-internal")) {
                createSyntheticBookingOrdersService(bookingRequest);
            }
            else {
                response = createBookingOrdersService(bookingRequest);
                System.out.println("Created booking by orders-service");
            }
        }
        return response;
    }

    private Client buildClient() {
        String proxyHost = System.getProperty("http.proxyHost");
        Integer proxyPort;
        try {
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
        } catch (NumberFormatException e) {
            proxyPort = null;
        }

        System.out.println("> Using proxy: " + proxyHost + ":" + proxyPort);
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null) {
            return new ResteasyClientBuilder()
                    .defaultProxy(proxyHost, proxyPort).build();
        } else {
            return ClientBuilder.newClient();
        }
    }

    /**
     * Makes a call to the Orders Service, but lets it know that this is a synthetic transaction
     * that has already been recorded (ie, here internally) and is sent just for exercising the orders
     * service; it should roll back or clean up and not store this tx as a real tx
     * @param bookingRequest
     */
    private void createSyntheticBookingOrdersService(BookingRequest bookingRequest) {
        System.out.println("Calling Orders Service with SYNTHETIC TX");
        OrdersRequestDTO ordersRequest = new OrdersRequestDTO(bookingRequest, true);

        try {
            Response response = buildClient()
                    .target(this.ordersServiceUri)
                    .request()
                    .post(Entity.entity(ordersRequest, MediaType.APPLICATION_JSON_TYPE));
            String sytheticResponse = response.readEntity(String.class);
            System.out.println("Response from SYNTHETIC TX: " + sytheticResponse);

        } catch (Exception e) {
            System.out.println("Caught an exception here: "+ e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * This method delegates the booking creation to the Orders Service
     * @param bookingRequest
     * @return
     */
    private Response createBookingOrdersService(BookingRequest bookingRequest) {

        OrdersRequestDTO ordersRequest = new OrdersRequestDTO(bookingRequest, false);

        try {
            Response response = buildClient()
                    .target(this.ordersServiceUri)
                    .request()
                    .post(Entity.entity(ordersRequest, MediaType.APPLICATION_JSON_TYPE));

            Booking booking = new Booking();
            booking.setContactEmail(bookingRequest.getEmail());
            Performance performance = getEntityManager().find(Performance.class, bookingRequest.getPerformance());
            booking.setPerformance(performance);
            booking.setCancellationCode("abc");

            JSONObject jsonObj = new JSONObject(response.readEntity(String.class));
            booking.setId(jsonObj.getLong("id"));
           
            return Response.ok().entity(booking).type(MediaType.APPLICATION_JSON_TYPE).build();

        } catch (Exception e) {
            System.out.println("Caught an exception here: "+ e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * This is the original implementation of creating a booking; relies on internal logic
     * @param bookingRequest
     * @return
     */
    private Response createBookingInternal(BookingRequest bookingRequest) {
        try {
            Set<Long> priceCategoryIds = bookingRequest.getUniquePriceCategoryIds();

            Performance performance = getEntityManager().find(Performance.class, bookingRequest.getPerformance());
            Map<Long, TicketPrice> ticketPricesById = loadTicketPrices(priceCategoryIds);

            Booking booking = new Booking();
            booking.setContactEmail(bookingRequest.getEmail());
            booking.setPerformance(performance);
            booking.setCancellationCode("abc");

            Map<Section, Map<TicketCategory, TicketRequest>> ticketRequestsPerSection = new TreeMap<Section, Map<TicketCategory, TicketRequest>>(SectionComparator.instance());
            
            for (TicketRequest ticketRequest : bookingRequest.getTicketRequests()) {
                final TicketPrice ticketPrice = ticketPricesById.get(ticketRequest.getTicketPrice());
                if (!ticketRequestsPerSection.containsKey(ticketPrice.getSection())) {
                    ticketRequestsPerSection
                            .put(ticketPrice.getSection(), new HashMap<TicketCategory, TicketRequest>());
                }
                ticketRequestsPerSection.get(ticketPrice.getSection()).put(
                        ticketPricesById.get(ticketRequest.getTicketPrice()).getTicketCategory(), ticketRequest);
            }

            Map<Section, AllocatedSeats> seatsPerSection = new TreeMap<Section, AllocatedSeats>(SectionComparator.instance());
            List<Section> failedSections = new ArrayList<Section>();

            for (Section section : ticketRequestsPerSection.keySet()) {
                int totalTicketsRequestedPerSection = 0;

                final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                
                for (TicketRequest ticketRequest : ticketRequestsByCategories.values()) {
                    totalTicketsRequestedPerSection += ticketRequest.getQuantity();
                }

                AllocatedSeats allocatedSeats = seatAllocationService.allocateSeats(section, performance, totalTicketsRequestedPerSection, true);
                if (allocatedSeats.getSeats().size() == totalTicketsRequestedPerSection) {
                    seatsPerSection.put(section, allocatedSeats);
                } else {
                    failedSections.add(section);
                }
            }

            if (failedSections.isEmpty()) {
                for (Section section : seatsPerSection.keySet()) {
                   
                    final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                    AllocatedSeats allocatedSeats = seatsPerSection.get(section);
                    allocatedSeats.markOccupied();
                    int seatCounter = 0;
                    // Now, add a ticket for each requested ticket to the booking
                    for (TicketCategory ticketCategory : ticketRequestsByCategories.keySet()) {
                        final TicketRequest ticketRequest = ticketRequestsByCategories.get(ticketCategory);
                        final TicketPrice ticketPrice = ticketPricesById.get(ticketRequest.getTicketPrice());
                        for (int i = 0; i < ticketRequest.getQuantity(); i++) {
                            Ticket ticket = new Ticket(allocatedSeats.getSeats().get(seatCounter + i), ticketCategory, ticketPrice.getPrice());
                            booking.getTickets().add(ticket);
                        }
                        seatCounter += ticketRequest.getQuantity();
                    }
                }

                // Persist the booking, including cascaded relationships
                booking.setPerformance(performance);
                booking.setCancellationCode("abc");
                getEntityManager().persist(booking);
                newBookingEvent.fire(booking);

                return Response.ok().entity(booking).type(MediaType.APPLICATION_JSON_TYPE).build();

            } else {
                Map<String, Object> responseEntity = new HashMap<String, Object>();
                responseEntity.put("errors", Collections.singletonList("Cannot allocate the requested number of seats!"));
                return Response.status(Response.Status.BAD_REQUEST).entity(responseEntity).build();
            }
        } catch (ConstraintViolationException e) {
        
            Map<String, Object> errors = new HashMap<String, Object>();
            List<String> errorMessages = new ArrayList<String>();
            for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                errorMessages.add(constraintViolation.getMessage());
            }
            errors.put("errors", errorMessages);
            // A WebApplicationException can wrap a response
            // Throwing the exception causes an automatic rollback
            throw new RestServiceException(Response.status(Response.Status.BAD_REQUEST).entity(errors).build());

        } catch (Exception e) {
            Map<String, Object> errors = new HashMap<String, Object>();
            errors.put("errors", Collections.singletonList(e.getMessage()));
            // A WebApplicationException can wrap a response
            // Throwing the exception causes an automatic rollback
            throw new RestServiceException(Response.status(Response.Status.BAD_REQUEST).entity(errors).build());
        }
    }

    /**
     * Utility method for loading ticket prices
     * @param priceCategoryIds
     * @return
     */
    private Map<Long, TicketPrice> loadTicketPrices(Set<Long> priceCategoryIds) {
        List<TicketPrice> ticketPrices = (List<TicketPrice>) getEntityManager()
                .createQuery("select p from TicketPrice p where p.id in :ids", TicketPrice.class)
                .setParameter("ids", priceCategoryIds).getResultList();
        // Now, map them by id
        Map<Long, TicketPrice> ticketPricesById = new HashMap<Long, TicketPrice>();
        for (TicketPrice ticketPrice : ticketPrices) {
            ticketPricesById.put(ticketPrice.getId(), ticketPrice);
        }
        return ticketPricesById;
    }

    public String getOrdersServiceUri() {
        return ordersServiceUri;
    }

    public void setOrdersServiceUri(String ordersServiceUri) {
        this.ordersServiceUri = ordersServiceUri;
    }
}
