/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ticketmonster.orders.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.ticketmonster.orders.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@CrossOrigin
@RestController
@RequestMapping("/rest/bookings")
public class OrdersService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    SeatAllocationService seatAllocationService;

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @Transactional
    public Booking getBooking(@PathVariable("id") String id) {
        Long longId = Long.parseLong(id);
        Booking booking = null;

        try {
            booking = (Booking) entityManager.createQuery(
                "SELECT b FROM Booking b " +
                "WHERE b.id = :id")
                .setParameter("id", longId)
                .getSingleResult();
        } catch (NoResultException noSectionEx) {
            System.out.println("Error: SQL query did not return a result.");
            entityManager.flush();
        }
        
        return booking;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Booking createBooking(@RequestBody BookingRequested bookingRequest) {
        try {
            Set<Long> ticketPriceIds = bookingRequest.getUniqueTicketPriceIds();
            Map<Long, TicketPriceGuide> ticketPricesById = loadTicketPrices(ticketPriceIds);

            // todo lookup the performance and make sure it's valid as well as get its name
            PerformanceId performance = new PerformanceId(bookingRequest.getPerformance(), "Hardcoded Perf Name -- need to lookup");

            Booking booking = new Booking();
            booking.setContactEmail(bookingRequest.getEmail());
            booking.setPerformanceId(performance);
            booking.setCancellationCode("abc");

            Map<Section, Map<TicketCategory, TicketRequest>> ticketRequestsPerSection = new TreeMap<Section, Map<TicketCategory, TicketRequest>>(SectionComparator.instance());

            for (TicketRequest ticketRequest : bookingRequest.getTicketRequests()) {
                final TicketPriceGuide ticketPriceGuide = ticketPricesById.get(ticketRequest.getTicketPriceGuideId());

                if (!ticketRequestsPerSection.containsKey(ticketPriceGuide.getSection())) {
                    ticketRequestsPerSection.put(ticketPriceGuide.getSection(), new HashMap<TicketCategory, TicketRequest>());
                }
                ticketRequestsPerSection.get(ticketPriceGuide.getSection()).put(extractTicketCategory(ticketPricesById, ticketRequest), ticketRequest);
            }

            Map<Section, AllocatedSeats> allocatedSeatsPerSection = new TreeMap<Section, AllocatedSeats>(SectionComparator.instance());
            List<Section> failedSections = new ArrayList<Section>();

            for (Section section : ticketRequestsPerSection.keySet()) {
                int totalTicketsRequestedPerSection = 0;
               
                final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                for (TicketRequest ticketRequest : ticketRequestsByCategories.values()) {
                    totalTicketsRequestedPerSection += ticketRequest.getQuantity();
                }

                AllocatedSeats allocatedSeats = seatAllocationService.allocateSeats(section, performance, totalTicketsRequestedPerSection, true);
                if (allocatedSeats.getSeats().size() == totalTicketsRequestedPerSection) {
                    allocatedSeatsPerSection.put(section, allocatedSeats);
                } else {
                    failedSections.add(section);
                }
            }

            if (failedSections.isEmpty()) {

                for (Section section : allocatedSeatsPerSection.keySet()) {
                    final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                    AllocatedSeats allocatedSeats = allocatedSeatsPerSection.get(section);
                    allocatedSeats.markOccupied();
                    int seatCounter = 0;

                    for (TicketCategory ticketCategory : ticketRequestsByCategories.keySet()) {
                        final TicketRequest ticketRequest = ticketRequestsByCategories.get(ticketCategory);
                        final TicketPriceGuide ticketPriceGuide = ticketPricesById.get(ticketRequest.getTicketPriceGuideId());
                        for (int i = 0; i < ticketRequest.getQuantity(); i++) {
                            Ticket ticket = new Ticket(allocatedSeats.getSeats().get(seatCounter + i), ticketCategory, ticketPriceGuide.getPrice());
                            booking.getTickets().add(ticket);
                        }
                        seatCounter += ticketRequest.getQuantity();
                    }
                }

                if (bookingRequest.isSynthetic()) {
                    SyntheticBooking syntheticBooking = new SyntheticBooking(booking);
                    return syntheticBooking;
                }
                else {
                    System.out.println("Persist the request");
                    entityManager.persist(booking);
                    return booking;
                }

            } else {
                // TODO ceposta: we need to change this so we still allocate some? and report back how many we got?
                Map<String, Object> responseEntity = new HashMap<String, Object>();
                responseEntity.put("errors", Collections.singletonList("Cannot allocate the requested number of seats!"));
                throw new RestServiceException(responseEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errors = new HashMap<String, Object>();
            errors.put("errors", Collections.singletonList(e.getMessage()));
            errors.put("stacktrace", Collections.singletonList(getStackTrace(e)));
            System.out.println(e.getMessage());
            throw new RestServiceException(errors);
        }
    }

    private TicketCategory extractTicketCategory(Map<Long, TicketPriceGuide> ticketPricesById, TicketRequest ticketRequest) {
        return ticketPricesById.get(ticketRequest.getTicketPriceGuideId()).getTicketCategory();
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private Map<Long, TicketPriceGuide> loadTicketPrices(Set<Long> priceCategoryIds) {

        List<TicketPriceGuide> ticketPriceGuides = (List<TicketPriceGuide>) entityManager
                .createQuery("select p from TicketPriceGuide p where p.id in :ids", TicketPriceGuide.class)
                .setParameter("ids", priceCategoryIds).getResultList();

        Map<Long, TicketPriceGuide> ticketPricesById = new HashMap<Long, TicketPriceGuide>();
        for (TicketPriceGuide ticketPriceGuide : ticketPriceGuides) {
            System.out.println("Ticket Price Guide: "+ticketPriceGuide.getId());
            ticketPricesById.put(ticketPriceGuide.getId(), ticketPriceGuide);
        }

        return ticketPricesById;
    }
}
