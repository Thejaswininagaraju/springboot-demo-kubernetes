package com.example.k8s.springbootmongo.springbootk8smongo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
class SpringbootK8sMongoApplicationTests {

	@Autowired
    private MockMvc mockMvc;
	@Test
	 public void getsAllProducts() throws Exception {
	        mockMvc.perform(MockMvcRequestBuilders.get("/findAllProducts")
	                .accept(MediaType.APPLICATION_JSON))
	                .andExpect(status().isOk())
	                .andReturn();
	    }

}
