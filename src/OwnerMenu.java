import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class OwnerMenu {
    private Scanner in;
    private Owner owner;
    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter pw;
    private TaxCalculator tC;
    private LocalDate date;

    // initialises scanner for the CLI menu
    public OwnerMenu() {
        in = new Scanner(System.in);
    }

    public void run() throws IOException {
        boolean more = true;
        InterfaceSelect iSelect = new InterfaceSelect();
        System.out.println("Are you a new User? Yes//No");
        String newUser = in.nextLine().toUpperCase();
        if (newUser.equals("YES")) {
            System.out.println("Enter Username:");
            String username = in.nextLine();
            System.out.println("Enter Password");
            String password = in.nextLine();
            fw = new FileWriter("src/systemLogins.csv", true);
            bw = new BufferedWriter(bw);
            pw = new PrintWriter(bw);
            pw.print("\n" + username + "," + password);
        }
        System.out.println("Enter Username:");
        String username = in.nextLine();
        System.out.println("Enter Password");
        String password = in.nextLine();
        owner = new Owner(username);
        tC = new TaxCalculator();
        if (userLogin(username, password, owner)) {
            System.out.println("Login Successful");
            while (more) {
                System.out.println(
                        "P)ay tax // L)ist properties and Tax Due // B)alancing Statements // R)egister properties // Q)uit");
                String command = in.nextLine().toUpperCase();
                // allows user to pay the tax due on their property-year check of some sort
                // needed
                if (command.equals("P")) {
                    System.out.println("What property do you want to pay tax for:\n");
                    Property p = getPropertie(owner.getProperties());
                    System.out.println("To be Paid:" + tC.propertyTax(p));// needs extra work done for getting penalty
                    double toPay = in.nextDouble();
                    in.nextLine();
                    Payment pay = new Payment(toPay, LocalDate.now(), owner, p, true);
                    writeToPayments("src/payments.csv", pay);
                    String paid = pay.payTax(p, toPay, tC.propertyTax(p));
                    System.out.println("Paid:" + paid + " For Property:" + p);
                    owner.removePaidProperty(p);
                    owner.addPaidProperty(p);
                }
                // lists the properties and taxes due on each of properties
                else if (command.equals("L")) {
                    System.out.println("List Paid Properties: Y)es//N)o");
                    String com = in.nextLine().toUpperCase();
                    if (com.equals("Y") || com.equals("YES")) {
                        getProperty(owner.getPaidProperties());
                    } else {
                        listProperties(owner.getProperties());
                    }
                }
                // gets balancing statements based on the Owner or Property as decided by the
                // user
                else if (command.equals("B")) {
                    System.out.println("P)roperty Eircode// A)ll properties");
                    String com = in.nextLine().toUpperCase();
                    // property balancing statement
                    if (com.equals("P")) {
                        System.out.println("Enter Eircode:");// Eircode not eircode routing key
                        String eircode = in.nextLine().toUpperCase();
                        for (int i = 0; i < owner.getProperties().size(); i++) {
                            if (eircode.equals(owner.getProperties().get(i).getEircode())) {
                                System.out.println(tC.balancingStatement(owner.getProperties().get(i)));
                                break;
                            }
                        }
                    }
                    // Owner balancing statement
                    else if (com.equals("A")) {
                        System.out.println(tC.balancingStatement(owner));
                    }
                }
                // allows a user to register a property
                else if (command.equals("R")) {
                    date = LocalDate.now();
                    System.out.println("Address Of Property:");
                    String ad = in.nextLine().toUpperCase();
                    System.out.println("Eircode Of Property:");
                    String eC = in.nextLine().toUpperCase();
                    System.out.println("Location Of Property:");
                    String lC = in.nextLine().toUpperCase();
                    System.out.println("Estimated market value Of Property:");
                    double emv = in.nextDouble();
                    System.out.println("Principal Primary Residence of Owner: Yes//No");
                    String Ppr = in.nextLine().toUpperCase();
                    in.nextLine();
                    boolean Pp = false;
                    if (Ppr.equals("YES")) {
                        Pp = true;
                    }
                    Property p = new Property(username, ad, eC, lC, emv, Pp, date);
                    owner.addProperty(p);
                    writeToProperties("src/properties.csv", p, username);
                }
                // quits the program
                else if (command.equals("Q")) {
                    more = false;
                }
            }
        } else {
            iSelect.run();
        }
        iSelect.run();// allows use of software by other users and through gui
    }

    // gets and lists properties for the pay tax function
    private Property getPropertie(ArrayList<Property> PChoices) {
        if (PChoices.size() == 0) {
            return null;
        }
        while (true) {
            char c = 'A';
            for (Property PChoice : PChoices) {
                System.out.println(c + ") " + PChoice);
                c++;
            }
            String input = in.nextLine();
            int n = input.toUpperCase().charAt(0) - 'A';
            if (0 <= n && n < PChoices.size()) {
                return PChoices.get(n);
            }
        }
    }

    // gets properties and the tax due for payment on properties for list function
    private Property getProperty(ArrayList<Property> PChoices) throws IOException {
        if (PChoices.size() == 0) {
            return null;
        }
        while (true) {
            char c = 'A';
            for (Property PChoice : PChoices) {
                System.out.println(c + ") " + PChoice);
                c++;
            }
            String input = in.nextLine();
            int n = input.toUpperCase().charAt(0) - 'A';
            if (0 <= n && n < PChoices.size()) {
                return PChoices.get(n);
            }
        }
    }

    private void listProperties(ArrayList<Property> PChoices) throws IOException {
        if (PChoices.size() == 0) {
            return;
        }
        char c = 'A';
        for (Property PChoice : PChoices) {
            System.out.println(
                    c + ") " + PChoice + " Taxes Due:" + tC.propertyTax(PChoice) + " " + tC.overdueTax(PChoice));
            c++;
        }
    }

    // writes properties resgistered to properties.csv- this is a nice method and I
    // like it, I don't know if we need to use it though
    private void writeToProperties(String filename, Property p, String name) throws IOException {
        try {
            String path = filename;
            fw = new FileWriter(path, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            pw.print("\n" + name + "," + p.getAddress() + "," + p.getEircode() + "," + p.getLocation() + "," + p.isPpr()
                    + "," + p.getDate());
            pw.flush();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // writes entries to the payments file after a payment has been logged
    private void writeToPayments(String filename, Payment pay) {
        try {
            String path = filename;
            fw = new FileWriter(path, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            pw.print("\n" + pay.getOwner().getName() + "," + pay.getProperty().getAddress() + ","
                    + pay.getProperty().getLocation() + "," + pay.getProperty().getEircode() + "," + pay.getDate() + ","
                    + pay.getAmount());
            pw.flush();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean userLogin(String username, String password, Owner owner) throws IOException {
        ArrayList<String> systemLogins = new ArrayList<String>();
        String[] login = new String[2];
        systemLogins = csvReader("src/systemLogins.csv");
        int i = 0;
        for (int j = 0; j < systemLogins.size(); j++) {
            while (i < systemLogins.size()) {
                login = systemLogins.get(i).split(",");
                break;
            }
            i++;
            if (login[0].equals(username) && login[1].equals(password)) {
                ArrayList<Property> properties = new ArrayList<Property>();
                for (int k = 0; k < properties.size(); i++) {

                }
                return true;
            }
        }
        return false;
    }

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
