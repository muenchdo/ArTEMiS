package de.tum.in.www1.artemis.service.team.strategies;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.Team;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.TeamRepository;
import de.tum.in.www1.artemis.service.team.TeamImportStrategy;

public class CreateOnlyStrategy extends TeamImportStrategy {

    public CreateOnlyStrategy(TeamRepository teamRepository) {
        super(teamRepository);
    }

    @Override
    public void importTeams(Exercise sourceExercise, Exercise destinationExercise) {
        // Filter the source teams and only clone the conflict-free teams into the destination exercise
        List<Team> conflictFreeSourceTeams = getConflictFreeSourceTeams(sourceExercise, destinationExercise);
        cloneTeamsIntoDestinationExercise(conflictFreeSourceTeams, destinationExercise);
    }

    /**
     * Filters the teams from the given source exercise and returns only those that can be imported into the destination exercise without conflicts
     *
     * Conditions for being conflict-free:
     * 1. No clash in team short name
     * 2. No overlapping students
     *
     * @param sourceExercise Exercise from which to take the teams for the import
     * @param destinationExercise Exercise in which to import the teams into
     * @return list of those source teams that have no conflicts
     */
    private List<Team> getConflictFreeSourceTeams(Exercise sourceExercise, Exercise destinationExercise) {
        // Get all teams from the source exercise and from the destination exercise
        List<Team> sourceTeams = teamRepository.findAllByExerciseId(sourceExercise.getId());
        List<Team> destinationTeams = teamRepository.findAllByExerciseId(destinationExercise.getId());

        // Compute sets of existing team short names and of students who are already part of teams in destination exercise
        Set<String> existingTeamShortNames = destinationTeams.stream().map(Team::getShortName).collect(Collectors.toSet());
        Set<User> existingTeamStudents = destinationTeams.stream().flatMap(team -> team.getStudents().stream()).collect(Collectors.toSet());

        // Filter for conflict-free source teams (1. no short name conflict, 2. no student overlap)
        Stream<Team> conflictFreeSourceTeams = sourceTeams.stream().filter(sourceTeam -> {
            final boolean noShortNameConflict = !existingTeamShortNames.contains(sourceTeam.getShortName());
            final boolean noStudentConflict = Collections.disjoint(existingTeamStudents, sourceTeam.getStudents());
            return noShortNameConflict && noStudentConflict;
        });

        return conflictFreeSourceTeams.collect(Collectors.toList());
    }
}
