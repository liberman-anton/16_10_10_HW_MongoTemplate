package tel_ran.database.mongo;


import java.util.*;
import java.beans.*;
import java.lang.reflect.*;

import org.bson.Document;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import tel_ran.databases.mongo.MongoConnection;
import tel_ran.persons.entities.Person;


public class MongoTemplate<T,ID> {
	private MongoCollection<Document> objects;
	private String keyFieldName = "_id";
	
	public MongoTemplate(String uriStr, String databaseName, String keyFieldName, String collectionName){
		MongoConnection mongoConnection = MongoConnection.getMongoConnection(uriStr, databaseName);
		objects = mongoConnection.getDataBase().getCollection(collectionName);
		if(keyFieldName != null)
			this.keyFieldName = keyFieldName;
	}
	
	public void drop(){
		objects.drop();
	}

	public void saveOne(T obj) throws Exception{
		objects.insertOne(getDocument(obj));
	}
	
	private Document getDocument(T obj) throws Exception {
		Map<String, Object> map = introspect(obj);
		if(keyFieldName != "_id" && map.containsKey(keyFieldName)){
			map.put("_id", map.remove(keyFieldName));
		}
		Document res = new Document(map);
		return res;
	}
	
	public static Map<String, Object> introspect(Object obj) throws Exception {
	    Map<String, Object> result = new HashMap<String, Object>();
	    BeanInfo info = Introspector.getBeanInfo(obj.getClass());
	    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
	        Method reader = pd.getReadMethod();
	        if (reader != null)
	            result.put(pd.getName(), reader.invoke(obj));
	    }
		result.put("_class",result.remove("class").toString().substring(6));
	    return result;
	}

	public void saveMany(List<T> objects) throws Exception{
		for(T obj : objects){
			this.objects.insertOne(getDocument(obj));
		}
	}
	public T findOne(ID id) throws Exception{
		Document query = new Document("_id",id);
		FindIterable<Document> resIterable = objects.find(query);
		if(resIterable == null)
			return null;
		Document resDocument = resIterable.first();
		if(resDocument == null)
			return null;
		return getObjFromDocument(resDocument);
	}
	
	@SuppressWarnings("unchecked")
	private T getObjFromDocument(Document document) throws Exception{
		BeanWrapper object = new BeanWrapperImpl
				(Class.forName(document.getString("_class")).newInstance());
	    for (Map.Entry<String, Object> property : document.entrySet()) {
	        object.setPropertyValue(property.getKey(), property.getValue());
	    }
		return (T) object.getWrappedInstance();
	}

	public Iterable<T> findMany(Document query) throws Exception{
		FindIterable<Document> resIterable = objects.find(query);
		return getObjectsFromResIterable(resIterable);		
	}

	private Iterable<T> getObjectsFromResIterable(FindIterable<Document> resIterable) throws Exception {
		ArrayList<T> res = new ArrayList<>();
		for(Document document : resIterable)
			res.add(getObjFromDocument(document));
		return res;
	}
}
