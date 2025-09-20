package com.jcbmanagement.booking.repository;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByCustomerUserID(Long customerId);
    
    List<Booking> findByCustomer(User customer);
    
    List<Booking> findByMachineMachineID(Long machineId);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.customer.userID = :customerId ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerUserIDOrdered(@Param("customerId") Long customerId);
    
    @Query("SELECT b FROM Booking b WHERE b.customer = :customer ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerOrdered(@Param("customer") User customer);
    
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.customer c " +
           "JOIN FETCH b.machine m " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllWithCustomerAndMachine();
    
    @Query("SELECT b FROM Booking b WHERE b.machine.machineID = :machineId " +
           "AND b.status != 'CANCELLED' " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(@Param("machineId") Long machineId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.startDate >= :startDate AND b.endDate <= :endDate")
    List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
