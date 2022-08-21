package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.revature.models.TestObject;


public class ORMTests {
	private TestObject testObject = new TestObject(1, "object", "this is a test object");
	private ResultSet mockResultSet = Mockito.mock(ResultSet.class);
	private Connection mockConnection = Mockito.mock(Connection.class);
	private PreparedStatement mockPreparedStatement= Mockito.mock(PreparedStatement.class);
	private ORM orm= new ORM(mockConnection);

	@Test
	public void testStoreObject() {
		
		try {
			Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
			Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
			Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);
		
			Mockito.when(mockResultSet.getInt("id")).thenReturn(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int id = orm.storeObject(testObject);
		assertEquals(testObject.id, id);
	}
}
