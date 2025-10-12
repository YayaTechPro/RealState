package org.example;

import java.io.*;
import java.util.*;

// ===== Genre enumeration =====
enum Genre {
    FAMILYHOUSE,
    CONDOMINIUM,
    FARM
}

// ===== PropertyInterface =====
interface PropertyInterface {
    void makeDiscount(int percentage);
    int getTotalPrice();
    double averageSqmPerRoom();
    String toString();
}

// ===== PanelInterface =====
interface PanelInterface {
    boolean hasSameAmount(RealEstate other);
    int roomPrice();
}

// ===================== RealEstate CLASS =====================
class RealEstate implements PropertyInterface, Comparable<RealEstate> {
    protected String city;
    protected double price;       // price per sqm
    protected int sqm;            // size in square meters
    protected int numberOfRooms;  // integer number of rooms
    protected Genre genre;

    // Default constructor
    public RealEstate() {
    }

    // Parameterized constructor
    public RealEstate(String city, double price, int sqm, int numberOfRooms, Genre genre) {
        this.city = city;
        this.price = price;
        this.sqm = sqm;
        this.numberOfRooms = numberOfRooms;
        this.genre = genre;
    }

    // Getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getSqm() { return sqm; }
    public void setSqm(int sqm) { this.sqm = sqm; }

    public int getNumberOfRooms() { return numberOfRooms; }
    public void setNumberOfRooms(int numberOfRooms) { this.numberOfRooms = numberOfRooms; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    // ---------------- Interface Implementations ----------------
    @Override
    public void makeDiscount(int percentage) {
        this.price = this.price * (100 - percentage) / 100.0;
    }

    @Override
    public int getTotalPrice() {
        double basePrice = price * sqm;
        double modifier = 1.0;

        if (city != null) {
            switch (city.toLowerCase()) {
                case "budapest":
                    modifier = 1.30;
                    break;
                case "debrecen":
                    modifier = 1.20;
                    break;
                case "nyíregyháza":
                case "nyiregyhaza":
                    modifier = 1.15;
                    break;
                default:
                    modifier = 1.0;
                    break;
            }
        }
        return (int) (basePrice * modifier);
    }

    @Override
    public double averageSqmPerRoom() {
        if (numberOfRooms == 0) return 0.0;
        return (double) sqm / numberOfRooms;
    }

    @Override
    public String toString() {
        return String.format(
                "RealEstate [City: %s, Genre: %s, Price per sqm: %.2f, Area: %d sqm, Rooms: %d, " +
                        "Total Price: %d, Average sqm per room: %.2f]",
                city, genre, price, sqm, numberOfRooms, getTotalPrice(), averageSqmPerRoom()
        );
    }

    // Comparable implementation for TreeSet
    @Override
    public int compareTo(RealEstate other) {
        int priceComparison = Integer.compare(this.getTotalPrice(), other.getTotalPrice());
        if (priceComparison != 0) {
            return priceComparison;
        }
        // If prices are equal, compare by city then by sqm to ensure uniqueness
        int cityComparison = this.city.compareTo(other.city);
        if (cityComparison != 0) {
            return cityComparison;
        }
        return Integer.compare(this.sqm, other.sqm);
    }
}

// ===================== Panel CLASS =====================
class Panel extends RealEstate implements PanelInterface {
    private int floor;
    private boolean isInsulated;

    // Default constructor
    public Panel() {
        super();
    }

    // Parameterized constructor
    public Panel(String city, double price, int sqm, int numberOfRooms, Genre genre,
                 int floor, boolean isInsulated) {
        super(city, price, sqm, numberOfRooms, genre);
        this.floor = floor;
        this.isInsulated = isInsulated;
    }

    // Getters and setters
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public boolean isInsulated() { return isInsulated; }
    public void setInsulated(boolean insulated) { isInsulated = insulated; }

    // ---------------- Overrides ----------------
    @Override
    public int getTotalPrice() {
        double basePrice = super.getTotalPrice();
        double modifier = 1.0;

        if (floor >= 0 && floor <= 2) {
            modifier += 0.05; // +5%
        } else if (floor >= 10) {
            modifier -= 0.05; // -5%
        }

        if (isInsulated) {
            modifier += 0.05; // +5%
        }

        return (int) (basePrice * modifier);
    }

