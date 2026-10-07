package com.example;

public class PricingEngine {

    public enum CustomerTier { STANDARD, SILVER, GOLD }

    private final AuditService auditService;

    // Default constructor for simple usage
    public PricingEngine() {
        this((tier, amount, result) -> {});
    }

    // Constructor with collaborator dependency
    public PricingEngine(AuditService auditService) {
        this.auditService = auditService;
    }

    public double calculateDiscount(double amount, CustomerTier tier) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (tier == null) {
            throw new NullPointerException("Tier cannot be null");
        }

        double rate = switch (tier) {
            case STANDARD -> (amount > 100) ? 0.05 : 0.0;
            case SILVER   -> 0.10;
            case GOLD     -> 0.20;
        };

        double result = Math.round(amount * (1.0 - rate) * 100.0) / 100.0;

        // calling auditservice log function from priceEngine.
        auditService.logCalculation(tier.name(), amount, result);
  
        return result;
    }
}