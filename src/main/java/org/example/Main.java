package org.example;

import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Directory project = createDemoProject();

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);

        SecurityAuditVisitor securityAudit = new SecurityAuditVisitor();
        project.accept(securityAudit);

        QualityGateVisitor qualityGate = new QualityGateVisitor(statistics, securityAudit);
        project.accept(qualityGate);

        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(compliance);

        TechnicalDebtVisitor technicalDebt = new TechnicalDebtVisitor();
        project.accept(technicalDebt);

        System.out.println("=== Smart Code Inspection & Compliance Engine ===");
        System.out.println(statistics.getReport());
        System.out.println(securityAudit.getReport());
        System.out.println(qualityGate.getReport());
        System.out.println(compliance.getReport());
        System.out.println(technicalDebt.getReport());
    }

    private static Directory createDemoProject() {
        Directory project = new Directory("fintech-project");
        Directory source = new Directory("src");
        source.add(new SourceCodeFile("src/PaymentService.java", """
                package fintech;
                public class PaymentService {
                    public void processPayment() {
                        validate();
                        charge();
                        record();
                    }
                    private void validate() {}
                    private void charge() {}
                    private void record() {}
                }
                """, 8, false));
        source.add(new SourceCodeFile("src/UserService.java", """
                package fintech;
                public class UserService {
                    public boolean isActive(boolean enabled) {
                        return enabled;
                    }
                }
                """, 6, false));
        project.add(source);

        Directory tests = new Directory("tests");
        tests.add(new TestFile("tests/PaymentServiceTest.java", """
                package fintech;
                class PaymentServiceTest {
                    void paymentCanBeProcessed() {
                        new PaymentService().processPayment();
                    }
                }
                """, List.of("PaymentService")));
        project.add(tests);

        Directory config = new Directory("config");
        config.add(new ConfigurationFile("config/application.properties", """
                server.ssl.enabled=true
                security.require-https=true
                cookie.secure=true
                """));
        project.add(config);
        project.add(new DependencyFile("pom.xml", List.of(
                new Dependency("org.junit.jupiter:junit-jupiter", "5.10.2", false, false),
                new Dependency("org.slf4j:slf4j-api", "2.0.13", false, false))));
        return project;
    }
}
