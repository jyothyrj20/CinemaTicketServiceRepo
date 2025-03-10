package uk.gov.dwp.uc.pairtest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for the {@link TicketServiceImpl} class.
 * <p>
 * These tests verify the functionality of ticket purchasing operations,
 * including payment processing and seat reservations, and ensure that
 * business rules are enforced correctly.
 * </p>
 * @author Jyothy Rajan
 */
@RunWith(MockitoJUnitRunner.class)
class TicketServiceImplTest {

        @InjectMocks
        private TicketServiceImpl ticketServiceImpl;

        @Mock
        private TicketPaymentService ticketPaymentService;

        @Mock
        private SeatReservationService seatReservationService;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }
    /**
     * Tests that a valid ticket purchase request processes successfully,
     * triggering payment and seat reservation services with expected parameters.
     *
     * @throws InvalidPurchaseException if the purchase request is invalid
     */
        @Test
        public void purchaseTickets_ValidRequest_Success() throws InvalidPurchaseException {
            // Arrange
            Long accountId = 7L;
            TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

            // Act
            ticketServiceImpl.purchaseTickets(accountId, ticketTypeRequest);

            // Assert
            verify(ticketPaymentService, times(1)).makePayment(accountId, 50); // 2 tickets * 20 each
            verify(seatReservationService, times(1)).reserveSeat(accountId, 2);
        }
    /**
     * Tests that a purchase request without adult tickets throws an {@link  InvalidPurchaseException},
     * enforcing the rule that child and infant tickets require at least one adult ticket.
     *
     * @throws InvalidPurchaseException if the purchase request is invalid
     */
        @Test
        public void purchaseTickets_NoAdultTickets_ThrowsException() throws InvalidPurchaseException {
            // Arrange
            Long accountId = 7L;
            TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

            InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
                ticketServiceImpl.purchaseTickets(accountId, childTicketRequest);

            });

            assertEquals("Child and Infant tickets require at least one Adult ticket.", exception.getMessage());
        }
    /**
     * Tests that a purchase request exceeding the maximum allowed number of tickets
     * throws an {@link InvalidPurchaseException}, enforcing the ticket limit rule.
     */
        @Test
        public void purchaseTickets_TooManyTickets_ThrowsException() {
            // Arrange
            Long accountId = 7L;
            TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

            // Act & Assert
            InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
                ticketServiceImpl.purchaseTickets(accountId, adultTicketRequest);
            });

            assertEquals("Cannot purchase more than 25 tickets at a time.", exception.getMessage());
        }

    /**
     * Tests that a purchase request with an invalid account ID throws an {@link InvalidPurchaseException},
     * enforcing the rule that account IDs must be valid.
     */
        @Test
        public void purchaseTickets_InvalidAccountId_ThrowsException() {
            // Arrange
            Long invalidAccountId = -1L;
            TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

            // Act & Assert
            InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
                ticketServiceImpl.purchaseTickets(invalidAccountId, adultTicketRequest);
            });

            assertEquals("Account ID is not valid.", exception.getMessage());
        }


}