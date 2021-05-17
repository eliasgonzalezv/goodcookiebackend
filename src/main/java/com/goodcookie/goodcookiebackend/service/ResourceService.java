package com.goodcookie.goodcookiebackend.service;


import com.goodcookie.goodcookiebackend.dto.DeleteJournalsRequest;
import com.goodcookie.goodcookiebackend.dto.GetJournalsResponse;
import com.goodcookie.goodcookiebackend.dto.JournalSaveRequest;
import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.model.Journal;
import com.goodcookie.goodcookiebackend.model.Phrase;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.JournalRepository;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides the logic to fulfill all
 * Phrase and Journal requests from the client
 */
@Service
@Transactional
public class ResourceService {

   //RestTemplate provided by spring to consume phrases API
   private final RestTemplate restTemplate;
   //AmazonClient that provides CRUD operations on the S3 storage service
   private final AmazonClient amazonClient;
   //Needed for fetching user information from database
   private final UserRepository userRepository;
   //Needed to perform CRUD operation on journal table in DB
   private final JournalRepository journalRepository;
   //Internal data structure to hold a list of 1643 random phrases fetched from api
   //Needed to minimize the number of api calls to phrases API
   private Deque<Phrase> phraseList;

   @Autowired
   public ResourceService(RestTemplate restTemplate, AmazonClient amazonClient, UserRepository userRepository, JournalRepository journalRepository) {
      this.restTemplate = restTemplate;
      this.amazonClient = amazonClient;
      this.userRepository = userRepository;
      this.journalRepository = journalRepository;

   }

   @PostConstruct
   private void initPhrases(){
      //Preload the list of phrases
      this.phraseList = this.getPhrasesFromApi();
   }

   /**
    * Returns a Phrase from the list of phrases held internally
    * @return a Phrase object containing a quote and its author
    */
   public Phrase getPhrase(){
      if(this.phraseList.isEmpty()){
         //Populate phrase list by making api call
         //should happen only every 1643 requests
         this.phraseList.addAll(this.getPhrasesFromApi());
      }

      System.out.println(this.phraseList.size());
      //Return the a random phrase
      return this.phraseList.pop();
   }

   /**
    * Fetches a json array containing random phrases from the zenquotes api
    * @return An ArrayDeque containing 1643 random phrases to populate internal data structure
    */
//   @PostConstruct
   private Deque<Phrase> getPhrasesFromApi() {

      List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
      //Add the Jackson Message converter
      MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
      // Note: here we are making this converter to process any kind of response,
      // not only application/*json, which is the default behaviour
      converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
      messageConverters.add(converter);
      restTemplate.setMessageConverters(messageConverters);
      //url = "https://zenquotes.io/api/quotes"; originally used but api has problems returning text instead of json
      String url = "https://type.fit/api/quotes";
      //Fetch the phrases
      ResponseEntity<Deque<Phrase>> responseEntity = restTemplate.exchange(url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {
              });
      return responseEntity.getBody();
   }

   /**
    * Fetches and returns a list of journals that the given user has saved.
    * @param username the user associated with the journals to fetch
    * @return A list of all journals saved by the given user
    */
   public List<GetJournalsResponse> getJournals(String username){
      //Find the user information associated with username
      User user = userRepository.findByUsername(username).orElseThrow(
              ()->new GoodCookieBackendException("Error occurred " +
                      "while retrieving user associated with journals"));
      //Fetch journals related to user
      List<Journal> journalList = this.journalRepository.findAllByUserEquals(user);
      //Map journals to GetJournalsResponse objects
      List<GetJournalsResponse> journalsResponses = journalList.stream()
              .map(journal -> { return GetJournalsResponse.builder()
                        .journalUrl(journal.getJournalURL())
                        .journalCreatedDate(Date.from(journal.getCreatedDate()))
                        .build();})
              .collect(Collectors.toList());
     return journalsResponses;
   }


//   /**
//    * Deletes a journal from the DB and the Amazon S3 storage service
//    * @param journalUrl the url of the journal image to delete
//    * @return ResponseEntity denoting the successful deletion of the journal.
//    */
//   public ResponseEntity<String> deleteJournal(String journalUrl){
//      //Delete journal entry from Amazon s3
//       this.amazonClient.deleteFileFromS3Bucket(journalUrl);
//      //Delete journal entry from database
//       this.journalRepository.deleteByJournalURL(journalUrl);
//       return new ResponseEntity<>("Journal Successfully Deleted.", HttpStatus.OK);
//   }
/*
TODO: Now that deleteJournalList works well, in reality I could use
TODO: it as the way to delete 1 or more journals. Eliminating the need
TODO: for the single deleteJournal and deleteFileFromS3Bucket methods.
 */
   /**
    * Deletes 1 or more journals from the database and the S3 storage service
    * @param deleteJournalsRequestList List of objects containing the urls of the journals to delete
    * @return ResponseEntity denoting the successful deletion of the journals.
    */
   public ResponseEntity<String> deleteJournalList(List<DeleteJournalsRequest> deleteJournalsRequestList) {
      if(!deleteJournalsRequestList.isEmpty()) {
         //Extract all urls from DeleteJournalsRequestList
         List<String> journalUrlList = deleteJournalsRequestList.stream()
                 .map(DeleteJournalsRequest::getJournalUrl)
                 .collect(Collectors.toList());
         //Delete journals from database
         this.journalRepository.deleteAllByJournalURLIsIn(journalUrlList);
         //Delete journal from storage service
         this.amazonClient.deleteManyFilesFromS3Bucket(journalUrlList);

         return new ResponseEntity<>("Journals successfully deleted", HttpStatus.OK);
      }
      else{
         //Should never happen
         throw new GoodCookieBackendException("Malformed deleteJournalList " +
                 "request: deleteJournalsRequestList is Empty");
      }
   }

   /**
    * Uploads journal image file to Amazon S3 using an
    * Amazon client, and saves the journal information
    * into our database.
    * @param journalSaveRequest JournalSaveRequest object containing image file and username
    * @return ResponseEntity indicating the success of saving process.
    */
   public ResponseEntity<String> saveJournal(JournalSaveRequest journalSaveRequest){
      //Upload the journal image file obtained from controller
      String journalUrl = this.amazonClient.uploadFile(journalSaveRequest.getJournalDataUrl(),
              journalSaveRequest.getUsername());
      //Find user associated with journal
      User user = this.userRepository.findByUsername(journalSaveRequest.getUsername())
              .orElseThrow(() -> new GoodCookieBackendException("Error occurred while fetching" +
                              " user associated with journal"));
      //Save the journal url and user id into the journal table
      Journal journal = Journal.builder()
              .journalURL(journalUrl)
              .user(user)
              .createdDate(Instant.now())
              .build();
      this.journalRepository.save(journal);
      return new ResponseEntity<>("Journal successfully saved", HttpStatus.CREATED);
   }



}
