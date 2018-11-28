/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vizuri.customer.unit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;
    
    private static final String CUSTOMER_JSON = "{ \"id\":\"1\",\"firstName\":\"Test\",\"lastName\":\"User\"}";

    @Test
    public void testCustomerAPI() throws Exception {

    	this.mockMvc.perform(post("/customer").content(CUSTOMER_JSON).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
    	.andDo(print())
    	.andExpect(content().json(CUSTOMER_JSON));
    	
        this.mockMvc.perform(get("/customer/1")).andDo(print()).andExpect(status().isOk())
        .andDo(print())
		.andExpect(content().json(CUSTOMER_JSON));
        
        this.mockMvc.perform(delete("/customer/1")).andDo(print()).andExpect(status().isOk());
        

     }


}
