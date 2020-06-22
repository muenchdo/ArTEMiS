package de.tum.in.www1.artemis.connector.jira;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import org.hamcrest.text.MatchesPattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.in.www1.artemis.service.connectors.jira.dto.JiraUserDTO;
import de.tum.in.www1.artemis.service.connectors.jira.dto.JiraUserDTO.JiraUserGroupDTO;
import de.tum.in.www1.artemis.service.connectors.jira.dto.JiraUserDTO.JiraUserGroupsDTO;

@Component
@Profile("jira")
public class JiraRequestMockProvider {

    @Value("${artemis.user-management.external.url}")
    private URL JIRA_URL;

    private final RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    public JiraRequestMockProvider(@Qualifier("jiraRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enableMockingOfRequests() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    public void mockIsGroupAvailable(String group) throws URISyntaxException {
        final var uriPattern = Pattern.compile(JIRA_URL + "/rest/api/2/group/member\\?groupname=" + group);

        mockServer.expect(requestTo(MatchesPattern.matchesPattern(uriPattern))).andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK));
    }

    public void mockIsGroupAvailableForMultiple(Set<String> groups) throws URISyntaxException {
        final var regexGroups = String.join("|", groups);
        final var uriPattern = Pattern.compile(JIRA_URL + "/rest/api/2/group/member\\?groupname=(" + regexGroups + ")");

        mockServer.expect(ExpectedCount.times(groups.size()), requestTo(MatchesPattern.matchesPattern(uriPattern))).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK));
    }

    public void mockAddUserToGroup(String group) throws URISyntaxException {
        mockIsGroupAvailable(group);
        final var uriPattern = Pattern.compile(JIRA_URL + "/rest/api/2/group/user\\?groupname=" + group);

        mockServer.expect(requestTo(MatchesPattern.matchesPattern(uriPattern))).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK));
    }

    public void mockAddUserToGroupForMultipleGroups(Set<String> groups) throws URISyntaxException {
        mockIsGroupAvailableForMultiple(groups);
        final var regexGroups = String.join("|", groups);
        final var uriPattern = Pattern.compile(JIRA_URL + "/rest/api/2/group/user\\?groupname=(" + regexGroups + ")");

        mockServer.expect(ExpectedCount.times(groups.size()), requestTo(MatchesPattern.matchesPattern(uriPattern))).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK));
    }

    public void mockRemoveUserFromGroup(Set<String> groups, String username) {
        final var regexGroups = String.join("|", groups);
        final var uriPattern = Pattern.compile(JIRA_URL + "/rest/api/2/group/user\\?groupname=(" + regexGroups + ")&username=" + username);

        mockServer.expect(ExpectedCount.twice(), requestTo(MatchesPattern.matchesPattern(uriPattern))).andExpect(method(HttpMethod.DELETE)).andRespond(withStatus(HttpStatus.OK));
    }

    public void mockGetUsernameForEmail(String email, String usernameToBeReturned) throws IOException, URISyntaxException {
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/user/search").queryParam("username", email).build().toUri();
        final var response = List.of(Map.of("name", usernameToBeReturned));

        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(response)));
    }

    public void mockCreateGroup(String groupName) throws URISyntaxException, JsonProcessingException {
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/group").build().toUri();
        final var body = Map.of("name", groupName);
        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.POST)).andExpect(content().json(mapper.writeValueAsString(body))).andRespond(withStatus(HttpStatus.OK));
    }

    public void mockDeleteGroup(String groupName) throws URISyntaxException {
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/group").queryParam("groupname", groupName).build().toUri();
        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.DELETE)).andRespond(withStatus(HttpStatus.OK));
    }

    public void mockGetOrCreateUserLti(String authUsername, String password, String username, String email, String firstName, Set<String> groups)
            throws URISyntaxException, IOException {
        final var response = new JiraUserDTO();
        final var groupsResponse = new JiraUserGroupsDTO();
        final var groupDTOs = new HashSet<JiraUserGroupDTO>();
        for (final var group : groups) {
            final var groupDTO = new JiraUserGroupDTO();
            groupDTO.setName(group);
            groupDTO.setSelf(new URL("http://localhost:8080/" + group));
            groupDTOs.add(groupDTO);
        }
        groupsResponse.setSize(groups.size());
        groupsResponse.setItems(groupDTOs);
        response.setName(username);
        response.setDisplayName(firstName);
        response.setEmailAddress(email);
        response.setGroups(groupsResponse);
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/user").queryParam("username", username).queryParam("expand", "groups").build().toUri();
        final var auth = authUsername + ":" + password;
        final var authHeader = new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));

        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Basic " + authHeader))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(response)));
    }

    public void mockGetOrCreateUserJira(String username, String email, String firstName, Set<String> groups) throws URISyntaxException, IOException {
        final var response = new JiraUserDTO();
        final var groupsResponse = new JiraUserGroupsDTO();
        final var groupDTOs = new HashSet<JiraUserGroupDTO>();
        for (final var group : groups) {
            final var groupDTO = new JiraUserGroupDTO();
            groupDTO.setName(group);
            groupDTO.setSelf(new URL("http://localhost:8080/" + group));
            groupDTOs.add(groupDTO);
        }
        groupsResponse.setSize(groups.size());
        groupsResponse.setItems(groupDTOs);
        response.setName(username);
        response.setDisplayName(firstName);
        response.setEmailAddress(email);
        response.setGroups(groupsResponse);
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/user").queryParam("username", username).queryParam("expand", "groups").build().toUri();

        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(response)));
    }

    public void mockGetOrCreateUserJiraCaptchaException(String username, String email, String firstName, Set<String> groups) throws URISyntaxException, IOException {
        final var response = new JiraUserDTO();
        final var groupsResponse = new JiraUserGroupsDTO();
        final var groupDTOs = new HashSet<JiraUserGroupDTO>();
        for (final var group : groups) {
            final var groupDTO = new JiraUserGroupDTO();
            groupDTO.setName(group);
            groupDTO.setSelf(new URL("http://localhost:8080/" + group));
            groupDTOs.add(groupDTO);
        }
        groupsResponse.setSize(groups.size());
        groupsResponse.setItems(groupDTOs);
        response.setName(username);
        response.setDisplayName(firstName);
        response.setEmailAddress(email);
        response.setGroups(groupsResponse);
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/user").queryParam("username", username).queryParam("expand", "groups").build().toUri();

        var headers = new HttpHeaders();
        headers.add("X-Authentication-Denied-Reason", "captcha");
        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).headers(headers).body(mapper.writeValueAsString(response)));
    }

    public void mockCreateUserInExternalUserManagement(String username, String fullname, String email) throws URISyntaxException, JsonProcessingException {
        final var path = UriComponentsBuilder.fromUri(JIRA_URL.toURI()).path("/rest/api/2/user").build().toUri();
        Map<String, Object> body = new HashMap<>();
        body.put("key", username);
        body.put("name", username);
        body.put("displayName", fullname);
        body.put("emailAddress", email);
        body.put("applicationKeys", List.of("jira-software"));

        mockServer.expect(requestTo(path)).andExpect(method(HttpMethod.POST)).andExpect(content().json(mapper.writeValueAsString(body))).andRespond(withStatus(HttpStatus.OK));
    }
}
