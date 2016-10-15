package tel_ran.database.mongo.constructor;

import java.util.*;

import org.bson.Document;

import tel_ran.database.mongo.model.dao.MongoTemplate;
import tel_ran.persons.entities.*;

public class MongoTemplateTestAppl {
	public static void main(String[] args) throws Exception {
		MongoTemplate<Person,Integer> mongoTemplate = new MongoTemplate<>
		("mongodb://root:12345@ds053166.mlab.com:53166/", "bsh_persons", "persons");
		mongoTemplate.drop();
		LinkedList<Person> persons = new LinkedList<>();
		for(int i = 0; i < 5; i++){
			persons.add(new Person((int)(Math.random() * 1000000 ), 2000 + i, "name" + i));
		}
		mongoTemplate.saveMany(persons);
		mongoTemplate.saveOne(new Person(12345000, 2016, "name1"));
		mongoTemplate.saveOne(new Child(12345, 2017, "name1","sun"));
		
		System.out.println(mongoTemplate.findOne(12345));
		System.out.println(mongoTemplate.findMany(new Document("name","name1")));
	}
}