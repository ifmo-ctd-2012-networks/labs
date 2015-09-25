package com.company;

import org.junit.Assert;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Alex on 19.09.2015.
 */
public class DataTest extends Assert {

	@org.junit.Test
	public void testGetBytes() throws Exception {
		Main.Data data = new Main.Data();
		Main.Data data1 = new Main.Data(data.getBytes());
		System.out.println(Arrays.toString(data.getBytes()));
		System.out.println(Arrays.toString(data1.getBytes()));
		assertEquals(data,data1);
	}
}