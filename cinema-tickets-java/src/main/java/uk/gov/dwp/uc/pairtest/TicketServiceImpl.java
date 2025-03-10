package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

/**
 *Implementation of Ticket service Interface
 * <P>
 *  This class handles ticket purchasing operations, including payment processing
 *  and seat reservations. It ensures that business rules, such as the requirement
 *  for at least one adult ticket per purchase and a maximum of 25 tickets per transaction,
 *  are enforced.
 * </P>
 * @author Jyothy
 */
public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS_ALLOWED = 25;
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }
    /**
     *  This method validates the ticket requests, calculates the total payment amount,reserves the appropriate number of seats, and processes the payment.
     * @param accountId The unique identifier of the account making the purchase.
     * @param ticketTypeRequests detailing the types and quantities of tickets requested.
     * @throws InvalidPurchaseException If the purchase request violates business rules,
     * such as missing adult tickets or exceeding the maximum allowed number of tickets.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID is not valid.");
        }

        int totalTickets = 0;
        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;
        int totalAmount = 0;
        int totalSeatsToReserve = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            int numberOfTickets = request.getNoOfTickets();
            totalTickets += numberOfTickets;

            switch (request.getTicketType()) {
                case ADULT:
                    adultTickets += numberOfTickets;
                    totalAmount += numberOfTickets * ADULT_TICKET_PRICE;
                    totalSeatsToReserve += numberOfTickets;
                    break;
                case CHILD:
                    childTickets += numberOfTickets;
                    totalAmount += numberOfTickets * CHILD_TICKET_PRICE;
                    totalSeatsToReserve += numberOfTickets;
                    break;
                case INFANT:
                    infantTickets += numberOfTickets;
                    // Infants don't require a seat or payment
                    break;
            }
        }

        if (totalTickets > MAX_TICKETS_ALLOWED) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }

        if (adultTickets == 0 && (childTickets > 0 || infantTickets > 0)) {
            throw new InvalidPurchaseException("Child and Infant tickets require at least one Adult ticket.");
        }

        paymentService.makePayment(accountId, totalAmount);
        reservationService.reserveSeat(accountId, totalSeatsToReserve);
    }

}