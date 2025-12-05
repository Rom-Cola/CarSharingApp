package com.loievroman.carsharingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.CarType;
import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.PaymentStatus;
import com.loievroman.carsharingapp.model.PaymentType;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user1;
    private User user2;
    private Payment p1;
    private Payment p2;
    private Payment p3;

    @BeforeEach
    void setUp() {
        User admin = em.getEntityManager().find(User.class, 1L);
        if (admin != null) {
            em.getEntityManager()
                    .createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 2")
                    .executeUpdate();
        }

        user1 = new User();
        user1.setEmail("u1@example.com");
        user1.setFirstName("U1");
        user1.setLastName("L1");
        user1.setPassword("pass1");
        user1.setRoles(Set.of());
        em.persist(user1);

        user2 = new User();
        user2.setEmail("u2@example.com");
        user2.setFirstName("U2");
        user2.setLastName("L2");
        user2.setPassword("pass2");
        user2.setRoles(Set.of());
        em.persist(user2);

        Car car = new Car();
        car.setModel("Model");
        car.setBrand("Brand");
        car.setType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(new BigDecimal("10.00"));
        em.persist(car);

        Rental r1 = new Rental();
        r1.setCar(car);
        r1.setUser(user1);
        r1.setRentalDate(LocalDate.now().minusDays(3));
        r1.setReturnDate(LocalDate.now().plusDays(3));
        em.persist(r1);

        Rental r2 = new Rental();
        r2.setCar(car);
        r2.setUser(user1);
        r2.setRentalDate(LocalDate.now().minusDays(2));
        r2.setReturnDate(LocalDate.now().plusDays(2));
        em.persist(r2);

        Rental r3 = new Rental();
        r3.setCar(car);
        r3.setUser(user2);
        r3.setRentalDate(LocalDate.now().minusDays(1));
        r3.setReturnDate(LocalDate.now().plusDays(1));
        em.persist(r3);

        p1 = new Payment();
        p1.setStatus(PaymentStatus.PENDING);
        p1.setType(PaymentType.PAYMENT);
        p1.setRental(r1);
        p1.setAmountToPay(new BigDecimal("100.00"));
        em.persist(p1);

        p2 = new Payment();
        p2.setStatus(PaymentStatus.PAID);
        p2.setType(PaymentType.FINE);
        p2.setRental(r2);
        p2.setAmountToPay(new BigDecimal("50.00"));
        em.persist(p2);

        p3 = new Payment();
        p3.setStatus(PaymentStatus.PENDING);
        p3.setType(PaymentType.PAYMENT);
        p3.setRental(r3);
        p3.setAmountToPay(new BigDecimal("75.00"));
        em.persist(p3);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findByUserId returns only payments for given user and supports sorting")
    void findByUserId_filtersByUser_andSortsByIdAsc() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Page<Payment> page = paymentRepository.findByUserId(user1.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        List<Long> ids = page.getContent().stream().map(Payment::getId).toList();
        assertThat(ids).containsExactly(p1.getId(), p2.getId());
        assertThat(page.getContent()).allMatch(
                p -> p.getRental().getUser().getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("findByUserId supports pagination")
    void findByUserId_paginates() {
        Pageable first = PageRequest.of(0, 1, Sort.by("id").ascending());
        Pageable second = PageRequest.of(1, 1, Sort.by("id").ascending());

        Page<Payment> page1 = paymentRepository.findByUserId(user1.getId(), first);
        Page<Payment> page2 = paymentRepository.findByUserId(user1.getId(), second);

        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).getId()).isEqualTo(p1.getId());

        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.getContent().get(0).getId()).isEqualTo(p2.getId());
    }

    @Test
    @DisplayName("findByUserId returns empty page when user has no payments")
    void findByUserId_emptyForUserWithoutPayments() {
        User user3 = new User();
        user3.setEmail("u3@example.com");
        user3.setFirstName("U3");
        user3.setLastName("L3");
        user3.setPassword("pass3");
        user3.setRoles(Set.of());
        em.persist(user3);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").ascending());
        Page<Payment> page = paymentRepository.findByUserId(user3.getId(), pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}
