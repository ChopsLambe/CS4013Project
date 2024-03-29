import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;

public class TaxCalculator {
    private double fixedCost, flatPprCharge, annualPenalty;
    private String[] Locations;// array for locations and respective charges
    private double[] pValues, locationsCharge, propertyRates;// array for property values and respective charges
    private ArrayList<String> propertiesFromCSV;

    // default constructor for TaxCalculator class
    public TaxCalculator() {
        this.fixedCost = 100;
        this.flatPprCharge = 100;
        this.annualPenalty = 0.07;
        this.Locations = new String[] { "Countryside", "Village", "Small town", "Large town", "City" };
        this.locationsCharge = new double[] { 25, 50, 60, 80, 100 };
        this.pValues = new double[] { 0, 150000, 400000, 650000 };
        this.propertyRates = new double[] { 0, 0.01, 0.02, 0.04 };
    }

    // variable constructor for TaxCalculator Class
    public TaxCalculator(double fixedCost, double flatPprCharge, double annualPenalty, String[] Locations,
            double[] pValues, double[] locationsCharge, double[] propertyRates) {
        this.fixedCost = fixedCost;
        this.flatPprCharge = flatPprCharge;
        this.annualPenalty = annualPenalty;
        this.Locations = Locations;
        this.pValues = pValues;
        this.locationsCharge = locationsCharge;
        this.propertyRates = propertyRates;
    }

    /**
     * @param p
     * @return double
     * @throws IOException
     */
    public double overallTax(Property p) throws IOException {
        return propertyTax(p) + overdueTax(p);
    }

    /**
     * @param p
     * @return double
     * @throws IOException
     */
    // checks the outstanding tax of a single property for one year-no penalty
    public double propertyTax(Property p) throws IOException {
        return fixedCost + (p.getEMV() * getMVTRate(p)) + getLocationCharge(p)
                + ((p.isPpr() == true) ? flatPprCharge : 0);
    }

    /**
     * @param o
     * @return double
     * @throws IOException
     */
    // calculates the outstanding tax of properties owned by an Owner
    public double propertyTax(Owner o) throws IOException {
        double taxO = 0;
        for (int i = 0; i < o.getProperties().size(); i++) {
            taxO = taxO + propertyTax(o.getProperties().get(i));
        }
        return taxO;
    }

    /**
     * @param p
     * @return double
     * @throws IOException
     */
    // gets the overdue tax on a specific property provided by the user
    public double overdueTax(Property p) throws IOException {
        double overdueTax = propertyTax(p);
        for (int i = 0; i < yearsOverdue(p); i++) {
            overdueTax = compoundTax(p, overdueTax);
        }
        return overdueTax;
    }

    /**
     * @param year
     * @return double
     * @throws IOException
     */
    // gets all the overdue tax for all properties -overdue tax
    // takes into account the compounding of tax as owners do not pay
    // their property tax
    public double overdueTax(LocalDate year) throws IOException {
        int j = 0, k = 0;
        double overdueTax = 0, Taxes = 0;
        Double[] overdueTaxes = new Double[256];
        String[] property = new String[7];
        propertiesFromCSV = csvReader("src/properties.csv");
        for (int i = 0; i < propertiesFromCSV.size(); i++) {
            int h = 0;
            while (j < propertiesFromCSV.size()) {
                property = propertiesFromCSV.get(i).split(",");
            }
            j++;
            LocalDate yearPropertyCSV = LocalDate.parse(property[6]);
            if (yearPropertyCSV == year) {
                Property p = new Property(property[0], property[1], property[2], property[3],
                        Double.parseDouble(property[4]), Boolean.parseBoolean(property[5]), yearPropertyCSV);
                double value = propertyTax(p);
                while (h < yearsOverdue(p)) {
                    overdueTax = compoundTax(p, value);
                    overdueTaxes[k] = overdueTax;
                    k++;
                    h++;
                }
            }
            if (i == propertiesFromCSV.size()) {// so the program can check each property
                break;
            }
        }
        for (int i = 0; i < overdueTaxes.length; i++) {
            if (overdueTaxes[i] != null) {
                Taxes = Taxes + overdueTaxes[i];
            } else {
                break;
            }
        }
        return Taxes;
    }

