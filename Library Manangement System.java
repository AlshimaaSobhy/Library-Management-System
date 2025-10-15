import java.io.*;
import java.util.*;

/**
 * Simple console-based Library Management System
 * Single-file implementation for learning and quick use.
 * Features:
 *    Add / Remove / List books
 *    Register members
 *    Issue / Return books
 *    Search books by title or author
 *    Save / Load data to disk using Java Serialization
 */

public class LibraryManagementSystem {
    public static void main(String[] args) {
        Library library = Library.loadFromDisk();
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Welcome to the Library Management System ===");
        boolean exit = false;
        while (!exit) {
            System.out.println();
            System.out.println("1. Books: Add / Remove / List / Search");
            System.out.println("2. Members: Register / List");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Save data to disk");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    booksMenu(library, sc);
                    break;
                case "2":
                    membersMenu(library, sc);
                    break;
                case "3":
                    issueBookFlow(library, sc);
                    break;
                case "4":
                    returnBookFlow(library, sc);
                    break;
                case "5":
                    library.saveToDisk();
                    System.out.println("Data saved.");
                    break;
                case "6":
                    System.out.println("Saving before exit...");
                    library.saveToDisk();
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        sc.close();
        System.out.println("Goodbye!");
    }

    static void booksMenu(Library library, Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("Books Menu:");
            System.out.println("1. Add book");
            System.out.println("2. Remove book (by id)");
            System.out.println("3. List all books");
            System.out.println("4. Search by title");
            System.out.println("5. Search by author");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author: ");
                    String author = sc.nextLine();
                    System.out.print("Total copies: ");
                    int copies = readInt(sc, 1);
                    Book b = library.addBook(title, author, copies);
                    System.out.println("Added: " + b);
                    break;
                case "2":
                    System.out.print("Book ID to remove: ");
                    int id = readInt(sc, 0);
                    boolean removed = library.removeBook(id);
                    System.out.println(removed ? "Book removed " : "Book not found or still issued ");
                    break;
                case "3":
                    library.listBooks();
                    break;
                case "4":
                    System.out.print("Title keyword: ");
                    String kwt = sc.nextLine();
                    library.searchByTitle(kwt);
                    break;
                case "5":
                    System.out.print("Author keyword: ");
                    String kwa = sc.nextLine();
                    library.searchByAuthor(kwa);
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Invalid choice ");
            }
        }
    }

    static void membersMenu(Library library, Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("Members Menu:");
            System.out.println("1. Register new member");
            System.out.println("2. List members");
            System.out.println("3. Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("Member name: ");
                    String name = sc.nextLine();
                    Member m = library.registerMember(name);
                    System.out.println("Registered: " + m);
                    break;
                case "2":
                    library.listMembers();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }

    static void issueBookFlow(Library library, Scanner sc) {
        System.out.print("Member ID: ");
        int mid = readInt(sc, 0);
        System.out.print("Book ID: ");
        int bid = readInt(sc, 0);
        boolean ok = library.issueBook(mid, bid);
        if (ok) System.out.println("Book issued successfully");
        else System.out.println("Failed to issue book. Check IDs or availability");
    }

    static void returnBookFlow(Library library, Scanner sc) {
        System.out.print("Member ID: ");
        int mid = readInt(sc, 0);
        System.out.print("Book ID: ");
        int bid = readInt(sc, 0);
        boolean ok = library.returnBook(mid, bid);
        if (ok) System.out.println("Book returned successfully");
        else System.out.println("Failed to return book. Check IDs ");
    }

    static int readInt(Scanner sc, int min) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer >= " + min + ": ");
            }
        }
    }
}


// Book class
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int NEXT_ID = 1;

    final int id;
    String title;
    String author;
    int totalCopies;
    int availableCopies;

    public Book(String title, String author, int copies) {
        this.id = NEXT_ID++;
        this.title = title;
        this.author = author;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public boolean issue() {
        if (availableCopies <= 0) return false;
        availableCopies--;
        return true;
    }

    public void giveBack() {
        if (availableCopies < totalCopies) availableCopies++;
    }

    @Override
    public String toString() {
        return String.format("[ID:%d] %s by %s (Available: %d / %d)", id, title, author, availableCopies, totalCopies);
    }

    // For serialization safety of NEXT_ID
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // ensure NEXT_ID keeps increasing across loads
        if (id >= NEXT_ID) NEXT_ID = id + 1;
    }
}

