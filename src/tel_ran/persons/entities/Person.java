package tel_ran.persons.entities;

import tel_ran.database.mongo.model.annotations.*;

public class Person {
	@Id
	private int id;
	@Index
	private int birthYear;
//	@Id
	private String name;
//	private String _class;
//	public String get_class() {
//		return _class;
//	}
//	public void set_class(String _class) {
//		this._class = _class;
//	}
	public int getId() {
		return id;
	}
	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBirthYear() {
		return birthYear;
	}
	public String getName() {
		return name;
	}
	public Person(int id, int birthYear, String name) {
		super();
		this.id = id;
		this.birthYear = birthYear;
		this.name = name;
	}
	public Person(){}
	@Override
	public String toString() {
		return "Person [id=" + id + ", birthYear=" + birthYear + ", name=" + name + "]";
	}
	
}
