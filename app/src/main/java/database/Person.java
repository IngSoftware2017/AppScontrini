package database;

/**
 * Represents one person that does some missions
 * @author Marco Olivieri on 26/11/2017 (Team 3)
 */

public class Person {

    private int ID;
    private String name;
    private String lastName;
    private String academicTitle;

    /**
     * Non parametric constructor
     */
    public Person() {
    }

    /**
     * Parametric constructor
     *
     * @param ID Unique ID of the Person
     * @param name Name of the person
     * @param lastName Last Name of the person
     * @param academicTitle Academic Title of the person
     */
    public Person(int ID, String name, String lastName, String academicTitle) {
        this.ID = ID;
        this.name = name;
        this.lastName = lastName;
        this.academicTitle = academicTitle;
    }
}
