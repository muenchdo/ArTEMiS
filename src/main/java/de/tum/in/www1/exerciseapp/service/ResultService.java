package de.tum.in.www1.exerciseapp.service;

import de.tum.in.www1.exerciseapp.domain.Participation;
import de.tum.in.www1.exerciseapp.domain.enumeration.ExerciseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by Josias Montag on 06.10.16.
 */
@Service
public class ResultService {

    private final Logger log = LoggerFactory.getLogger(ResultService.class);

    @Inject
    private ContinuousIntegrationService continuousIntegrationService;

    @Inject
    private LtiService ltiService;

    @Inject
    SimpMessageSendingOperations messagingTemplate;


    /**
     * Perform async operations after we were notified about new results.
     *
     * @param participation Participation for which a new build is available
     */
    @Async
    public void onResultNotified(Participation participation) {
        // fetches the new build result
        Object websocketPayload = continuousIntegrationService.onBuildCompleted(participation);
        // notify user via websocket
        messagingTemplate.convertAndSend("/topic/participation/" + participation.getId() + "/newResults", true);
        log.debug("Received new result for participationId = "+participation.getId()+" Exercise: "+participation.getExercise().getTitle() + ". Have sent this info to the client (WebSocket)");

        if (participation.getExercise().getExerciseType() == ExerciseType.UML_CLASS_DIAGRAM && websocketPayload != null){
            log.debug("Result for a "+ExerciseType.UML_CLASS_DIAGRAM+" participationId = "+participation.getId()+" Exercise: "+participation.getExercise().getTitle() + ". Have sent this result "+websocketPayload+" to the client (WebSocket)");

            messagingTemplate.convertAndSend("/topic/participation/" + participation.getId() + "/umlexercise/assessmentResults", websocketPayload);
        }
        // handles new results and sends them to LTI consumers
        ltiService.onNewBuildResult(participation);
    }

}