    /**
     * @param year
     * @param key
     * @return double
     * @throws IOException
     */
    // gets the overdue tax for properties based on the eircode routing key
    public double overdueTax(LocalDate year, String key) throws IOException {
        int j = 0, k = 0;
        double overdueTax = 0, Taxes = 0;
        Double[] overdueTaxes = new Double[256];
        String[] property = new String[7];
        propertiesFromCSV = csvReader("src/properties.csv");
        for (int i = 0; i < propertiesFromCSV.size(); i++) {
            int h = 0;
            while (j < propertiesFromCSV.size()) {
                property = propertiesFromCSV.get(i).split(",");
                break;
            }
            j++;
            LocalDate yearPropertiesCSV = LocalDate.parse(property[6]);
            String propertiesEircode = property[2];
            String[] propertiesKeyCSV = propertiesEircode.split(" ");
            String propertiesEirKey = propertiesKeyCSV[0];
            if (yearPropertiesCSV.isBefore(year) && propertiesEirKey.equalsIgnoreCase(key)) {
                Property p = new Property(property[0], property[1], property[2], property[3],
                        Double.parseDouble(property[4]), Boolean.parseBoolean(property[5]), yearPropertiesCSV);
                double value = propertyTax(p);
                while (h < yearsOverdue(p)) {
                    overdueTax = compoundTax(p, value);
                    overdueTaxes[k] = overdueTax;
                    k++;
                    h++;
                }
            }
            if (i == propertiesFromCSV.size()) {
                break;
            }
        }
        for (int i = 0; i < overdueTaxes.length; i++) {
            if (overdueTaxes[i] != null) {
                Taxes = Taxes + overdueTaxes[i];
            } else {
                break;
            }

        }
        return Taxes;
    }

    /**
     * @param o
     * @param year
     * @return String
     * @throws IOException
     */
    // returns a balancing statement for a particular Owner
    public String balancingStatement(Owner o, int year) throws IOException {
        double total = 0;
        for (int i = 0; i < o.getProperties().size(); i++) {
            if (o.getProperties().get(i).getDate().getYear() == year) {
                total = total + propertyTax(o.getProperties().get(i));
            }
        }

        return "This owner accumulated " + total + " in taxes on their properties this year";
    }

    /**
     * @param p
     * @return String
     * @throws IOException
     */
    // returns a balancing statement for a particular property for
    public String balancingStatement(Property p) throws IOException {
        return "This property has " + propertyTax(p) + " due in taxes this year";
    }

    /**
     * @param p
     * @return int
     * @throws IOException
     */
    // counts the number of years a property is overdue on tax
    private int yearsOverdue(Property p) throws IOException {
        int count = 0, k = 0;
        String[] property = new String[7];
        propertiesFromCSV = csvReader("src/properties.csv");
        for (int i = 0; i < dateCheck(p); i++) {
            while (k < propertiesFromCSV.size()) {
                property = propertiesFromCSV.get(i).split(",");
                break;
            }
            if (getPenalty(p, property)) {
                count++;
            }
            k++;
        }
        return count;
    }

    /**
     * @param p
     * @return double
     */
    // gets the market value tax rate of the property supplied
    private double getMVTRate(Property p) {
        double rate = 0;
        for (int i = 0; i < pValues.length; i++) {
            if (p.getEMV() <= pValues[i]) {
                rate = propertyRates[i];
                break;
            } else {
                rate = propertyRates[i];
            }
        }
        return rate;
    }

    /**
     * @param p
     * @return double
     */
    // gets the location tax charge associated with the given location
    private double getLocationCharge(Property p) {
        double value = 0;
        for (int i = 0; i < Locations.length; i++) {
            if (p.getLocation().equalsIgnoreCase(Locations[i])) {
                value = locationsCharge[i];
                break;
            }
        }
        return value;
    }

    /**
     * @param p
     * @param property
     * @return boolean
     * @throws IOException
     */
    // returns true if a given year
    private boolean getPenalty(Property p, String[] property) throws IOException {
        boolean penalty = false;
        LocalDate propertyYear = LocalDate.now();
        if (p.getDate().isBefore(propertyYear)) {
            penalty = true;
        }
        return penalty;
    }

    /**
     * @param p
     * @return int
     */
    // checks how many times the date has passed jan 1st since the registration of
    // property
    private int dateCheck(Property p) {
        int count = 0;
        count = (int) java.time.temporal.ChronoUnit.YEARS.between(p.getDate(), LocalDate.now());
        return count;
    }

    /**
     * @param p
     * @param yearPrevious
     * @return double
     * @throws IOException
     */
    // compounds tax years together
    private double compoundTax(Property p, double yearPrevious) throws IOException {
        return (yearPrevious * (1 + annualPenalty)) + propertyTax(p);
    }

    /**
     * @param filename
     * @return ArrayList<String>
     * @throws IOException
     */
    // reads in data from csv files
    private ArrayList<String> csvReader(String filename) throws IOException {
        Path pathToFile = Paths.get(filename);
        ArrayList<String> attributes = new ArrayList<String>();
        try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
            String line = br.readLine();
            while (line != null) {
                attributes.add(line);
                line = br.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return attributes;
    }

}
