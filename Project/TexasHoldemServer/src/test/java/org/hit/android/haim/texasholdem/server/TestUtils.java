package org.hit.android.haim.texasholdem.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

/**
 * @author Haim Adrian
 * @since 21-Mar-21
 */
public class TestUtils {
   public static String getJwtTokenFromMvcResult(MvcResult mvcResult) throws UnsupportedEncodingException, JsonProcessingException {
      return "Bearer " + new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class).get("token").textValue();
   }
}

