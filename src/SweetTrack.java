import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class SweetTrack {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        ArrayList<User> users = loadUsers();

        boolean adminExists = false;
        for (User u : users) {
            if (u.username.equals("admin")) {
                adminExists = true;
                break;
            }
        }

        if (!adminExists) {
            users.add(new User("admin", "1234", "admin"));
            saveUsers(users);
        }

        System.out.println("===== SWEET TRACK LOGIN =====");
        System.out.println("1. Login");
        System.out.println("2. Signup");

        System.out.print("Enter choice: ");
        int loginChoice = safeInt();

        if (loginChoice == 2) {

            System.out.print("Create username: ");
            String newUsername = sc.nextLine();

            System.out.print("Create password: ");
            String newPassword = sc.nextLine();

            users.add(new User(newUsername, newPassword, "customer"));
            saveUsers(users);

            System.out.println("Signup successful!");
        }

        System.out.println("\n===== LOGIN =====");

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        boolean loggedIn = false;
        String role = "";

        for (User u : users) {
            if (u.username.equalsIgnoreCase(username) && u.password.equals(password)) {
                loggedIn = true;
                role = u.role;
                break;
            }
        }

        if (!loggedIn) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Logged in as: " + role.toUpperCase());

        ArrayList<Chocolate> chocolates = loadChocolates();

        int choice;

        do {

            System.out.println("\n==== SWEET TRACK ====");

            if (role.equals("admin")) {
                System.out.println("1. Add Chocolate");
                System.out.println("2. View Chocolates");
                System.out.println("3. Place Order");
                System.out.println("4. Search Chocolate");
                System.out.println("5. Delete Chocolate");
                System.out.println("6. Update Chocolate");
                System.out.println("7. View All Orders");
                System.out.println("8. Exit");
            } else {
                System.out.println("1. View Chocolates");
                System.out.println("2. Place Order");
                System.out.println("3. Search Chocolate");
                System.out.println("4. View My Orders");
                System.out.println("5. Exit");
            }

            System.out.print("Enter choice: ");
            choice = safeInt();

            if (role.equals("admin")) {

                switch (choice) {

                    case 1 -> addChocolate(chocolates);
                    case 2 -> viewChocolates(chocolates);
                    case 3 -> placeOrder(username, chocolates);
                    case 4 -> searchChocolate(chocolates);
                    case 5 -> deleteChocolate(chocolates);
                    case 6 -> updateChocolate(chocolates);
                    case 7 -> viewAllOrders();
                    case 8 -> System.out.println("Thank you admin!");
                    default -> System.out.println("Invalid choice.");
                }

            } else {

                switch (choice) {

                    case 1 -> viewChocolates(chocolates);
                    case 2 -> placeOrder(username, chocolates);
                    case 3 -> searchChocolate(chocolates);
                    case 4 -> viewMyOrders(username);
                    case 5 -> System.out.println("Thank you!");
                    default -> System.out.println("Invalid choice.");
                }
            }

        } while ((role.equals("admin") && choice != 8) || (!role.equals("admin") && choice != 5));

        sc.close();
    }

    // ================= ORDER =================
    public static void placeOrder(String username, ArrayList<Chocolate> chocolates) {

        viewChocolates(chocolates);

        System.out.print("Select chocolate number: ");
        int i = safeInt();

        if (i < 1 || i > chocolates.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Enter quantity: ");
        int qty = safeInt();

        if (qty <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }

        Chocolate c = chocolates.get(i - 1);

        if (qty > c.stock) {
            System.out.println("Insufficient stock.");
            return;
        }

        double total = qty * c.price;
        c.stock -= qty;

        saveChocolates(chocolates);

        saveOrder(username, c.name, qty, total);

        System.out.println("Order placed successfully!");

        generateBill(c, qty, total);

        System.out.println("\nThank you for your order 😊");
    }

    // ================= USER ORDERS =================
    public static void viewMyOrders(String username) {

        System.out.println("\n===== YOUR ORDERS =====");

        try {
            File f = new File("orders.txt");

            if (!f.exists()) {
                System.out.println("No orders found.");
                return;
            }

            Scanner scf = new Scanner(f);
            boolean found = false;

            while (scf.hasNextLine()) {

                String[] d = scf.nextLine().split(",");

                if (d[0].equalsIgnoreCase(username)) {

                    System.out.println("Product: " + d[1] +
                            " | Qty: " + d[2] +
                            " | Total: " + d[3]);

                    found = true;
                }
            }

            scf.close();

            if (!found) {
                System.out.println("No orders yet.");
            }

        } catch (Exception e) {
            System.out.println("Error reading orders.");
        }
    }

    // ================= ADMIN ORDERS =================
    public static void viewAllOrders() {

        System.out.println("\n===== ALL ORDERS (ADMIN) =====");

        try {
            File f = new File("orders.txt");

            if (!f.exists()) {
                System.out.println("No orders found.");
                return;
            }

            Scanner scf = new Scanner(f);

            while (scf.hasNextLine()) {

                String[] d = scf.nextLine().split(",");

                System.out.println(
                        "User: " + d[0] +
                        " | Product: " + d[1] +
                        " | Qty: " + d[2] +
                        " | Total: " + d[3]
                );
            }

            scf.close();

        } catch (Exception e) {
            System.out.println("Error reading orders.");
        }
    }

    // ================= BILL =================
    public static void generateBill(Chocolate c, int qty, double total) {

        System.out.println("\n===== BILL =====");
        System.out.println("Chocolate: " + c.name);
        System.out.println("Price: " + c.price);
        System.out.println("Quantity: " + qty);
        System.out.println("Total: " + total);
        System.out.println("================");
    }

    // ================= SAVE ORDER =================
    public static void saveOrder(String username, String product, int qty, double total) {

        try {
            FileWriter w = new FileWriter("orders.txt", true);
            w.write(username + "," + product + "," + qty + "," + total + "\n");
            w.close();
        } catch (Exception e) {
            System.out.println("Order save error");
        }
    }

    // ================= CHOCOLATE OPS =================
    public static void addChocolate(ArrayList<Chocolate> chocolates) {

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Price: ");
        double price = Double.parseDouble(sc.nextLine());

        System.out.print("Stock: ");
        int stock = Integer.parseInt(sc.nextLine());

        chocolates.add(new Chocolate(name, price, stock));
        saveChocolates(chocolates);

        System.out.println("Added successfully!");
    }

    public static void updateChocolate(ArrayList<Chocolate> chocolates) {

        viewChocolates(chocolates);

        System.out.print("Enter number: ");
        int i = safeInt();

        Chocolate c = chocolates.get(i - 1);

        System.out.print("New price: ");
        c.price = Double.parseDouble(sc.nextLine());

        System.out.print("New stock: ");
        c.stock = Integer.parseInt(sc.nextLine());

        saveChocolates(chocolates);
    }

    public static void deleteChocolate(ArrayList<Chocolate> chocolates) {

        viewChocolates(chocolates);

        System.out.print("Enter number: ");
        int i = safeInt();

        chocolates.remove(i - 1);

        saveChocolates(chocolates);
    }

    public static void viewChocolates(ArrayList<Chocolate> chocolates) {

        for (int i = 0; i < chocolates.size(); i++) {
            Chocolate c = chocolates.get(i);
            System.out.println((i + 1) + ". " + c.name + " | " + c.price + " | " + c.stock);
        }
    }

    public static void searchChocolate(ArrayList<Chocolate> chocolates) {

        System.out.print("Enter name: ");
        String s = sc.nextLine();

        for (Chocolate c : chocolates) {
            if (c.name.equalsIgnoreCase(s)) {
                System.out.println(c.name + " | " + c.price + " | " + c.stock);
                return;
            }
        }

        System.out.println("Not found.");
    }

    // ================= FILE OPS =================
    public static ArrayList<User> loadUsers() { return new ArrayList<>(); }
    public static void saveUsers(ArrayList<User> users) {}

    public static ArrayList<Chocolate> loadChocolates() { return new ArrayList<>(); }
    public static void saveChocolates(ArrayList<Chocolate> chocolates) {}

    // ================= SAFE INPUT =================
    public static int safeInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Enter valid number: ");
            }
        }
    }
}