    @Override
    public boolean hasSameAmount(RealEstate other) {
        if (other == null) return false;
        return this.getTotalPrice() == other.getTotalPrice();
    }

    @Override
    public int roomPrice() {
        if (numberOfRooms == 0) return 0;
        // Calculate room price without settlement/floor/insulation modifiers
        double basePrice = price * sqm;
        return (int) (basePrice / numberOfRooms);
    }

    @Override
    public String toString() {
        return String.format(
                "Panel [City: %s, Genre: %s, Price per sqm: %.2f, Area: %d sqm, Rooms: %d, " +
                        "Floor: %d, Insulated: %s, Total Price: %d, Average sqm per room: %.2f, Room Price: %d]",
                city, genre, price, sqm, numberOfRooms, floor, isInsulated ? "yes" : "no",
                getTotalPrice(), averageSqmPerRoom(), roomPrice()
        );
    }
}

// ===================== RealEstateAgent CLASS =====================
class RealEstateAgent {
    private static TreeSet<RealEstate> realEstateCollection = new TreeSet<>();

    // Load properties from file
    public static void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            realEstateCollection.clear(); // Clear existing data

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    RealEstate property = parsePropertyLine(line);
                    if (property != null) {
                        realEstateCollection.add(property);
                    }
                }
            }
            System.out.println("Successfully loaded " + realEstateCollection.size() + " properties from file: " + filename);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename + ". Loading sample data instead.");
            loadSampleData();
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            loadSampleData();
        }
    }

    // Parse a single property line from file
    private static RealEstate parsePropertyLine(String line) {
        try {
            String[] parts = line.split("#");

            if (parts.length < 6) {
                System.err.println("Invalid line format (not enough parts): " + line);
                return null;
            }

            String className = parts[0].trim().toUpperCase();
            String city = parts[1].trim();
            double price = Double.parseDouble(parts[2].trim());
            int sqm = Integer.parseInt(parts[3].trim());
            int numberOfRooms = Integer.parseInt(parts[4].trim());

            Genre genre;
            try {
                String genreStr = parts[5].trim().toUpperCase();
                // Handle potential variations in genre names
                if (genreStr.equals("FLAT")) {
                    genre = Genre.CONDOMINIUM;
                } else {
                    genre = Genre.valueOf(genreStr);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid genre in line: " + line + ". Using CONDOMINIUM as default.");
                genre = Genre.CONDOMINIUM;
            }

            if ("PANEL".equals(className)) {
                if (parts.length >= 8) {
                    int floor = Integer.parseInt(parts[6].trim());
                    boolean isInsulated = "yes".equalsIgnoreCase(parts[7].trim());
                    return new Panel(city, price, sqm, numberOfRooms, genre, floor, isInsulated);
                } else {
                    System.err.println("Invalid Panel format (missing floor/insulation): " + line);
                    return null;
                }
            } else {
                return new RealEstate(city, price, sqm, numberOfRooms, genre);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in line: " + line);
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    // Load sample data if file reading fails
    private static void loadSampleData() {
        System.out.println("Loading sample data...");
        realEstateCollection.clear();

        realEstateCollection.add(new RealEstate("Budapest", 250000, 100, 4, Genre.CONDOMINIUM));
        realEstateCollection.add(new RealEstate("Debrecen", 220000, 120, 5, Genre.FAMILYHOUSE));
        realEstateCollection.add(new RealEstate("Nyíregyháza", 110000, 60, 2, Genre.FARM));
        realEstateCollection.add(new RealEstate("Nyíregyháza", 250000, 160, 6, Genre.FAMILYHOUSE));
        realEstateCollection.add(new RealEstate("Kisvárda", 150000, 50, 2, Genre.CONDOMINIUM));
        realEstateCollection.add(new Panel("Budapest", 180000, 70, 3, Genre.CONDOMINIUM, 4, false));
        realEstateCollection.add(new Panel("Debrecen", 120000, 35, 2, Genre.CONDOMINIUM, 0, true));
        realEstateCollection.add(new Panel("Tiszaújváros", 120000, 75, 3, Genre.CONDOMINIUM, 10, false));
        realEstateCollection.add(new Panel("Nyíregyháza", 170000, 80, 3, Genre.CONDOMINIUM, 7, false));

        System.out.println("Loaded " + realEstateCollection.size() + " sample properties.");
    }

    // Generate analysis report and save to file
    public static void generateAnalysisReport(String outputFilename) {
        if (realEstateCollection.isEmpty()) {
            System.out.println("No properties in collection. Cannot generate report.");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("REAL ESTATE ANALYSIS REPORT\n");
        report.append("===========================\n\n");

        try {
            // 1. Average square meter price of real estate
            double avgSqmPrice = realEstateCollection.stream()
                    .mapToDouble(RealEstate::getPrice)
                    .average()
                    .orElse(0.0);
            report.append(String.format("1. Average square meter price of real estate: %.2f\n", avgSqmPrice));

            // 2. Price of the cheapest property
            int cheapestPrice = realEstateCollection.stream()
                    .mapToInt(RealEstate::getTotalPrice)
                    .min()
                    .orElse(0);
            report.append(String.format("2. Price of the cheapest property: %d\n", cheapestPrice));

            // 3. Average square meter value per room of the most expensive apartment in Budapest
            Optional<RealEstate> mostExpensiveBudapest = realEstateCollection.stream()
                    .filter(property -> "budapest".equalsIgnoreCase(property.getCity()))
                    .max(Comparator.comparingInt(RealEstate::getTotalPrice));

            if (mostExpensiveBudapest.isPresent()) {
                double avgSqmPerRoom = mostExpensiveBudapest.get().averageSqmPerRoom();
                report.append(String.format("3. Average square meter value per room of most expensive Budapest property: %.2f\n", avgSqmPerRoom));
            } else {
                report.append("3. No properties found in Budapest\n");
            }

            // 4. Total price of all properties
            long totalPrice = realEstateCollection.stream()
                    .mapToLong(RealEstate::getTotalPrice)
                    .sum();
            report.append(String.format("4. Total price of all properties: %d\n", totalPrice));

            // Calculate average property price for filtering
            double avgPropertyPrice = realEstateCollection.stream()
                    .mapToInt(RealEstate::getTotalPrice)
                    .average()
                    .orElse(0.0);

            // 5. List of condominium properties whose total price does not exceed average price
            report.append(String.format("\n5. Condominium properties with total price <= average price (%.2f):\n", avgPropertyPrice));
            List<RealEstate> affordableCondos = realEstateCollection.stream()
                    .filter(property -> property.getGenre() == Genre.CONDOMINIUM)
                    .filter(property -> property.getTotalPrice() <= avgPropertyPrice)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            if (affordableCondos.isEmpty()) {
                report.append("   No condominium properties found within average price range.\n");
            } else {
                for (RealEstate property : affordableCondos) {
                    report.append("   - ").append(property.toString()).append("\n");
                }
            }

            // Display report on console
            System.out.println("\n" + "=".repeat(80));
            System.out.println(report.toString());
            System.out.println("=".repeat(80));

            // Save report to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))) {
                writer.print(report.toString());
                writer.println("\nGenerated on: " + new Date());
                System.out.println("Report successfully saved to: " + outputFilename);
            } catch (IOException e) {
                System.err.println("Error writing to output file: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error generating analysis report: " + e.getMessage());
        }
    }

    // Display all properties in the collection
    public static void displayAllProperties() {
        if (realEstateCollection.isEmpty()) {
            System.out.println("No properties in the collection.");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL PROPERTIES IN COLLECTION (sorted by total price)");
        System.out.println("=".repeat(80));

        int count = 1;
        for (RealEstate property : realEstateCollection) {
            System.out.println(count + ". " + property);
            count++;
        }
    }

    // Getter for the collection
    public static TreeSet<RealEstate> getRealEstateCollection() {
        return realEstateCollection;
    }

    // Get collection size
    public static int getCollectionSize() {
        return realEstateCollection.size();
    }

    // Add property manually
    public static void addProperty(RealEstate property) {
        if (property != null) {
            realEstateCollection.add(property);
        }
    }
}

// ===================== MAIN DEMO CLASS =====================
public class RealEstateDemo {
    public static void main(String[] args) {
        System.out.println("REAL ESTATE MANAGEMENT SYSTEM");
        System.out.println("==============================\n");

        // Load data manually (no file needed)
        loadDataManually();

        // Display all loaded properties
        RealEstateAgent.displayAllProperties();

        // Generate and display comprehensive analysis report
        RealEstateAgent.generateAnalysisReport("outputRealEstate.txt");

        // Original demo code
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ORIGINAL DEMO FUNCTIONALITY");
        System.out.println("=".repeat(80));

        RealEstate house1 = new RealEstate("Budapest", 500000, 120, 4, Genre.FAMILYHOUSE);
        RealEstate condo1 = new RealEstate("Debrecen", 300000, 80, 3, Genre.CONDOMINIUM);

        Panel panel1 = new Panel("Budapest", 400000, 60, 2, Genre.CONDOMINIUM, 1, true);
        Panel panel2 = new Panel("Nyíregyháza", 250000, 70, 3, Genre.CONDOMINIUM, 10, false);

        System.out.println("=== RealEstate Properties ===");
        System.out.println(house1);
        System.out.println(condo1);

        System.out.println("\n=== Panel Properties ===");
        System.out.println(panel1);
        System.out.println(panel2);

        System.out.println("\n=== After 10% discount on house1 ===");
        house1.makeDiscount(10);
        System.out.println(house1);

        System.out.println("\n=== Price Comparisons ===");
        System.out.println("Panel1 has same total price as house1: " + panel1.hasSameAmount(house1));
        System.out.println("Panel1 has same total price as condo1: " + panel1.hasSameAmount(condo1));

        System.out.println("\n=== Room Prices ===");
        System.out.println("Panel1 room price: " + panel1.roomPrice());
        System.out.println("Panel2 room price: " + panel2.roomPrice());
    }

    // Method to load data manually without file
    private static void loadDataManually() {
        System.out.println("Loading data manually...\n");

        // REALESTATE properties
        addPropertyToCollection("REALESTATE#Budapest#250000#100#4#CONDOMINIUM");
        addPropertyToCollection("REALESTATE#Debrecen#220000#120#5#FAMILYHOUSE");
        addPropertyToCollection("REALESTATE#Nyíregyháza#110000#60#2#FARM");
        addPropertyToCollection("REALESTATE#Nyíregyháza#250000#160#6#FAMILYHOUSE");
        addPropertyToCollection("REALESTATE#Kisvárda#150000#50#2#CONDOMINIUM");

        // PANEL properties
        addPropertyToCollection("PANEL#Budapest#180000#70#3#CONDOMINIUM#4#no");
        addPropertyToCollection("PANEL#Debrecen#120000#35#2#CONDOMINIUM#0#yes");
        addPropertyToCollection("PANEL#Tiszaújváros#120000#75#3#CONDOMINIUM#10#no");
        addPropertyToCollection("PANEL#Nyíregyháza#170000#80#3#CONDOMINIUM#7#no");

        System.out.println("Successfully loaded " + RealEstateAgent.getCollectionSize() + " properties manually.\n");
    }

    // Helper method to parse and add a property
    private static void addPropertyToCollection(String propertyString) {
        try {
            String[] parts = propertyString.split("#");
            String className = parts[0].trim().toUpperCase();
            String city = parts[1].trim();
            double price = Double.parseDouble(parts[2].trim());
            int sqm = Integer.parseInt(parts[3].trim());
            int numberOfRooms = Integer.parseInt(parts[4].trim());

            Genre genre;
            String genreStr = parts[5].trim().toUpperCase();
            if (genreStr.equals("FLAT")) {
                genre = Genre.CONDOMINIUM;
            } else {
                genre = Genre.valueOf(genreStr);
            }

            RealEstate property;
            if ("PANEL".equals(className) && parts.length >= 8) {
                int floor = Integer.parseInt(parts[6].trim());
                boolean isInsulated = "yes".equalsIgnoreCase(parts[7].trim());
                property = new Panel(city, price, sqm, numberOfRooms, genre, floor, isInsulated);
            } else {
                property = new RealEstate(city, price, sqm, numberOfRooms, genre);
            }

            RealEstateAgent.addProperty(property);
        } catch (Exception e) {
            System.err.println("Error adding property: " + propertyString);
        }
    }
}