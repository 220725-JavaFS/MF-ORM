package com.revature.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.Driver;
import com.revature.utils.ConnectionUtil;

import dev.rehm.exceptions.JsonMappingException;

public class ORM {

	private Connection connection;
	private static Logger log = LoggerFactory.getLogger(ORM.class);
	
	public ORM() {

	}

	public ORM(Connection connection) {
		this.connection = connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public int storeObject(Object o) {

		StringBuilder statmentBuilder = new StringBuilder();
		statmentBuilder.append("INSERT INTO ");

		// obtain the names of the fields in the object
		Class<?> objectClass = o.getClass();
		String[] tokens = objectClass.getName().split(".");
		String className = tokens[tokens.length - 1];

		statmentBuilder.append(className + "(");

		Field[] fields = objectClass.getFields();

		StringBuilder fieldBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		for (Field field : fields) {

			String fieldName = field.getName();

			if (fieldName == "id") {
				continue;
			}

			fieldBuilder.append(fieldName + ",");

			// obtain the appropriate getter (using the field name)
			String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
//			System.out.println(getterName);

			try {
				// obtain the getter method from the class we are mapping
				Method getterMethod = objectClass.getMethod(getterName);

				// invoke that method on the object that we are mapping
				Object fieldValue = getterMethod.invoke(o);

				valueBuilder.append(fieldValue + ",");

			} catch (NoSuchMethodException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (SecurityException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (IllegalArgumentException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				log.error(e.getLocalizedMessage(), e);
			}

		}

		fieldBuilder.deleteCharAt(fieldBuilder.length() - 1);
		valueBuilder.deleteCharAt(valueBuilder.length() - 1);

		statmentBuilder.append(fieldBuilder + ") VALUES (" + valueBuilder + ") RETURNING id;");

		String sql = statmentBuilder.toString();
		log.info(sql);
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			if (result.next()) {
				return result.getInt("id");
			}

		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return -1;
	}

	public <T> List<T> retriveObject(Class<T> clazz) {

		ArrayList<T> objects = new ArrayList<T>();

		String[] tokens = clazz.getName().split(".");
		String className = tokens[tokens.length - 1];

		Field[] fields = clazz.getFields();

		String sql = "SELECT * from " + className + ";";
		log.info(sql);

		try {

			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				T newObject = null;

				try {
					// create a new instance of the class being constructed
					newObject = clazz.getDeclaredConstructor().newInstance();

				} catch (InstantiationException | IllegalAccessException | InvocationTargetException
						| NoSuchMethodException e) {
					e.printStackTrace();
				}

				for (Field field : fields) {

					String fieldName = field.getName();

					// obtain the appropriate getter (using the field name)
					String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					try {
						// getting the type of the setter parameter, based on the field type
						Class<?> setterParamType = clazz.getDeclaredField(fieldName).getType();

						// obtain the setter method using the setter name and setter parameter type
						Method setter = clazz.getMethod(setterName, setterParamType);

						// below we define a utility method to convert the string field value to the

						// appropriate type for the field
						Object fieldValue = convertStringToFieldType(result.getString(fieldName), setterParamType);

						// we invoke the setter to populate the field of the object that's being created
						setter.invoke(newObject, fieldValue);

					} catch (NoSuchFieldException e) {
						log.error(e.getLocalizedMessage(), e);

					} catch (NoSuchMethodException e) {
						log.error(e.getLocalizedMessage(), e);
					} catch (IllegalAccessException e) {
						log.error(e.getLocalizedMessage(), e);
					} catch (InvocationTargetException | InstantiationException e) {
						log.error(e.getLocalizedMessage(), e);
					}
				}

				objects.add(newObject);

			}

			return objects;
		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	public void updateObject(Object o) {

		StringBuilder statmentBuilder = new StringBuilder();
		statmentBuilder.append("UPDATE ");

		// obtain the names of the fields in the object
		Class<?> objectClass = o.getClass();
		String[] tokens = objectClass.getName().split(".");
		String className = tokens[tokens.length - 1];

		statmentBuilder.append(className + " SET ");

		Field[] fields = objectClass.getFields();

		String id = "";
		for (Field field : fields) {

			String fieldName = field.getName();

			// obtain the appropriate getter (using the field name)
			String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
//			System.out.println(getterName);

			try {
				// obtain the getter method from the class we are mapping
				Method getterMethod = objectClass.getMethod(getterName);

				// invoke that method on the object that we are mapping
				Object fieldValue = getterMethod.invoke(o);

				if (fieldName == "id") {
					id = fieldValue.toString();
					continue;
				}
				statmentBuilder.append(fieldName + " = " + fieldValue + ",");

			} catch (NoSuchMethodException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (SecurityException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (IllegalArgumentException e) {
				log.error(e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {

				log.error(e.getLocalizedMessage(), e);
			}

		}

		statmentBuilder.deleteCharAt(statmentBuilder.length() - 1);
		statmentBuilder.append(" WHERE id = " + id + ";");

		String sql = statmentBuilder.toString();
		log.info(sql);
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
		}

	}

	public void deleteObject(Object o) {

		StringBuilder statmentBuilder = new StringBuilder();
		statmentBuilder.append("DELETE FROM ");

		// obtain the names of the fields in the object
		Class<?> objectClass = o.getClass();
		String[] tokens = objectClass.getName().split(".");
		String className = tokens[tokens.length - 1];

		statmentBuilder.append(className + " WHERE  id  =");

		String getterName = "getId";

		try {
			// obtain the getter method from the class we are mapping
			Method getterMethod = objectClass.getMethod(getterName);

			// invoke that method on the object that we are mapping
			Object fieldValue = getterMethod.invoke(o);

			statmentBuilder.append(fieldValue + ";");

		} catch (NoSuchMethodException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (SecurityException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			log.error(e.getLocalizedMessage(), e);
		}

		String sql = statmentBuilder.toString();
		log.info(sql);
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
		}

	}

	private Object convertStringToFieldType(String input, Class<?> type)
			throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		switch (type.getName()) {
		case "byte":
			return Byte.valueOf(input);
		case "short":
			return Short.valueOf(input);
		case "int":
			return Integer.valueOf(input);
		case "long":
			return Long.valueOf(input);
		case "double":
			return Double.valueOf(input);
		case "float":
			return Float.valueOf(input);
		case "boolean":
			return Boolean.valueOf(input);
		case "java.lang.String":
			return input;
		case "java.time.LocalDate":
			return LocalDate.parse(input);
		default:
			return type.getDeclaredConstructor().newInstance();
		}
	}
}
