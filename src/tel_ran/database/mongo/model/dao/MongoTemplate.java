package tel_ran.database.mongo.model.dao;

import java.util.*;
//import java.beans.*;
import java.lang.reflect.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import tel_ran.database.mongo.model.annotations.*;
//import org.springframework.beans.BeanWrapper;
//import org.springframework.beans.BeanWrapperImpl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;

import tel_ran.databases.mongo.MongoConnection;


public class MongoTemplate<T,ID> {
	private MongoCollection<Document> objects;
	private String keyFieldName;
	
	public MongoTemplate(String uriStr, String databaseName, String collectionName){
//TODO		delete parameter keyFieldName and do annotation
//TODO если несколко annotatyion id, то exeption
		MongoConnection mongoConnection = MongoConnection.getMongoConnection(uriStr, databaseName);
		objects = mongoConnection.getDataBase().getCollection(collectionName);
	}
	
	public void drop(){
		objects.drop();
	}

	public void saveOne(T obj) throws Exception{
//TODO		create index if index not exist
		objects.insertOne(getDocument(obj));
	}
	
	private Document getDocument(T obj) throws Exception {
		Map<String, Object> map = introspect(obj);
		if(keyFieldName == null)
			setKeyFieldNameAndIndex(obj.getClass());
		if(keyFieldName != "_id" && map.containsKey(keyFieldName)){
			map.put("_id", map.remove(keyFieldName));
		}
		Document res = new Document(map);
		return res;
	}
	
//	private static Map<String, Object> introspect(Object obj) throws Exception {
//    Map<String, Object> result = new HashMap<String, Object>();
//    BeanInfo info = Introspector.getBeanInfo(obj.getClass());
//    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
//        Method reader = pd.getReadMethod();
//        if (reader != null)
//            result.put(pd.getName(), reader.invoke(obj));
//    }
//	result.put("_class",result.remove("class").toString().substring(6));
//    return result;
//}
	
	private void setKeyFieldNameAndIndex(Class<? extends Object> clazz) throws Exception {
		List<Field> fields = getListFields(clazz);
		boolean flag = true;
		for(Field field : fields){
			if(field.isAnnotationPresent(Id.class))
				if(flag){
					keyFieldName = field.getName();
					flag = false;
				} else
					throw new Exception();
			else if(field.isAnnotationPresent(Index.class)){
				objects.createIndex(
						new Document(field.getName(), 1),
								new IndexOptions().unique(
										field.getAnnotation(Index.class).unique())
						);
				}
		}
		if(keyFieldName == null)
			keyFieldName = "_id";
		
	}

	private static Map<String, Object> introspect(Object obj) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		Class<?> clazz = obj.getClass();
		HashMap<String,Method> methods = getMapMethods(clazz);
		LinkedList<Field> fields = getListFields(clazz);
		for(Field field : fields ){
			StringBuilder bilder = new StringBuilder("get");
			String fieldName = field.getName();
			bilder.append(Character.toUpperCase(fieldName.charAt(0))).append(fieldName.substring(1));
			String key = bilder.toString();
			if(methods.containsKey(key)){
				res.put(fieldName, methods.get(key).invoke(obj));
			}
		}
		res.put("_class", clazz.getName());
		return res;		
	}

	private static LinkedList<Field> getListFields(Class<?> clazz) {
		LinkedList<Field> res = new LinkedList<>();
		res.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Class<?> superClass = clazz.getSuperclass();
		while(superClass != Object.class){
			res.addAll(Arrays.asList(superClass.getDeclaredFields()));
			superClass = superClass.getSuperclass();
		}
		return res;
	}
	private static HashMap<String, Field> getMapFields(Class<?> clazz) {
		HashMap<String, Field> res = new HashMap<>();
		for(Field field : Arrays.asList(clazz.getDeclaredFields())){
			res.put(field.getName(), field);
		}
		Class<?> superClass = clazz.getSuperclass();
		while(superClass != Object.class){
			for(Field field : Arrays.asList(superClass.getDeclaredFields())){
				res.put(field.getName(), field);
			}
			superClass = superClass.getSuperclass();
		}
		return res;
	}

	private static HashMap<String,Method> getMapMethods(Class<?> clazz) {
		LinkedList<Method> methods = new LinkedList<>();
		methods .addAll(Arrays.asList(clazz.getMethods()));
		Class<?> superClass = clazz.getSuperclass();
		while(superClass != Object.class){
			methods.addAll(Arrays.asList(superClass.getMethods()));
			superClass = superClass.getSuperclass();
		}
		HashMap<String,Method> res = new HashMap<>();
		for(Method method : methods){
			res.put(method.getName(), method);
		}
		return res;
	}

	public void saveMany(List<T> objects) throws Exception{
		LinkedList<Document> listDoc = new LinkedList<>();
		for(T obj : objects){
			listDoc.add(getDocument(obj));
		}
		this.objects.insertMany(listDoc);
	}
	public T findOne(ID id) throws Exception{
		Document query = new Document("_id",id);
		FindIterable<Document> resIterable = objects.find(query);
		if(resIterable == null)
			return null;
		Document document = resIterable.first();
		if(document == null)
			return null;
		Document res = updatePrimaryKeyInDocument(document);
		return getObjFromDocument(res);
	}
	
	private Document updatePrimaryKeyInDocument(Document document) {
//		if(keyFieldName == null)
//			document.remove("_id");
//		else
		if(keyFieldName != "_id")
			document.put(keyFieldName,document.remove("_id"));
		return document;
	}
	private Document updatePrimaryKeyInQuery(Document document) {
		Document res = new Document();
		for(Map.Entry<String, Object> entry : document.entrySet()){
			if(entry.getKey() == keyFieldName)
				res.put(keyFieldName, document.remove("_id"));
			else
				res.put(entry.getKey(), entry.getValue());
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private T getObjFromDocument(Document document) throws Exception{
		Class<?> clazz = Class.forName((String) document.remove("_class"));
		Object res = clazz.newInstance();
		HashMap<String,Field> mapFieldsOfClasses = getMapFields(clazz);
		for (Map.Entry<String, Object> entry : document.entrySet()){
			String key = entry.getKey();
			if(mapFieldsOfClasses.containsKey(key)){
				Field field = mapFieldsOfClasses.get(key);
				field.setAccessible(true);
				field.set(res, entry.getValue());
			}
		}
		return (T)res ;
	}

//	@SuppressWarnings("unchecked")
//	private T getObjFromDocument(Document document) throws Exception{
//		BeanWrapper object = new BeanWrapperImpl
//				(Class.forName((String) document.remove("_class")).newInstance());
//	    for (Map.Entry<String, Object> property : document.entrySet()) {
//	        object.setPropertyValue(property.getKey(), property.getValue());
//	    }
//		return (T) object.getWrappedInstance();
//	}

	public Iterable<T> findMany(Document query) throws Exception{
		FindIterable<Document> resIterable = objects.find(updatePrimaryKeyInQuery(query));
		return getIterableFromResIterable(resIterable);		
	}

	private Iterable<T> getIterableFromResIterable(FindIterable<Document> resIterable) throws Exception {
		List<T> res = new LinkedList<>();
		for(Document document : resIterable)
			res.add(getObjFromDocument(updatePrimaryKeyInDocument(document)));
		return res;
	}
}
