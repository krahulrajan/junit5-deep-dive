
## Unit test in java

Parameterized test using,

1. DATA-DRIVEN: CSV Generation
2. DATA-DRIVEN: Programmatic Stream Generation -- method source to provide input
3. DYNAMIC TEST FACTORY: Generated at Runtime -- with TestFactory annotation
4. PROPERTY-BASED: Auto-generated inputs (jqwik engine) -- random inputs generated at configured number of times

**Mockito**
Mockito is using to test the dependend class. 
Never mock the class want to test using mockito, only the dependencies.
