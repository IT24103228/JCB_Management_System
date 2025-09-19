package com.jcbmanagement.booking.service;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.repository.BookingRepository;
import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.repository.MachineRepository;
import com.jcbmanagement.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MachineRepository machineRepository;

    public Booking createBooking(Booking booking) {
        validateBooking(booking);

        // Check machine availability
        if (!isMachineAvailable(booking.getMachine().getMachineID(),
                booking.getStartDate(), booking.getEndDate())) {
            throw new IllegalArgumentException("Machine is not available for the selected dates");
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByCustomer(User customer) {
        return bookingRepository.findByCustomer(customer);
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            booking.setUpdatedAt(LocalDateTime.now());
            return bookingRepository.save(booking);
        }
        throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
    }

    public Booking cancelBooking(Long bookingId, User customer) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Check if the booking belongs to the customer
            if (!booking.getCustomer().getUserID().equals(customer.getUserID())) {
                throw new SecurityException("You can only cancel your own bookings");
            }

            // Check if booking can be cancelled
            if (booking.getStatus() == Booking.BookingStatus.COMPLETED ||
                    booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                throw new IllegalArgumentException("Cannot cancel a " + booking.getStatus().name().toLowerCase() + " booking");
            }

            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            return bookingRepository.save(booking);
        }
        throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
    }

    public void deleteBooking(Long bookingId, User customer) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Check if the booking belongs to the customer
            if (!booking.getCustomer().getUserID().equals(customer.getUserID())) {
                throw new SecurityException("You can only delete your own bookings");
            }

            // Only allow deletion of cancelled bookings
            if (booking.getStatus() != Booking.BookingStatus.CANCELLED) {
                throw new IllegalArgumentException("Only cancelled bookings can be deleted");
            }

            bookingRepository.deleteById(bookingId);
        } else {
            throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
        }
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(Booking.BookingStatus.PENDING);
    }

    public long countBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status).size();
    }

    public long countTotalBookings() {
        return bookingRepository.count();
    }

    private void validateBooking(Booking booking) {
        if (booking.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        if (booking.getEndDate().isBefore(booking.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    private boolean isMachineAvailable(Long machineId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                machineId, startDate, endDate);
        return conflictingBookings.isEmpty();
    }
}
