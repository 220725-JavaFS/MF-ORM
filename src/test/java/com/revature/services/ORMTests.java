package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.revature.models.TestObject;

public class ORMTests {
	private TestObject testObject = new TestObject(1, "object", "this is a test object");
	private ResultSet mockResultSet = Mockito.mock(ResultSet.class);
	private Connection mockConnection = Mockito.mock(Connection.class);
	private PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
	private ORM orm = new ORM(mockConnection);

	@BeforeEach
	public void mockObjects() {
		try {
			Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
			Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
			Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testStoreObject() {

		try {
			Mockito.when(mockResultSet.getInt("id")).thenReturn(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int id = orm.storeObject(testObject);
		assertEquals(testObject.id, id);
	}

	@Test
	public void testRetriveObject() {

		try {
			Mockito.when(mockResultSet.getString("id")).thenReturn("1");
			Mockito.when(mockResultSet.getString("name")).thenReturn("object");
			Mockito.when(mockResultSet.getString("description")).thenReturn("this is a test object");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<TestObject> testList = orm.retriveObject(TestObject.class);

		TestObject test = testList.get(0);

		assertEquals(testObject, test);
	}

	@Test
	public void testUpdateObject() {
		Boolean updated = orm.updateObject(testObject);
		assertTrue(updated);
	}

	@Test
	public void testDeleteObject() {
		Boolean updated = orm.deleteObject(testObject);
		assertTrue(updated);
	}
}
