package com.example;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// mockito for behaviour verification of the class
// without mockito only output verification is happening
// Never mock the class want to test using mockito, only the dependencies.
// Here junit test is given for the class we are testing which is PricingEngine
// Mockito is using to test the dependend class like AuditService
// If you want to mock PricingEngine, then there must another class using PricingEngine
@ExtendWith(MockitoExtension.class)
@DisplayName("PricingEngine Comprehensive Test Suite with Mockito")
class PricingEngineTest {

    // Fake object 
    @Mock
    private AuditService auditService;

    // Automatically injects the mocked AuditService into PricingEngine
    @InjectMocks
    private PricingEngine engine;

    // -------------------------------------------------------------
    // 1. DATA-DRIVEN: CSV Generation
    // -------------------------------------------------------------
    @ParameterizedTest(name = "Amount {0} with Tier {1} => Expected {2}")
    @CsvSource({
        "50.0,  STANDARD, 50.0",
        "150.0, STANDARD, 142.5",
        "100.0, SILVER,   90.0",
        "100.0, GOLD,     80.0"
    })
    // maps parameters by position comma seperated from csv source
    void shouldCalculateDiscountsAcrossTiers(double amount, PricingEngine.CustomerTier tier, double expected) {
        double result = engine.calculateDiscount(amount, tier);
        // allowing difference upto 0.01
        assertEquals(expected, result, 0.01);

        // Mockito verify: ensure audit logging was invoked once per run
        verify(auditService, times(1)).logCalculation(tier.name(), amount, expected);
    }

    // -------------------------------------------------------------
    // 2. DATA-DRIVEN: Programmatic Stream Generation
    // -------------------------------------------------------------
    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    @DisplayName("Should throw IllegalArgumentException on negative values")
    void shouldRejectNegativeAmounts(double invalidAmount) {
        assertThrows(IllegalArgumentException.class, () -> 
            engine.calculateDiscount(invalidAmount, PricingEngine.CustomerTier.STANDARD)
        );

        // Verification: ensure no audit log is sent when validation fails
        verifyNoInteractions(auditService);
    }

    // Input provider to be set in method source annotation
    static Stream<Double> invalidInputProvider() {
        return Stream.of(-0.01, -1.0, -100.0, Double.NEGATIVE_INFINITY);
    }

    // -------------------------------------------------------------
    // 3. DYNAMIC TEST FACTORY: Generated at Runtime
    // -------------------------------------------------------------
    @TestFactory
    @DisplayName("Dynamically generate tests for all Tier combinations")
    Stream<DynamicTest> generateDynamicTierTests() {
        List<PricingEngine.CustomerTier> tiers = List.of(
            PricingEngine.CustomerTier.STANDARD,
            PricingEngine.CustomerTier.SILVER,
            PricingEngine.CustomerTier.GOLD
        );

        return tiers.stream().map(tier -> 
            DynamicTest.dynamicTest("Test zero amount for tier " + tier, () -> {

                AuditService localAudit = Mockito.mock(AuditService.class);
                PricingEngine localEngine = new PricingEngine(localAudit);

                double discounted = localEngine.calculateDiscount(0.0, tier);
                
                assertEquals(0.0, discounted, "Discount on zero must be zero");

                verify(localAudit).logCalculation(tier.name(), 0.0, 0.0);
            })
        );
    }

    // -------------------------------------------------------------
    // 4. PROPERTY-BASED: Auto-generated inputs (jqwik engine)
    // -------------------------------------------------------------
    // Run this test 200 times with different randomly generated inputs.
    @Property(tries = 200)
    void discountShouldNeverIncreaseOriginalPrice(
            @ForAll @Positive double amount, 
            @ForAll PricingEngine.CustomerTier tier) {
        
        // Use a no-op mock or standalone instance for fast property execution
        AuditService noOpAudit = Mockito.mock(AuditService.class);
        PricingEngine propertyEngine = new PricingEngine(noOpAudit);

        double discounted = propertyEngine.calculateDiscount(amount, tier);
        
        // Assert invariants: final price must be <= original price
        assertThat(discounted).isLessThanOrEqualTo(amount);

        verify(noOpAudit, atLeastOnce()).logCalculation(anyString(), anyDouble(), anyDouble());
    }
}