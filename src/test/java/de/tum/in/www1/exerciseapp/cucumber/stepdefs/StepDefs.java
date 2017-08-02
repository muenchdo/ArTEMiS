package de.tum.in.www1.exerciseapp.cucumber.stepdefs;

import de.tum.in.www1.exerciseapp.ExerciseApplicationApp;

import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

@WebAppConfiguration
@ContextConfiguration(classes = ExerciseApplicationApp.class, loader = SpringBootContextLoader.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