// Member class
class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int NEXT_ID = 1;

    final int id;
    String name;
    // set of currently issued book ids
    Set<Integer> issuedBooks = new HashSet<>();

    public Member(String name) {
        this.id = NEXT_ID++;
        this.name = name;
    }

    public boolean hasBook(int bookId) {
        return issuedBooks.contains(bookId);
    }

    public void borrow(int bookId) {
        issuedBooks.add(bookId);
    }

    public void returned(int bookId) {
        issuedBooks.remove(bookId);
    }

    @Override
    public String toString() {
        return String.format("[MID:%d] %s (Borrowed: %d)", id, name, issuedBooks.size());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (id >= NEXT_ID) NEXT_ID = id + 1;
    }
}


// Library class — contains collections and operations
class Library implements Serializable {
    private static final long serialVersionUID = 1L;

    Map<Integer, Book> books = new HashMap<>();
    Map<Integer, Member> members = new HashMap<>();

    // Files to persist data
    private static final String BOOKS_FILE = "books.ser";
    private static final String MEMBERS_FILE = "members.ser";

    // Add a book
    public Book addBook(String title, String author, int copies) {
        Book b = new Book(title, author, copies);
        books.put(b.id, b);
        return b;
    }

    // Remove book (only if no copies are issued)
    public boolean removeBook(int id) {
        Book b = books.get(id);
        if (b == null) return false;
        if (b.availableCopies != b.totalCopies) return false; // someone has it
        books.remove(id);
        return true;
    }

    public void listBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in library.");
            return;
        }
        books.values().stream()
                .sorted(Comparator.comparingInt(x -> x.id))
                .forEach(System.out::println);
    }

    public void searchByTitle(String kw) {
        String k = kw.toLowerCase();
        books.values().stream()
                .filter(b -> b.title.toLowerCase().contains(k))
                .sorted(Comparator.comparingInt(x -> x.id))
                .forEach(System.out::println);
    }

    public void searchByAuthor(String kw) {
        String k = kw.toLowerCase();
        books.values().stream()
                .filter(b -> b.author.toLowerCase().contains(k))
                .sorted(Comparator.comparingInt(x -> x.id))
                .forEach(System.out::println);
    }

    public Member registerMember(String name) {
        Member m = new Member(name);
        members.put(m.id, m);
        return m;
    }

    public void listMembers() {
        if (members.isEmpty()) {
            System.out.println("No members registered ");
            return;
        }
        members.values().stream()
                .sorted(Comparator.comparingInt(x -> x.id))
                .forEach(System.out::println);
    }

    public boolean issueBook(int memberId, int bookId) {
        Member m = members.get(memberId);
        Book b = books.get(bookId);
        if (m == null || b == null) return false;
        if (!b.isAvailable()) return false;
        boolean ok = b.issue();
        if (ok) m.borrow(bookId);
        return ok;
    }

    public boolean returnBook(int memberId, int bookId) {
        Member m = members.get(memberId);
        Book b = books.get(bookId);
        if (m == null || b == null) return false;
        if (!m.hasBook(bookId)) return false;
        m.returned(bookId);
        b.giveBack();
        return true;
    }

    // Save and load helper methods
    public void saveToDisk() {
        try (ObjectOutputStream outB = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE));
             ObjectOutputStream outM = new ObjectOutputStream(new FileOutputStream(MEMBERS_FILE))) {
            outB.writeObject(books);
            outM.writeObject(members);
        } catch (IOException e) {
            System.err.println("Error while saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Library loadFromDisk() {
        Library lib = new Library();
        boolean loaded = false;
        try (ObjectInputStream inB = new ObjectInputStream(new FileInputStream(BOOKS_FILE));
             ObjectInputStream inM = new ObjectInputStream(new FileInputStream(MEMBERS_FILE))) {
            Object ob = inB.readObject();
            Object om = inM.readObject();
            if (ob instanceof Map && om instanceof Map) {
                lib.books = (Map<Integer, Book>) ob;
                lib.members = (Map<Integer, Member>) om;
                loaded = true;
            }
        } catch (FileNotFoundException fnf) {
            // first run: ignore
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load saved data: " + e.getMessage());
        }

        if (!loaded) {
            System.out.println("Starting with an empty library (no saved data) ");
            // optionally add some sample books
            lib.addBook("The Hobbit", "J.R.R. Tolkien", 3);
            lib.addBook("1984", "George Orwell", 2);
            lib.addBook("Clean Code", "Robert C. Martin", 1);
        } else {
            System.out.println("Library data loaded from disk ");
        }
        return lib;
    }
}
