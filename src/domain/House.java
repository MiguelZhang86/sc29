/**
 * @author Jaime Sousa
 */
package domain;

import java.util.LinkedList;
import java.util.List;

public class House {
	private String name;
	private List<Section> sections;
    private User owner;

	/**
	 * Creates a house with a name, sections (rooms) and an owner.
	 *
	 * @param name the house name
	 * @param owner the owner of the house
	 */
	
	House(String name, User owner) {
		this.name = name;
		this.sections = new LinkedList<Section>();
		this.owner = owner;


		this.sections.add(new Section("Electros"));
		this.sections.add(new Section("Garden"));
		this.sections.add(new Section("Luzes"));
		this.sections.add(new Section("Multimedia"));
		this.sections.add(new Section("Portas"));
		this.sections.add(new Section("Stores"));

		for (Section s : sections) {
			s.addAllowedUser(owner);
		}
	}

	/**
	 * Gets the house name.
	 *
	 * @return the house name
	 */
	String getName() {
		return name;
	}

	/**
	 * Checks whether a user is the owner of this house.
	 *
	 * @param user the user to check
	 * @return true if the user is the owner, false otherwise
	 */
	boolean isOwner(User user) {
		return this.owner.equals(user);
	}
	/**
	 * Allows a user to access the devices of specific section of the house.
	 * 
	 * @param owner the user attempting to allow access (must be the house owner)
	 * @param user the user to allow
	 * @param sectionName the name of the section to which to allow the user access
	 */
	boolean allowUser(User owner, User user, String  sectionName) {
		if (isOwner(owner)) {
			if (sectionName.equals("all")) {
				for (Section s : sections) {
					s.addAllowedUser(user);
				}
				return true;
			}
			for (Section s : sections) {
				if (s.getName().charAt(0) == sectionName.charAt(0)) {
					s.addAllowedUser(user);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Disallows a user from accessing the devices of specific section of the house.
	 *
	 * @param owner the user attempting to disallow access (must be the house owner)
	 * @param user the user to disallow
	 * @param sectionName the name of the section from which to disallow the user
	 */
	void disallowUser(User owner, User user, String  sectionName) {
		if (isOwner(owner)) {
			for (Section s : sections) {
				if (s.getName().equals(sectionName)) {
					s.removeAllowedUser(user);
					break;
				}
			}
		}
	}

	boolean isUserAllowed(User user, String deviceName) {
		for (Section s : sections) {
			if (s.hasDevice(deviceName) && (s.isUserAllowed(user) || isOwner(user))) {
				return true;
			}
		}
		return false;
	}

	boolean isUserAllowedInSection(User user, String sectionName) {
		for (Section s : sections) {
			boolean match = sectionName.length() == 1
				? s.getName().charAt(0) == sectionName.charAt(0)
				: s.getName().equals(sectionName);
			if (match) return s.isUserAllowed(user) || isOwner(user);
		}
		return false;
	}

	boolean hasSection(String sectionName) {
		for (Section s : sections) {
			boolean match = sectionName.length() == 1
				? s.getName().charAt(0) == sectionName.charAt(0)
				: s.getName().equals(sectionName);
			if (match) return true;
		}
		return false;
	}

	boolean turnOnDevice(User user, String deviceName, int time) {
		for (Section s : sections) {
			if(s.turnOnDevice(deviceName, time, user)) {
				return true;
				
			}
			
			
		}
		return false;
	}

	int getDeviceUpTime(String deviceName) {
		for (Section s : sections) {
			try {
				return s.getDeviceUpTime(deviceName);
			} catch (IllegalArgumentException e) {
				// Device not found in this section, continue searching
			}
		}
		throw new IllegalArgumentException("Device not found in any section of the house");
	}

	/**
	 * Adds one section to the house.
	 *
	 * @param section the section to add
	 */
	void addSection(Section section) {
		sections.add(section);
		section.addAllowedUser(owner);
	}

	/**
	 * Adds multiple sections to the house.
	 *
	 * @param sections the sections to add
	 */
	void addSections(List<Section> sections) {
		this.sections.addAll(sections);
		for (Section s : sections) {
			s.addAllowedUser(owner);
		}
	}

	List<Section> getSections() {
		return sections;
	}

	boolean isUserAllowedInAllSections(User user) {
		if (isOwner(user)) {
			return true;
		}
		for (Section s : sections) {
			if (!s.isUserAllowed(user)) {
				return false;
			}
		}
		return true;
	}

	
	String toText() {
		StringBuilder sb = new StringBuilder();
		sb.append("HOUSE:").append(name).append(":").append(owner.getName()).append("\n");
		for (Section s : sections) {
			sb.append(s.toText());
		}
		return sb.toString();
	}


}

