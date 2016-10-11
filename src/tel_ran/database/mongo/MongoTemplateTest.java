package tel_ran.database.mongo;
import java.util.*;

import org.bson.Document;

import tel_ran.persons.entities.*;
public class MongoTemplateTest {
	public static void main(String[] args) throws Exception {
		MongoTemplate<Person,Integer> mongoTemplate = new MongoTemplate<>
		("mongodb://root:12345@ds053166.mlab.com:53166/", "bsh_persons", "id", "persons");
		LinkedList<Person> persons = new LinkedList<>();
		for(int i = 0; i < 10; i++){
			persons.add(new Person((int)(Math.random() * 1000000 ), 2000 + i, "name" + i));
		}
		mongoTemplate.drop();
		mongoTemplate.saveMany(persons);
		
		mongoTemplate.saveOne(new Person(12345000, 2016, "name1"));
		mongoTemplate.saveOne(new Child(12345, 2016, "name1","sun"));
		System.out.println(mongoTemplate.findMany(new Document("name","name1")));
		
		
	}
}
