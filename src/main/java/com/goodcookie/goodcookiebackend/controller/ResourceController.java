package com.goodcookie.goodcookiebackend.controller;

import com.goodcookie.goodcookiebackend.dto.DeleteJournalsRequest;
import com.goodcookie.goodcookiebackend.dto.GetJournalsResponse;
import com.goodcookie.goodcookiebackend.dto.JournalSaveRequest;
import com.goodcookie.goodcookiebackend.model.Phrase;
import com.goodcookie.goodcookiebackend.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for handling
 * requests that Get, update or delete
 * any of the resources provided in the
 * system.
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Returns a phrase from the internal list of phrases
     * @return A Phrase object from fetched from the internal list of phrases provided by the service
     */
    @GetMapping("/phrase")
    public Phrase getPhrase(){
       return this.resourceService.getPhrase();
    }

    /**
     * Returns a list of all journals that the given user has saved
     * @param username the username associated with the journals
     * @return ResponseEntity containing a list of journals from the result of getJournals operation by the service
     */
    @GetMapping("/getJournals/{username}")
    public ResponseEntity<List<GetJournalsResponse>> getJournals(@PathVariable("username") String username){
        return ResponseEntity.ok(this.resourceService.getJournals(username));
    }

    /**
     * Captures the save journal request and calls on
     *  the service to process the request.
     * @param journalSaveRequest the journal save request object containing the
     * base64 url of image file to save and the username associated with the journal to save
     * @return ResponseEntity indicating the result of saveJournal operation by the service
     */
    @PostMapping("/saveJournal")
    public ResponseEntity<String> saveJournal(@RequestBody JournalSaveRequest journalSaveRequest){
        return this.resourceService.saveJournal(journalSaveRequest);
    }

    /**
     * Captures a list of journal urls to delete
     * and calls on the service to delete the journals
     * @param deleteJournalsRequestList The list of journal urls to delete
     * @return ResponseEntity indicating the result of deleteJournalList operation by service
     */
    @DeleteMapping("/deleteJournalList")
    public ResponseEntity<String> deleteJournalList(@RequestBody List<DeleteJournalsRequest> deleteJournalsRequestList){
       return this.resourceService.deleteJournalList(deleteJournalsRequestList);
    }

}
