package domain;

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
    }
}
    