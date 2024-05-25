//package zzangmin.db_automation.controller;
//
//import com.google.gson.Gson;
//import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
//import com.slack.api.app_backend.util.JsonPayloadTypeDetector;
//import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
//import com.slack.api.methods.MethodsClient;
//import com.slack.api.methods.SlackApiException;
//import com.slack.api.model.Action;
//import com.slack.api.model.block.*;
//import com.slack.api.model.view.View;
//import com.slack.api.model.view.ViewState;
//import com.slack.api.util.json.GsonFactory;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import zzangmin.db_automation.entity.DatabaseRequestCommandGroup.CommandType;
//import zzangmin.db_automation.service.SlackService;
//
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class SlackControllerTest {
//
//    @Mock
//    private MethodsClient slackClient;
//
//    @Mock
//    private SlackService slackService;
//
//    @Mock
//    private SlackActionHandler slackActionHandler;
//
//    @Mock
//    private Gson gson;
//
//    @InjectMocks
//    private SlackController slackController;
//
//    private static final JsonPayloadTypeDetector payloadTypeDetector = new JsonPayloadTypeDetector();
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testSlackCallBack_BlockActions() throws IOException, SlackApiException {
//        // Arrange
//        String payload = "{ \"type\": \"block_actions\", ... }";
//        String requestBody = "request_body";
//        String slackSignature = "valid_signature";
//        String timestamp = "1234567890";
//
//        BlockActionPayload blockActionPayload = mock(BlockActionPayload.class);
//        View view = mock(View.class);
//        ViewState state = mock(ViewState.class);
//        List<LayoutBlock> viewBlocks = mock(List.class);
//        List<Action> actions = mock(List.class);
//
//        when(payloadTypeDetector.detectType(anyString())).thenReturn("block_actions");
//        when(GsonFactory.createSnakeCase().fromJson(anyString(), eq(BlockActionPayload.class))).thenReturn(blockActionPayload);
//        when(blockActionPayload.getView()).thenReturn(view);
//        when(view.getState()).thenReturn(state);
//        when(view.getBlocks()).thenReturn(viewBlocks);
//   //     when(blockActionPayload.getActions()).thenReturn(actions);
//
//        // Act
//        ResponseEntity<?> response = slackController.slackCallBack(payload, requestBody, slackSignature, timestamp, null);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(slackService, times(1)).validateRequest(slackSignature, timestamp, requestBody);
//        verify(slackActionHandler, times(1)).handleAction(any(), eq(viewBlocks), eq(state.getValues()));
//    }
////
//    @Test
//    public void testSlackCallBack_ViewSubmission() throws IOException, SlackApiException {
//        // Arrange
//        String payload = "{ \"type\": \"view_submission\", ... }";
//        String requestBody = "request_body";
//        String slackSignature = "valid_signature";
//        String timestamp = "1234567890";
//
//        ViewSubmissionPayload viewSubmissionPayload = mock(ViewSubmissionPayload.class);
//        View view = mock(View.class);
//        ViewState state = mock(ViewState.class);
//        List<LayoutBlock> viewBlocks = mock(List.class);
//        CommandType commandType = CommandType.ADD_COLUMN;
//
//        when(payloadTypeDetector.detectType(anyString())).thenReturn("view_submission");
//        when(GsonFactory.createSnakeCase().fromJson(anyString(), eq(ViewSubmissionPayload.class))).thenReturn(viewSubmissionPayload);
//        when(viewSubmissionPayload.getView()).thenReturn(view);
//        when(view.getBlocks()).thenReturn(viewBlocks);
//        when(view.getState()).thenReturn(state);
////        when(slackController.findCommandType(state)).thenReturn(commandType);
////        when(slackController.closeViewJsonString()).thenReturn("{\"response_action\": \"clear\"}");
//
//        // Act
//        ResponseEntity<?> response = slackController.slackCallBack(payload, requestBody, slackSignature, timestamp, null);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("{\"response_action\": \"clear\"}", response.getBody());
//        verify(slackService, times(1)).validateRequest(slackSignature, timestamp, requestBody);
//        verify(slackActionHandler, times(1)).handleSubmission(commandType, viewBlocks, state.getValues());
//    }
////
////    @Test
////    public void testSlackCallBack_ViewSubmissionWithException() throws IOException, SlackApiException {
////        // Arrange
////        String payload = "{ \"type\": \"view_submission\", ... }";
////        String requestBody = "request_body";
////        String slackSignature = "valid_signature";
////        String timestamp = "1234567890";
////
////        ViewSubmissionPayload viewSubmissionPayload = mock(ViewSubmissionPayload.class);
////        View view = mock(View.class);
////        ViewState state = mock(ViewState.class);
////        List<LayoutBlock> viewBlocks = mock(List.class);
////        Exception exception = new Exception("Test exception");
////
////        when(payloadTypeDetector.detectType(anyString())).thenReturn("view_submission");
////        when(GsonFactory.createSnakeCase().fromJson(anyString(), eq(ViewSubmissionPayload.class))).thenReturn(viewSubmissionPayload);
////        when(viewSubmissionPayload.getView()).thenReturn(view);
////        when(view.getBlocks()).thenReturn(viewBlocks);
////        when(view.getState()).thenReturn(state);
////        when(slackController.findCommandType(state)).thenThrow(exception);
////        when(slackController.displayErrorViewJsonString(exception, viewBlocks)).thenReturn("{\"error\": \"Test exception\"}");
////
////        // Act
////        ResponseEntity<?> response = slackController.slackCallBack(payload, requestBody, slackSignature, timestamp, null);
////
////        // Assert
////        assertNotNull(response);
////        assertEquals(HttpStatus.OK, response.getStatusCode());
////        assertEquals("{\"error\": \"Test exception\"}", response.getBody());
////        verify(slackService, times(1)).validateRequest(slackSignature, timestamp, requestBody);
////        verify(slackActionHandler, never()).handleSubmission(any(), any(), any());
////    }
////
////    @Test
////    public void testSlackCallBack_UnsupportedPayload() throws IOException, SlackApiException {
////        // Arrange
////        String payload = "{ \"type\": \"unsupported_payload\", ... }";
////        String requestBody = "request_body";
////        String slackSignature = "valid_signature";
////        String timestamp = "1234567890";
////
////        when(payloadTypeDetector.detectType(anyString())).thenReturn("unsupported_payload");
////
////        // Act and Assert
////        assertThrows(IllegalArgumentException.class, () -> {
////            slackController.slackCallBack(payload, requestBody, slackSignature, timestamp, null);
////        });
////        verify(slackService, times(1)).validateRequest(slackSignature, timestamp, requestBody);
////    }
////
////    @Test
////    public void testGenerateSlackTagUserString() {
////        // Arrange
////        String userId = "U12345";
////        String expectedResult = "<@U12345>";
////
////        // Act
////        String result = slackController.generateSlackTagUserString(userId);
////
////        // Assert
////        assertEquals(expectedResult, result);
////    }
////
////    @Test
////    public void testFindCommandType() {
////        // Arrange
////        ViewState state = mock(ViewState.class);
////        Map<String, SelectCommand.StateValue> values = mock(Map.class);
////
////        when(state.getValues()).thenReturn(values);
////        when(values.containsKey("command")).thenReturn(true);
////        when(values.get("command")).thenReturn(new SelectCommand.StateValue("READ"));
////
////        // Act
////        CommandType result = slackController.findCommandType(state);
////
////        // Assert
////        assertEquals(CommandType.READ, result);
////    }
////
////    @Test
////    public void testUpdateView() throws IOException, SlackApiException {
////        // Arrange
////        List<LayoutBlock> viewBlocks = mock(List.class);
////        View view = mock(View.class);
////        ViewsUpdateRequest viewsUpdateRequest = mock(ViewsUpdateRequest.class);
////        ViewsUpdateResponse viewsUpdateResponse = mock(ViewsUpdateResponse.class);
////
////        when(slackService.findGlobalRequestModalView(viewBlocks)).thenReturn(viewsUpdateRequest);
////        when(slackClient.viewsUpdate(viewsUpdateRequest)).thenReturn(viewsUpdateResponse);
////
////        // Act
////        slackController.updateView(viewBlocks, view);
////
////        // Assert
////        verify(slackService, times(1)).findGlobalRequestModalView(viewBlocks);
////        verify(slackClient, times(1)).viewsUpdate(viewsUpdateRequest);
////    }
////
////    @Test
////    public void testCloseViewJsonString() {
////        // Arrange
////        String expectedJson = "{\"response_action\": \"clear\"}";
////
////        // Act
////        String result = slackController.closeViewJsonString();
////
////        // Assert
////        assertEquals(expectedJson, result);
////    }
////
////    @Test
////    public void testDisplayErrorViewJsonString() {
////        // Arrange
////        Exception exception = new Exception("Test exception");
////        List<LayoutBlock> viewBlocks = mock(List.class);
////        String expectedJson = "{\"response_action\": \"update\", \"view\": {\"blocks\": [], \"error\": \"Test exception\"}}";
////
////        // Act
////        String result = slackController.displayErrorViewJsonString(exception, viewBlocks);
////
////        // Assert
////        assertEquals(expectedJson, result);
////    }
////
////    @Test
////    public void testDatabaseRequestCommand() throws SlackApiException, IOException {
////        // Arrange
////        String token = "valid_token";
////        String teamId = "T12345";
////        String teamDomain = "example.com";
////        String channelId = "C12345";
////        String channelName = "general";
////        String userId = "U12345";
////        String userName = "john_doe";
////        String command = "/dbselect";
////        String text = "read users";
////        String responseUrl = "https://example.com/response";
////        String triggerId = "trigger_id";
////        String requestBody = "request_body";
////        String slackSignature = "valid_signature";
////        String timestamp = "1234567890";
////
////        List<LayoutBlock> initialBlocks = mock(List.class);
////        ViewsOpenResponse viewsOpenResponse = mock(ViewsOpenResponse.class);
////
////        when(slackService.validateRequest(slackSignature, timestamp, requestBody)).thenReturn();
////        when(SelectCommand.selectCommandGroupAndCommandTypeBlocks()).thenReturn(initialBlocks);
////        when(slackService.findGlobalRequestModalView(initialBlocks)).thenReturn(viewsOpenResponse);
////
////        // Act
////        slackController.databaseRequestCommand(token, teamId, teamDomain, channelId, channelName, userId, userName, command, text, responseUrl, triggerId, requestBody, slackSignature, timestamp);
////
////        // Assert
////        verify(slackService, times(1)).validateRequest(slackSignature, timestamp, requestBody);
////        verify(SelectCommand, times(1)).selectCommandGroupAndCommandTypeBlocks();
////        verify(slackService, times(1)).findGlobalRequestModalView(initialBlocks);
////        verify(slackClient, times(1)).viewsOpen(any());
////    }
//}
