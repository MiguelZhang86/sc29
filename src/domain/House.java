import java.util.ArrayList;
import java.util.List;

public class House {
	private String name;
	private List<Section> sections;
    private User owner;

	/**
	 * Creates a house with a name, sections (rooms) and an owner.
	 *
	 * @param name the house name
	 * @param sections the sections (rooms) of the house
	 * @param owner the owner of the house
	 */
	public House(String name, List<Section> sections, User owner) {
		this.name = name;
		this.sections = new ArrayList<Section>();
		this.owner = owner;
	}

	/**
	 * Gets the house name.
	 *
	 * @return the house name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks whether a user is the owner of this house.
	 *
	 * @param user the user to check
	 * @return true if the user is the owner, false otherwise
	 */
	public boolean isOwner(User user) {
		return this.owner.equals(user);
	}

	/**
	 * Gets a copy of the house sections list.
	 *
	 * @return a new list containing all sections
	 */
	public List<Section> getSections() {
		return new ArrayList<>(sections);
	}

	/**
	 * Adds one section to the house.
	 *
	 * @param section the section to add
	 */
	public void addSection(Section section) {
		sections.add(section);
	}

	/**
	 * Adds multiple sections to the house.
	 *
	 * @param sections the sections to add
	 */
	public void addSections(List<Section> sections) {
		this.sections.addAll(sections);
	}
}

