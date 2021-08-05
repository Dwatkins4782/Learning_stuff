package com.ercot.puppetfiles.v1.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PuppetfilesController.class)
public class PuppetfilesControllerTest {

    private static final String READ_API_ENDPOINT = "/puppetfiles/v1/read/%1$s/%2$s";

    private static final String TEST_HOST = "dvtlsdt0008";
    private static final String TEST_TYPE_JIRA = "jira.yaml";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void checkReadPuppetfile() throws Exception {
        final RequestBuilder requestBuilder = MockMvcRequestBuilders.get(String.format(READ_API_ENDPOINT, TEST_HOST, TEST_TYPE_JIRA)).accept(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
        final File resource = new ClassPathResource(TEST_HOST + File.separator + TEST_TYPE_JIRA).getFile();
        final String jiraYaml = new String(Files.readAllBytes(resource.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals(jiraYaml, result.getResponse().getContentAsString());
    }
}