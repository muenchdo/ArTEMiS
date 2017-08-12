package de.tum.in.www1.exerciseapp.web.rest;

import de.tum.in.www1.exerciseapp.domain.Participation;
import de.tum.in.www1.exerciseapp.repository.ParticipationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * This is a temporari API to get the id of a student for the evaluation
 */

@RestController
@RequestMapping({"/api", "/api_basic"})
public class StudentEmailResource {

    private final Logger log = LoggerFactory.getLogger(StudentEmailResource.class);

    @Inject
    private ParticipationRepository participationRepository;




    @RequestMapping(value = "/student/email",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<EmailResponse> getEmail(@RequestParam("buildplan") String buildPlanId, @RequestParam(value="secret") String secret){

        if (secret == null || secret.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!secret.equals("superSec123578ik")){
            return new ResponseEntity<EmailResponse>(HttpStatus.UNAUTHORIZED);
        }


        if (buildPlanId == null || buildPlanId.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Participation participantion = participationRepository.findOneByBuildPlanId(buildPlanId);
        if (participantion == null){
            return new ResponseEntity<EmailResponse>(HttpStatus.NOT_FOUND);
        }

        String email = participantion.getStudent().getEmail();
        if (email == null || email.isEmpty()){
            return new ResponseEntity<EmailResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<EmailResponse>(new EmailResponse(email), HttpStatus.OK);
    }


    public static class EmailResponse{

        public String email;

        public EmailResponse() {
        }

        public EmailResponse(String email) {
            this.email = email;
        }
    }
}
