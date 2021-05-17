package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.dto.DeleteJournalsRequest;
import com.goodcookie.goodcookiebackend.dto.GetJournalsResponse;
import com.goodcookie.goodcookiebackend.dto.JournalSaveRequest;
import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.model.Journal;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.JournalRepository;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AmazonClient amazonClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JournalRepository journalRepository;


    //    private Deque<Phrase> phraseListMock;
    private ResourceService underTest;

    @BeforeEach
    void setUp() {

        underTest = new ResourceService(restTemplate,
                amazonClient, userRepository, journalRepository);

    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void canGetJournals() {

        System.out.println("canGetJournals Test: Begin");
        //given
        String username = "test";
        User user = User.builder().username(username).build();
        Journal j1 = Journal.builder()
                .journalId(1)
                .journalURL("http://testing.com")
                .user(user)
                .createdDate(Instant.now())
                .build();
        Journal j2 = Journal.builder()
                .journalId(2)
                .journalURL("http://testing2.com")
                .user(user)
                .createdDate(Instant.now())
                .build();
        List<Journal> journalList = new ArrayList<>();
        journalList.add(j1);
        journalList.add(j2);

        List<GetJournalsResponse> expectedRes = journalList.stream()
                .map(journal -> GetJournalsResponse.builder()
                        .journalUrl(journal.getJournalURL())
                        .journalCreatedDate(Date.from(journal.getCreatedDate()))
                        .build()).collect(Collectors.toList());

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(journalRepository.findAllByUserEquals(user)).willReturn(journalList);
        //when
        List<GetJournalsResponse> actual = underTest.getJournals(username);
        //then
        assertThat(actual).isEqualTo(expectedRes);

        System.out.println("canGetJournals Test: Pass");
    }

    @Test
    void shouldThrowWhenGettingJournalsWithUnknownUsername() {

        System.out.println("shouldThrowWhenGettingJournalsWithUnknownUsername Test: Begin");
        //given
        String username = "test";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        //when
        Throwable exception = Assertions.assertThrows(GoodCookieBackendException.class,
                () -> underTest.getJournals(username));

        //then
        Assertions.assertEquals("Error occurred while retrieving " +
                "user associated with journals", exception.getMessage());

        System.out.println("shouldThrowWhenGettingJournalsWithUnknownUsername Test: Pass");
    }


    @Test
    void canDeleteJournalList() {

        System.out.println("canDeleteJournalList Test: Begin");
        //given
        DeleteJournalsRequest d1 = DeleteJournalsRequest.builder()
                .journalUrl("http://testing.com")
                .journalCreatedDate(Instant.now())
                .build();
        DeleteJournalsRequest d2 = DeleteJournalsRequest.builder()
                .journalUrl("http://testing.com")
                .journalCreatedDate(Instant.now())
                .build();

        List<DeleteJournalsRequest> deleteJournalsRequestList = Arrays.asList(d1, d2);

        List<String> expectedUrlList = deleteJournalsRequestList.stream()
                .map(DeleteJournalsRequest::getJournalUrl)
                .collect(Collectors.toList());

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Journals successfully deleted");

        //when
        ResponseEntity<String> actualResponse = underTest.deleteJournalList(deleteJournalsRequestList);
        //then
        ArgumentCaptor<List<String>> urlListCaptor = ArgumentCaptor.forClass(List.class);

        ArgumentCaptor<List<String>> urlListAmazonCaptor = ArgumentCaptor.forClass(List.class);

        verify(journalRepository).deleteAllByJournalURLIsIn(urlListCaptor.capture());
        verify(amazonClient).deleteManyFilesFromS3Bucket(urlListAmazonCaptor.capture());

        List<String> capturedUrlList = urlListCaptor.getValue();
        List<String> capturedUrlListAmazon = urlListAmazonCaptor.getValue();

        assertThat(capturedUrlList).isEqualTo(expectedUrlList);
        assertThat(capturedUrlListAmazon).isEqualTo(expectedUrlList);
        assertThat(actualResponse).isEqualTo(expectedResponse);
        System.out.println("canDeleteJournalList Test: Pass");

    }

    @Test
    void shouldThrowWhenDeleteJournalListIsEmpty(){

        System.out.println("shouldThrowWhenDeleteJournalListIsEmpty Test: Begin");
        //given
        List<DeleteJournalsRequest> emptyRequestList = new ArrayList<>();

        String expectedMessage = "Malformed deleteJournalList " +
                "request: deleteJournalsRequestList is Empty";

        //when
        //then
        Throwable exception = Assertions.assertThrows(GoodCookieBackendException.class,
                () -> underTest.deleteJournalList(emptyRequestList));
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);

        System.out.println("shouldThrowWhenDeleteJournalListIsEmpty Test: Pass");
    }



    @Test
    void canSaveJournal() {

        System.out.println("canSaveJournal Test: Begin");
       //given
        var journalSaveRequest = JournalSaveRequest.builder()
                .username("test")
                .journalDataUrl("data:image/png;base64," + "iVBORw0KGgoAAAANSUhEUgAAAOEAAADhCAMAAAAJ" +
                        "bSJIAAAAgVBMVEX///8AAAD7+/vt7e3x8fG5ubmtra3j4+P4+PjV1dXn5+ednZ05O" +
                        "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" +
                        "eX8kyAN59lkOR0sytVtWlercjGYLifZPEyD+mXmfxD+pGaB1f6uAAAAAElFTkSuQmCC")
                .build();

        var expectedResponse =
                new ResponseEntity<>("Journal successfully saved", HttpStatus.CREATED);
        given(userRepository.findByUsername(journalSaveRequest.getUsername()))
                .willReturn(Optional.of(User.builder()
                .username(journalSaveRequest.getUsername()).build()));
        //when
        var actualResponse = underTest.saveJournal(journalSaveRequest);
        //then
        ArgumentCaptor<String> amazonUploadCallCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userRepoCallCaptor = ArgumentCaptor.forClass(String.class);

        verify(amazonClient).uploadFile(amazonUploadCallCaptor.capture(),
                amazonUploadCallCaptor.capture());
        verify(userRepository).findByUsername(userRepoCallCaptor.capture());


        assertThat(amazonUploadCallCaptor.getAllValues())
                .isEqualTo(Arrays.asList(journalSaveRequest.getJournalDataUrl(),
                        journalSaveRequest.getUsername()));
        assertThat(userRepoCallCaptor.getValue()).isEqualTo(journalSaveRequest.getUsername());

        assertThat(actualResponse).isEqualTo(expectedResponse);

        System.out.println("canSaveJournal Test: Pass");
    }

    @Test
    void shouldThrowWhenUnknownUserAttemptsSaveJournalRequest(){

        System.out.println("shouldThrowWhenUnknownUserAttemptsSaveJournalRequest Test: Begin");
        //given
        var malformedJournalSaveReq =
                JournalSaveRequest.builder().username("").journalDataUrl("").build();
        var expectedMessage = "Error occurred while fetching user associated with journal";
        //when
        when(userRepository.findByUsername(malformedJournalSaveReq.getUsername()))
                .thenReturn(Optional.empty());
        //then
        Throwable exception = Assertions.assertThrows(GoodCookieBackendException.class,
                () -> underTest.saveJournal(malformedJournalSaveReq));
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);

        System.out.println("shouldThrowWhenUnknownUserAttemptsSaveJournalRequest Test: Pass");
    }
}
