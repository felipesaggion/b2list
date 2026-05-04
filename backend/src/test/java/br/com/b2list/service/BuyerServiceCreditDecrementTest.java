package br.com.b2list.service;

import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.repository.BuyerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Testes de Atomicidade - Decremento de Crédito")
@Slf4j
public class BuyerServiceCreditDecrementTest {

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private BuyerRepository buyerRepository;

    private Buyer testBuyer;
    private final BigDecimal INITIAL_CREDIT = new BigDecimal("1000.00");

    @BeforeEach
    public void setUp() {
        testBuyer = new Buyer();
        testBuyer.setId(UUID.randomUUID());
        testBuyer.setName("Test Buyer");
        testBuyer.setExternalReference("TEST_BUYER_" + System.currentTimeMillis());
        testBuyer.setCreditLimit(INITIAL_CREDIT);
        testBuyer.setTenantCode("TEST_TENANT");
        testBuyer.setEnabled(true);
        testBuyer.setCreatedAt(OffsetDateTime.now());
        testBuyer.setLastModified(OffsetDateTime.now());

        testBuyer = buyerRepository.save(testBuyer);
    }

    @Test
    @DisplayName("Deve lançar exceção quando crédito é insuficiente")
    @Transactional
    public void testInsufficientCredit() {
        BigDecimal decrementAmount = new BigDecimal("1500.00"); // Maior que o crédito

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> buyerService.decrementCreditAtomically(testBuyer.getId(), decrementAmount)
        );

        assertTrue(exception.getMessage().contains("Crédito insuficiente"));

        Buyer buyerAfter = buyerRepository.findById(testBuyer.getId()).orElse(null);
        assertNotNull(buyerAfter);
        assertEquals(INITIAL_CREDIT, buyerAfter.getCreditLimit());
    }

    @Test
    @DisplayName("Deve decrementar crédito múltiplas vezes corretamente")
    @Transactional
    public void testMultipleCreditDecrements() {
        BigDecimal amount1 = new BigDecimal("300.00");
        BigDecimal amount2 = new BigDecimal("200.00");
        BigDecimal amount3 = new BigDecimal("150.00");

        Buyer buyer1 = buyerService.decrementCreditAtomically(testBuyer.getId(), amount1);
        Buyer buyer2 = buyerService.decrementCreditAtomically(buyer1.getId(), amount2);
        Buyer buyer3 = buyerService.decrementCreditAtomically(buyer2.getId(), amount3);

        BigDecimal expectedFinal = INITIAL_CREDIT
                .subtract(amount1)
                .subtract(amount2)
                .subtract(amount3);

        assertEquals(expectedFinal, buyer3.getCreditLimit());
        assertEquals(new BigDecimal("350.00"), buyer3.getCreditLimit());
    }

    @Test
    @DisplayName("Deve ser à prova de race condition com múltiplas threads")
    public void testConcurrentCreditDecrement() throws InterruptedException {
        int threadCount = 5;
        BigDecimal decrementAmountPerThread = new BigDecimal("100.00");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // Para iniciar todos ao mesmo tempo
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Aguarda sinal para começar

                    buyerService.decrementCreditAtomically(
                            testBuyer.getId(),
                            decrementAmountPerThread
                    );

                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        Thread.sleep(100);
        startLatch.countDown();

        endLatch.await();
        executorService.shutdown();

        assertEquals(threadCount, successCount.get() + failureCount.get(),
                "Total de requisições não corresponde");

        Buyer finalBuyer = buyerRepository.findById(testBuyer.getId()).orElse(null);
        assertNotNull(finalBuyer);

        BigDecimal expectedFinalCredit = INITIAL_CREDIT
                .subtract(decrementAmountPerThread.multiply(BigDecimal.valueOf(successCount.get())));

        assertEquals(expectedFinalCredit, finalBuyer.getCreditLimit(),
                String.format("Crédito final incorreto. Sucesso: %d, Falha: %d",
                        successCount.get(), failureCount.get()));
    }

    @Test
    @DisplayName("Deve ser à prova de race condition com alta contenção")
    public void testHighContention() throws InterruptedException {

        int threadCount = 10;
        BigDecimal smallDecrement = new BigDecimal("50.00"); // 10 × 50 = 500
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    buyerService.decrementCreditAtomically(testBuyer.getId(), smallDecrement);
                    successCount.incrementAndGet();
                } catch (InterruptedException exception) {
                    log.error(exception.getMessage(), exception);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        Buyer finalBuyer = buyerRepository.findById(testBuyer.getId()).orElse(null);
        assertNotNull(finalBuyer);

        BigDecimal expectedCredit = INITIAL_CREDIT
                .subtract(smallDecrement.multiply(BigDecimal.valueOf(successCount.get())));

        assertEquals(expectedCredit, finalBuyer.getCreditLimit());

        log.info("✅ Teste de alta contenção PASSOU");
        log.info("   - Threads bem-sucedidas: " + successCount.get());
        log.info("   - Crédito final: " + finalBuyer.getCreditLimit());
    }

    @Test
    @DisplayName("Deve lançar exceção para buyer inexistente")
    @Transactional
    public void testNonExistentBuyer() {

        UUID nonExistentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> buyerService.decrementCreditAtomically(nonExistentId, amount)
        );

        assertTrue(exception.getMessage().contains("não encontrado"));
    }
}

