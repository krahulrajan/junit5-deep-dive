package com.example;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PricingEngine Comprehensive Test Suite")
class PricingEngineTest {

    private final PricingEngine engine = new PricingEngine();

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
                double discounted = engine.calculateDiscount(0.0, tier);
                assertEquals(0.0, discounted, "Discount on zero must be zero");
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
        
        double discounted = engine.calculateDiscount(amount, tier);
        
        // Assert invariants: final price must be <= original price
        assertThat(discounted).isLessThanOrEqualTo(amount);
    }
}