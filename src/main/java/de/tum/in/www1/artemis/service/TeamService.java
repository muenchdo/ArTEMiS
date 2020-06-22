package de.tum.in.www1.artemis.service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.Team;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.enumeration.TeamImportStrategyType;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseStudentParticipation;
import de.tum.in.www1.artemis.repository.TeamRepository;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.service.connectors.VersionControlService;
import de.tum.in.www1.artemis.service.dto.TeamSearchUserDTO;
import de.tum.in.www1.artemis.service.team.TeamImportStrategy;
import de.tum.in.www1.artemis.service.team.strategies.CreateOnlyStrategy;
import de.tum.in.www1.artemis.service.team.strategies.PurgeExistingStrategy;
import de.tum.in.www1.artemis.web.rest.errors.StudentsAlreadyAssignedException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    private final UserRepository userRepository;

    private final AuthorizationCheckService authCheckService;

    private final Optional<VersionControlService> versionControlService;

    private final ProgrammingExerciseParticipationService programmingExerciseParticipationService;

    private final ParticipationService participationService;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository, AuthorizationCheckService authCheckService,
            Optional<VersionControlService> versionControlService, ProgrammingExerciseParticipationService programmingExerciseParticipationService,
            ParticipationService participationService) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.authCheckService = authCheckService;
        this.versionControlService = versionControlService;
        this.programmingExerciseParticipationService = programmingExerciseParticipationService;
        this.participationService = participationService;
    }

    /**
     * Finds the team of a given user for an exercise
     *
     * @param exercise Exercise for which to find the team
     * @param user Student for which to find the team
     * @return found team (or empty if student has not been assigned to a team yet for the exercise)
     */
    public Optional<Team> findOneByExerciseAndUser(Exercise exercise, User user) {
        return teamRepository.findOneByExerciseIdAndUserId(exercise.getId(), user.getId());
    }

    /**
     * Finds latest team instance (i.e. the exercise team that was created last) that a given user belongs to in a course
     *
     * @param course Course for which to find the team
     * @param user Student for which to find the team
     * @return latest team instance
     */
    public Optional<Team> findLatestTeamByCourseAndUser(Course course, User user) {
        return teamRepository.findAllByCourseIdAndUserIdOrderByIdDesc(course.getId(), user.getId()).stream().findFirst();
    }

    /**
     * Returns all teams for an exercise (optionally filtered for a specific tutor who owns the teams)
     * @param exercise Exercise for which to return all teams
     * @param teamOwnerId Optional user id by which to filter teams on their owner
     * @return List of teams
     */
    public List<Team> findAllByExerciseIdWithEagerStudents(Exercise exercise, Long teamOwnerId) {
        if (teamOwnerId != null) {
            return teamRepository.findAllByExerciseIdAndTeamOwnerIdWithEagerStudents(exercise.getId(), teamOwnerId);
        }
        else {
            return teamRepository.findAllByExerciseIdWithEagerStudents(exercise.getId());
        }
    }

    /**
     * Returns whether the student is already assigned to a team for a given exercise
     *
     * @param exercise Exercise for which to check
     * @param user Student for which to check
     * @return boolean flag whether the student has been assigned already or not yet
     */
    public Boolean isAssignedToTeam(Exercise exercise, User user) {
        if (!exercise.isTeamMode()) {
            return null;
        }
        return teamRepository.findOneByExerciseIdAndUserId(exercise.getId(), user.getId()).isPresent();
    }

    /**
     * Search for users by login or name in course
     *
     * @param course Course in which to search students
     * @param exercise Exercise in which the student might be added to a team
     * @param loginOrName Login or name by which to search students
     * @return users whose login matched
     */
    public List<TeamSearchUserDTO> searchByLoginOrNameInCourseForExerciseTeam(Course course, Exercise exercise, String loginOrName) {
        List<User> users = userRepository.searchByLoginOrNameInGroup(course.getStudentGroupName(), loginOrName);
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<TeamSearchUserDTO> teamSearchUsers = users.stream().map(TeamSearchUserDTO::new).collect(Collectors.toList());

        // Get list of all students (with id of assigned team) that are already assigned to a team for the exercise
        List<long[]> userIdAndTeamIdPairs = teamRepository.findAssignedUserIdsWithTeamIdsByExerciseIdAndUserIds(exercise.getId(), userIds);

        // convert Set<[userId, teamId]> into Map<userId -> teamId>
        Map<Long, Long> userIdAndTeamIdMap = userIdAndTeamIdPairs.stream().collect(Collectors.toMap(userIdAndTeamIdPair -> userIdAndTeamIdPair[0], // userId
                userIdAndTeamIdPair -> userIdAndTeamIdPair[1] // teamId
        ));

        // Annotate to which team the user is already assigned to for the given exercise (null if not assigned)
        teamSearchUsers.forEach(user -> user.setAssignedTeamId(userIdAndTeamIdMap.get(user.getId())));

        return teamSearchUsers;
    }

    /**
     * Update the members of a team repository if a participation exists already. Users might need to be removed or added.
     *
     * @param exerciseId Id of the exercise to which the team belongs
     * @param existingTeam Old team before update
     * @param updatedTeam New team after update
     */
    public void updateRepositoryMembersIfNeeded(Long exerciseId, Team existingTeam, Team updatedTeam) {
        Optional<ProgrammingExerciseStudentParticipation> optionalParticipation = this.programmingExerciseParticipationService.findByExerciseIdAndTeamId(exerciseId,
                existingTeam.getId());

        optionalParticipation.ifPresent(participation -> {
            // Users in the existing team that are no longer in the updated team need to be removed
            Set<User> usersToRemove = new HashSet<>(existingTeam.getStudents());
            usersToRemove.removeAll(updatedTeam.getStudents());
            usersToRemove.forEach(user -> versionControlService.get().removeMemberFromRepository(participation.getRepositoryUrlAsUrl(), user));

            // Users in the updated team that were not yet part of the existing team need to be added
            Set<User> usersToAdd = new HashSet<>(updatedTeam.getStudents());
            usersToAdd.removeAll(existingTeam.getStudents());
            usersToAdd.forEach(user -> versionControlService.get().addMemberToRepository(participation.getRepositoryUrlAsUrl(), user));
        });
    }

    /**
     * Saves a team to the database (and verifies before that none of the students is already assigned to another team)
     *
     * @param exercise Exercise which the team belongs to
     * @param team Team to be saved
     * @return saved Team
     */
    public Team save(Exercise exercise, Team team) {
        // verify that students are not assigned yet to another team for this exercise
        List<Pair<User, Team>> conflicts = findStudentTeamConflicts(exercise, team);
        if (!conflicts.isEmpty()) {
            throw new StudentsAlreadyAssignedException(conflicts);
        }
        // audit information is normally updated automatically but since changes in the many-to-many relationships are not registered,
        // we need to trigger the audit explicitly by modifying a column of the team entity itself
        if (team.getId() != null) {
            team.setLastModifiedDate(Instant.now());
        }
        team.setExercise(exercise);
        return teamRepository.save(team);
    }

    /**
     * Checks for each student in the given team whether they already belong to a different team
     *
     * @param exercise Exercise which the team belongs to
     * @param team Team whose students should be checked for conflicts with other teams
     * @return list of conflict pairs <student, team> where team is a different team than in the argument
     */
    private List<Pair<User, Team>> findStudentTeamConflicts(Exercise exercise, Team team) {
        List<Pair<User, Team>> conflicts = new ArrayList<Pair<User, Team>>();
        team.getStudents().forEach(student -> {
            Optional<Team> assignedTeam = teamRepository.findOneByExerciseIdAndUserId(exercise.getId(), student.getId());
            if (assignedTeam.isPresent() && !assignedTeam.get().equals(team)) {
                conflicts.add(Pair.of(student, assignedTeam.get()));
            }
        });
        return conflicts;
    }

    /**
     * Imports the teams from the source exercise into destination exercise using the given strategy
     *
     * @param sourceExercise Exercise from which to copy the existing teams
     * @param destinationExercise Exercise in which to copy the teams from source exercise
     * @param importStrategyType Type of strategy used to import teams (relevant for conflicts)
     * @return list of all teams that are now in the destination exercise
     */
    public List<Team> importTeamsFromSourceExerciseIntoDestinationExerciseUsingStrategy(Exercise sourceExercise, Exercise destinationExercise,
            TeamImportStrategyType importStrategyType) {
        TeamImportStrategy teamImportStrategy = getTeamImportStrategy(importStrategyType);
        teamImportStrategy.importTeams(sourceExercise, destinationExercise);
        return teamRepository.findAllByExerciseId(destinationExercise.getId());
    }

    /**
     * Returns an instance of TeamImportStrategy based on the given import strategy type (enum)
     *
     * @param importStrategyType Type for which to instantiate a strategy
     * @return TeamImportStrategy
     */
    private TeamImportStrategy getTeamImportStrategy(TeamImportStrategyType importStrategyType) {
        switch (importStrategyType) {
            case PURGE_EXISTING:
                return new PurgeExistingStrategy(teamRepository, participationService);
            case CREATE_ONLY:
                return new CreateOnlyStrategy(teamRepository);
            default:
                throw new Error("Unknown team import strategy type: " + importStrategyType);
        }
    }
}
