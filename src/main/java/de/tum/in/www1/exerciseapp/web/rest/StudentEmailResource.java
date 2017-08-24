package de.tum.in.www1.exerciseapp.web.rest;

import de.tum.in.www1.exerciseapp.domain.Participation;
import de.tum.in.www1.exerciseapp.domain.Result;
import de.tum.in.www1.exerciseapp.domain.User;
import de.tum.in.www1.exerciseapp.repository.ParticipationRepository;
import de.tum.in.www1.exerciseapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a temporari API to get the id of a student for the evaluation
 */

@RestController
public class StudentEmailResource {

    private final Logger log = LoggerFactory.getLogger(StudentEmailResource.class);

    @Inject
    private ParticipationRepository participationRepository;

    @Inject
    private UserRepository userRepository;

    private Result findLast(Set<Result> resultSet) {
        Result last = null;
        for (Result result : resultSet) {
            if (last == null)
                last = result;
            else if (result.getBuildCompletionDate().isAfter(last.getBuildCompletionDate()))
                last = result;
        }
        return last;
    }

    @RequestMapping(value = "/students.csv", method = RequestMethod.GET)
    public void allUsers(HttpServletResponse response) throws IOException {

        Map<Long, Long> resultMap = new HashMap<>();
        List<Participation> participations = participationRepository.findByExerciseId(4L);
        for (Participation participation : participations) {
            Result last = findLast(participation.getResults());
            if (last != null)
                resultMap.put(participation.getStudent().getId(), last.getScore());
        }

        StringBuilder builder = new StringBuilder("Name,Email,Result");
        for (User user : userRepository.findAll()) {

            Long res = resultMap.get(user.getId());

            if (res != null) {
                builder
                    .append("\n")
                    .append(user.getFirstName())
                    .append(" ")
                    .append(user.getLastName())
                    .append(',')
                    .append(user.getEmail())
                    .append(',')
                    .append(res.toString());
            }
        }

        response.setContentType("text/plain; charset=utf-8");
        response.getWriter().print(builder.toString());
    }


    @RequestMapping(value = "/student/{planKey}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)

    // public ResponseEntity<EmailResponse> getEmail(@RequestParam(value = "buildplan") String buildPlanId, @RequestParam(value="secret") String secret){
    public ResponseEntity<EmailResponse> getEmail(@PathVariable("planKey") String buildPlanId, @RequestParam(value = "secret") String secret) {

        if (secret == null || secret.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!secret.equals("superSec123578ik")) {
            return new ResponseEntity<EmailResponse>(HttpStatus.UNAUTHORIZED);
        }

        if (buildPlanId == null || buildPlanId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Participation participation = participationRepository.findOneByBuildPlanId(buildPlanId);
        if (participation == null) {
            return new ResponseEntity<EmailResponse>(HttpStatus.NOT_FOUND);
        }

        String email = participation.getStudent().getEmail();
        if (email == null || email.isEmpty()) {
            return new ResponseEntity<EmailResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<EmailResponse>(new EmailResponse(email), HttpStatus.OK);
    }


    public static class EmailResponse {

        public String email;

        public EmailResponse() {
        }

        public EmailResponse(String email) {
            this.email = email;
        }
    }

}
