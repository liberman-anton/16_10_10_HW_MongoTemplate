package tel_ran.database.mongo.model.test;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import tel_ran.database.mongo.model.dao.MongoTemplate;
import tel_ran.persons.entities.Child;
import tel_ran.persons.entities.Person;

public class MongoTemplateTest {
	private static MongoTemplate<Person,Integer> mongoTemplate = new MongoTemplate<>
		("mongodb://root:12345@ds053166.mlab.com:53166/", "bsh_persons", "persons");
	private static Person personExpect = new Person(12345000, 2016, "name10");
	private static Child childExpect = new Child(12345, 2017, "name10","sun");
	
	
	@Before
	public void setUp() throws Exception {
		mongoTemplate.drop();
	}

	@Test
	public void testSaveOne() {
		try {
			mongoTemplate.saveOne(personExpect);
			mongoTemplate.saveOne(childExpect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testSaveMany() {
		LinkedList<Person> persons = new LinkedList<>();
		for(int i = 0; i < 5; i++){
			persons.add(new Person((int)(Math.random() * 1000000 ), 2000 + i, "name" + i));
		}
		try {
			mongoTemplate.saveMany(persons);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testFindOne() {
		testSaveOne();
		Person person = new Person();
		Child child = new Child();
		try {
			person = mongoTemplate.findOne(12345000);
			child = (Child) mongoTemplate.findOne(12345);
			System.out.println(person);
			System.out.println(child);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(personExpect.toString(), person.toString());
		assertEquals(childExpect.toString(), child.toString());
	}
	@Test
	public void testFindMany() {
		testSaveOne();
		LinkedList<Person> persons = new LinkedList<>();
		try {
			persons = (LinkedList<Person>) mongoTemplate.findMany(new Document("name","name10"));
			System.out.println(persons);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(2,persons.size());
		assertEquals(personExpect.toString(),persons.removeFirst().toString());
		assertEquals(childExpect.toString(),persons.removeFirst().toString());
		assertEquals(0,persons.size());		
	}

}
