package domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

//test class

public class Main {
    public static void main(String[] args) {
        System.out.println("main");

        User john = new User("John", "passwordJohn");
        User alice = new User("Alice", "passwordAlice");
        House johnHouse = new House("JohnHouse", john);
        Section kitchen = new Section("Kitchen");
        Section livingRoom = new Section("Living Room");
        livingRoom.registerDevice("tv");
        livingRoom.registerDevice("computer");
        kitchen.registerDevice("fridge");
        johnHouse.getSections().add(kitchen);
        johnHouse.getSections().add(livingRoom);

        johnHouse.allowUser(john, alice, "Living Room");

        johnHouse.allowUser(john, alice, "Kitchen");

        boolean b = johnHouse.isUserAllowed(alice, "tv");
        System.out.println("Alice to change tv in Living Room: " + b);
        
        try{
        johnHouse.turnOnDevice(alice, "tv", 10);
        johnHouse.turnOnDevice(alice, "computer", 20);
        johnHouse.turnOnDevice(alice, "fridge", 30);
        System.out.println("Up time for tv: " + johnHouse.getDeviceUpTime("tv"));
        System.out.println("Up time for computer: " + johnHouse.getDeviceUpTime("computer"));
        System.out.println("Up time for fridge: " + johnHouse.getDeviceUpTime("fridge"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        johnHouse.disallowUser(john, alice, "Living Room");
        try {
            johnHouse.turnOnDevice(alice, "tv", 10);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        b = johnHouse.isUserAllowed(alice, "tv");
        System.out.println("Alice allowed to change tv after disallowing: " + b);

        runDomainHandlerAndPersistenceTests();

    }

    private static void runDomainHandlerAndPersistenceTests() {
        System.out.println("\n--- DomainHandler / toText / save-load tests ---");

        DataManager dm = DataManager.getInstance();
        dm.addUser(new User("owner1", "pw1"));
        dm.addUser(new User("guest1", "pw2"));

        DomainHandler handler = createDomainHandlerForTests();
        AuthEnum authenticated = handler.authenticateUser("owner1", "pw1");
        System.out.println("owner1 autenticado no DomainHandler: " + authenticated);

        if (authenticated == AuthEnum.OK_USER || authenticated == AuthEnum.OK_NEW_USER) {
            handler.createHouse("CasaA");
            handler.createHouse("CasaB");

            handler.registerDevice("CasaA", "Electros");
            handler.registerDevice("CasaA", "Garden");
            handler.registerDevice("CasaB", "Multimedia");

            try {
                handler.addDeviceTime("CasaA", "E1", 15);
                handler.addDeviceTime("CasaA", "G1", 7);
                handler.addDeviceTime("CasaB", "M1", 9);
            } catch (IllegalArgumentException e) {
                System.out.println("addDeviceTime lancou excecao esperada: " + e.getMessage());
            }

            System.out.println("owner1 permitido em todas as sections de CasaA: "
                    + handler.isUserAllowedInAllSections("CasaA"));

            handler.logout();
        }

        User tester = new User("Tester", "pw");
        House houseToText = new House("CasaTeste", tester);
        Section office = new Section("Office");
        office.addAllowedUser(tester);
        office.registerDevice("ignoredName");
        houseToText.addSection(office);

        System.out.println("\nHouse.toText():\n" + houseToText.toText());
        System.out.println("Section.toText():\n" + office.toText());

        dm.save();
        System.out.println("\nsave() executado no DataManager.");
        runLoadTestWithReflection(dm);
    }

    private static DomainHandler createDomainHandlerForTests() {
        try {
            Constructor<DomainHandler> constructor = DomainHandler.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel criar DomainHandler para teste", e);
        }
    }

    private static void runLoadTestWithReflection(DataManager dm) {
        try {
            Field usersField = DataManager.class.getDeclaredField("users");
            Field housesField = DataManager.class.getDeclaredField("houses");
            usersField.setAccessible(true);
            housesField.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) usersField.get(dm);
            @SuppressWarnings("unchecked")
            List<House> houses = (List<House>) housesField.get(dm);

            int usersBeforeReload = users.size();
            int housesBeforeReload = houses.size();

            users.clear();
            houses.clear();

            Method loadMethod = DataManager.class.getDeclaredMethod("load", String.class);
            loadMethod.setAccessible(true);
            loadMethod.invoke(dm, "data.txt");

            int usersAfterReload = users.size();
            int housesAfterReload = houses.size();

            System.out.println("load() testado via reflection.");
            System.out.println("Users antes/depois reload: " + usersBeforeReload + " / " + usersAfterReload);
            System.out.println("Houses antes/depois reload: " + housesBeforeReload + " / " + housesAfterReload);
            System.out.println("owner1 autenticado depois do load: "
                    + (dm.authenticateUser("owner1", "pw1") != null));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao testar load()", e);
        }

    }
}
    