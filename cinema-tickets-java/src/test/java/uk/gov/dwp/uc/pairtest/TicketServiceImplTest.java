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