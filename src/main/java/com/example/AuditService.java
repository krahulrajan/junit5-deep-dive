package com.example;

// Only interface is needed mockito will implement a test class in runtime
public interface AuditService {
    void logCalculation(String tier, double amount, double result);
}