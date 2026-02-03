package com.trustbridge.architectureTest;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

// 1. Tell ArchUnit where to look (your package)
@AnalyzeClasses(packages = "com.trustbridge", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // -----------------------------------------------------------
    // RULE 1: THE HYBRID STRUCTURE DEFENSE
    // -----------------------------------------------------------
    @ArchTest
    static final ArchRule enforce_layered_architecture = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.trustbridge..")

            // A. Define the Layers (The Map)
            .layer("Domain").definedBy("..domain..")
            .layer("Engine").definedBy("..engine..")
            .layer("Features").definedBy("..features..")
            .layer("Integrations").definedBy("..integrations..")
            .layer("Config").definedBy("..config..")

            // B. Define the Rules (The Laws)

            // "Domain" is the Foundation. Everyone can see it, but it sees no one.
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Engine", "Features", "Integrations", "Config")

            // "Engine" is the Brain. It is pure logic.
            // It MUST NOT know about external APIs (Integrations) or Web Controllers (Features).
            .whereLayer("Engine").mayOnlyBeAccessedByLayers("Features", "Integrations", "Config")

            // "Integrations" are the Rails.
            // They can only be used by Features (to execute work) or Config (setup).
            .whereLayer("Integrations").mayOnlyBeAccessedByLayers("Features", "Config")

            // "Features" are the User Interface.
            // Nobody depends on Features. They are the top of the food chain.
            .whereLayer("Features").mayNotBeAccessedByAnyLayer();


    // -----------------------------------------------------------
    // RULE 2: THE "ANTI-SHORTCUT" CHECKS
    // -----------------------------------------------------------

    // Prevent "cycle" dependencies (A -> B -> A) which cause infinite loops and memory leaks.
    @ArchTest
    static final ArchRule no_cycles = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
            .matching("com.trustbridge.(*)..")
            .should().beFreeOfCycles();

    // -----------------------------------------------------------
    // RULE 3: PROFESSIONAL CODING STANDARDS
    // -----------------------------------------------------------

    // Don't throw "throw new Exception()". Be specific (e.g., "JobNotFoundException").
    @ArchTest
    static final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    // Use SLF4J (standard), not java.util.logging (old/weird).
    @ArchTest
    static final ArchRule use_correct_logger = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

}
