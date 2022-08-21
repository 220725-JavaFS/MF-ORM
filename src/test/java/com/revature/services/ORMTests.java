package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ORMTests {
	private BookRepo mockRepo = Mockito.mock(BookRepo.class);
	private BookService bookService = new BookService(mockRepo);
	private Book testBook = new Book("Adam", "Smith", "The Wealth of Nations", 56.72);
	
	@Test
	public void testFindByTitle() {
		Mockito.when(mockRepo.getBookByTitleFromDB("The Wealth of Nations"))
			.thenReturn(new Book("Adam", "Smith", "The Wealth of Nations", 56.72));
		Book b = bookService.getBookByTitle("The Wealth of Nations");
		assertEquals(testBook, b);
	}
}
