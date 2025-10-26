package org.example;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

// ==========================================================
// 🧪 Combined JUnit 5 Tests for Real Estate Project
// ==========================================================

class RealEstateTest {

    @Test
    void testGetTotalPrice_WithCityModifiers() {
        RealEstate budapest = new RealEstate("Budapest", 200000, 100, 4, Genre.CONDOMINIUM);
        RealEstate debrecen = new RealEstate("Debrecen", 200000, 100, 4, Genre.CONDOMINIUM);
        RealEstate nyiregyhaza = new RealEstate("Nyíregyháza", 200000, 100, 4, Genre.CONDOMINIUM);
        RealEstate kisvarda = new RealEstate("Kisvárda", 200000, 100, 4, Genre.CONDOMINIUM);

        assertEquals(26000000, budapest.getTotalPrice());
        assertEquals(24000000, debrecen.getTotalPrice());
        assertEquals(23000000, nyiregyhaza.getTotalPrice());
        assertEquals(20000000, kisvarda.getTotalPrice());
    }

    @Test
    void testAverageSqmPerRoom() {
        RealEstate property = new RealEstate("Budapest", 250000, 120, 4, Genre.CONDOMINIUM);
        assertEquals(30.0, property.averageSqmPerRoom());
    }

    @Test
    void testMakeDiscount() {
        RealEstate property = new RealEstate("Budapest", 200000, 100, 4, Genre.CONDOMINIUM);
        property.makeDiscount(10);
        assertEquals(180000, property.getPrice(), 0.01);
    }

    @Test
    void testCompareTo_SortsByTotalPrice() {
        RealEstate cheap = new RealEstate("Debrecen", 100000, 50, 2, Genre.CONDOMINIUM);
        RealEstate expensive = new RealEstate("Budapest", 300000, 100, 4, Genre.CONDOMINIUM);
        assertTrue(cheap.compareTo(expensive) < 0);
    }
}

// ==========================================================
// 🧪 Panel Tests
// ==========================================================
class PanelTest {

    @Test
    void testGetTotalPrice_WithModifiers() {
        // base price = 180000 * 70 * 1.3 (Budapest) = 16,380,000
        Panel lowFloor = new Panel("Budapest", 180000, 70, 3, Genre.CONDOMINIUM, 1, true);
        Panel highFloor = new Panel("Budapest", 180000, 70, 3, Genre.CONDOMINIUM, 10, false);

        // Low floor + insulated: +5% +5% = +10%
        assertEquals((int)(16380000 * 1.10), lowFloor.getTotalPrice());

        // High floor (-5%)
        assertEquals((int)(16380000 * 0.95), highFloor.getTotalPrice());
    }

    @Test
    void testHasSameAmount() {
        RealEstate estate = new RealEstate("Budapest", 200000, 50, 2, Genre.CONDOMINIUM);
        Panel panel = new Panel("Budapest", 200000, 50, 2, Genre.CONDOMINIUM, 2, false);
        assertFalse(panel.hasSameAmount(estate));
    }

    @Test
    void testRoomPriceCalculation() {
        Panel panel = new Panel("Debrecen", 120000, 60, 3, Genre.CONDOMINIUM, 3, false);
        // base price = 120000 * 60 = 7,200,000 / 3 = 2,400,000 per room
        assertEquals(2400000, panel.roomPrice());
    }
}

// ==========================================================
// 🧪 RealEstateAgent Tests
// ==========================================================
class RealEstateAgentTest {

    @BeforeEach
    void setup() {
        RealEstateAgent.getRealEstateCollection().clear();
        RealEstateAgent.addProperty(new RealEstate("Budapest", 200000, 100, 4, Genre.CONDOMINIUM));
        RealEstateAgent.addProperty(new RealEstate("Debrecen", 150000, 80, 3, Genre.FAMILYHOUSE));
        RealEstateAgent.addProperty(new Panel("Budapest", 180000, 70, 3, Genre.CONDOMINIUM, 4, true));
    }

    @Test
    void testAddPropertyIncreasesCollection() {
        int before = RealEstateAgent.getCollectionSize();
        RealEstateAgent.addProperty(new RealEstate("Nyíregyháza", 100000, 60, 2, Genre.FARM));
        assertEquals(before + 1, RealEstateAgent.getCollectionSize());
    }

    @Test
    void testGenerateAnalysisReportCreatesFile() {
        String filename = "testReport.txt";
        RealEstateAgent.generateAnalysisReport(filename);
        File report = new File(filename);
        assertTrue(report.exists());
        assertTrue(report.length() > 0);
        report.delete(); // cleanup
    }

    @Test
    void testGetCollectionSize() {
        assertEquals(3, RealEstateAgent.getCollectionSize());
    }
}
