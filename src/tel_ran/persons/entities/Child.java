package tel_ran.persons.entities;

public class Child extends Person {
	private String kindergarten;

	public void setKindergarten(String kindergarten) {
		this.kindergarten = kindergarten;
	}

	@Override
	public String toString() {
		return "Child [kindergarten=" + kindergarten + ", " + super.toString() + "]";
	}

	public String getKindergarten() {
		return kindergarten;
	}
	public Child(){}
	public Child(int id, int birthYear, String name, String kindergarten) {
		super(id, birthYear, name);
		this.kindergarten = kindergarten;
	}
	
}
