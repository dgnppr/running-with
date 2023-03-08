package com.runningwith;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String ROOT_PACKAGE = "com.runningwith";
    private static final String EVENT = "..domain.event..";
    private static final String MAIN = "..domain.main..";
    private static final String STUDY = "..domain.study..";
    private static final String TAG = "..domain.tag..";
    private static final String USERS = "..domain.users..";
    private static final String ZONE = "..domain.zone..";


    @ArchTest
    ArchRule domainPackageRule = classes().that().resideInAPackage(ROOT_PACKAGE + ".domain..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(ROOT_PACKAGE + ".domain..");

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT, MAIN);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, USERS, EVENT);

    @ArchTest
    ArchRule usersPackageRule = classes().that().resideInAPackage(USERS)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, USERS);

    @ArchTest
    ArchRule cycleCheck = slices().matching(ROOT_PACKAGE + ".domain.(*)..")
            .should().beFreeOfCycles();

}
