package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.util.LinkedMultiValueMap;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.FileType;
import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseRepository;
import de.tum.in.www1.artemis.util.*;
import de.tum.in.www1.artemis.web.rest.dto.FileMove;
import de.tum.in.www1.artemis.web.rest.dto.RepositoryStatusDTO;

public class TestRepositoryResourceIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    private final String testRepoBaseUrl = "/api/test-repository/";

    @Autowired
    private DatabaseUtilService database;

    @Autowired
    private RequestUtilService request;

    @Autowired
    ProgrammingExerciseRepository programmingExerciseRepository;

    private Course course;

    private ProgrammingExercise programmingExercise;

    private String currentLocalFileName = "currentFileName";

    private String currentLocalFileContent = "testContent";

    private String currentLocalFolderName = "currentFolderName";

    private String newLocalFileName = "newFileName";

    private String newLocalFolderName = "newFolderName";

    LocalRepository testRepo = new LocalRepository();

    @BeforeEach
    public void setup() throws Exception {
        database.addUsers(0, 0, 1);
        course = database.addEmptyCourse();
        programmingExercise = ModelFactory.generateProgrammingExercise(ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(7), course);
        testRepo.configureRepos("testLocalRepo", "testOriginRepo");

        // add file to the repository folder
        Path filePath = Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName);
        var file = Files.createFile(filePath).toFile();
        // write content to the created file
        FileUtils.write(file, currentLocalFileContent);

        // add folder to the repository folder
        filePath = Paths.get(testRepo.localRepoFile + "/" + currentLocalFolderName);
        var folder = Files.createDirectory(filePath).toFile();

        var testRepoUrl = new GitUtilService.MockFileRepositoryUrl(testRepo.localRepoFile);
        programmingExercise.setTestRepositoryUrl(testRepoUrl.toString());
        doReturn(gitService.getRepositoryByLocalPath(testRepo.localRepoFile.toPath())).when(gitService).getOrCheckoutRepository(testRepoUrl.getURL(), true);
        doReturn(gitService.getRepositoryByLocalPath(testRepo.localRepoFile.toPath())).when(gitService).getOrCheckoutRepository(testRepoUrl.getURL(), false);
    }

    @AfterEach
    public void tearDown() throws IOException {
        database.resetDatabase();
        reset(gitService);
        testRepo.resetLocalRepo();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testGetFiles() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        var files = request.getMap(testRepoBaseUrl + programmingExercise.getId() + "/files", HttpStatus.OK, String.class, FileType.class);
        assertThat(files).isNotEmpty();

        // Check if all files exist
        for (String key : files.keySet()) {
            assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + key))).isTrue();
        }
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testGetFile() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("file", currentLocalFileName);
        var file = request.get(testRepoBaseUrl + programmingExercise.getId() + "/file", HttpStatus.OK, byte[].class, params);
        assertThat(file).isNotEmpty();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName))).isTrue();
        assertThat(new String(file)).isEqualTo(currentLocalFileContent);
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testCreateFile() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/newFile"))).isFalse();
        params.add("file", "newFile");
        request.postWithoutResponseBody(testRepoBaseUrl + programmingExercise.getId() + "/file", HttpStatus.OK, params);
        assertThat(Files.isRegularFile(Paths.get(testRepo.localRepoFile + "/newFile"))).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testCreateFolder() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/newFolder"))).isFalse();
        params.add("folder", "newFolder");
        request.postWithoutResponseBody(testRepoBaseUrl + programmingExercise.getId() + "/folder", HttpStatus.OK, params);
        assertThat(Files.isDirectory(Paths.get(testRepo.localRepoFile + "/newFolder"))).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testRenameFile() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName))).isTrue();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + newLocalFileName))).isFalse();
        FileMove fileMove = new FileMove();
        fileMove.setCurrentFilePath(currentLocalFileName);
        fileMove.setNewFilename(newLocalFileName);
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/rename-file", fileMove, HttpStatus.OK, null);
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName))).isFalse();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + newLocalFileName))).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testRenameFolder() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFolderName))).isTrue();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + newLocalFolderName))).isFalse();
        FileMove fileMove = new FileMove();
        fileMove.setCurrentFilePath(currentLocalFolderName);
        fileMove.setNewFilename(newLocalFolderName);
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/rename-file", fileMove, HttpStatus.OK, null);
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFolderName))).isFalse();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + newLocalFolderName))).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testDeleteFile() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName))).isTrue();
        params.add("file", currentLocalFileName);
        request.delete(testRepoBaseUrl + programmingExercise.getId() + "/file", HttpStatus.OK, params);
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + currentLocalFileName))).isFalse();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testCommitChanges() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        var receivedStatusBeforeCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusBeforeCommit.repositoryStatus.toString()).isEqualTo("UNCOMMITTED_CHANGES");
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/commit", null, HttpStatus.OK, null);
        var receivedStatusAfterCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusAfterCommit.repositoryStatus.toString()).isEqualTo("CLEAN");
        var testRepoCommits = testRepo.getAllLocalCommits();
        assertThat(testRepoCommits.size() == 1).isTrue();
        assertThat(database.getUserByLogin("instructor1").getName()).isEqualTo(testRepoCommits.get(0).getAuthorIdent().getName());
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testPullChanges() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        String fileName = "remoteFile";

        // Create a commit for the local and the remote repository
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/commit", null, HttpStatus.OK, null);
        var remote = gitService.getRepositoryByLocalPath(testRepo.originRepoFile.toPath());

        // Create file in the remote repository
        Path filePath = Paths.get(testRepo.originRepoFile + "/" + fileName);
        Files.createFile(filePath).toFile();

        // Check if the file exists in the remote repository and that it doesn't yet exists in the local repository
        assertThat(Files.exists(Paths.get(testRepo.originRepoFile + "/" + fileName))).isTrue();
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + fileName))).isFalse();

        // Stage all changes and make a second commit in the remote repository
        gitService.stageAllChanges(remote);
        testRepo.originGit.commit().setMessage("TestCommit").setAllowEmpty(true).setCommitter("testname", "test@email").call();

        // Checks if the current commit is not equal on the local and the remote repository
        assertThat(testRepo.getAllLocalCommits().get(0)).isNotEqualTo(testRepo.getAllOriginCommits().get(0));

        // Execute the Rest call
        request.get(testRepoBaseUrl + programmingExercise.getId() + "/pull", HttpStatus.OK, Void.class);

        // Check if the current commit is the same on the local and the remote repository and if the file exists on the local repository
        assertThat(testRepo.getAllLocalCommits().get(0)).isEqualTo(testRepo.getAllOriginCommits().get(0));
        assertThat(Files.exists(Paths.get(testRepo.localRepoFile + "/" + fileName))).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testResetToLastCommit() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        String fileName = "testFile";
        var localRepo = gitService.getRepositoryByLocalPath(testRepo.localRepoFile.toPath());
        var remoteRepo = gitService.getRepositoryByLocalPath(testRepo.originRepoFile.toPath());

        // Check status of git before the commit
        var receivedStatusBeforeCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusBeforeCommit.repositoryStatus.toString()).isEqualTo("UNCOMMITTED_CHANGES");

        // Create a commit for the local and the remote repository
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/commit", null, HttpStatus.OK, null);

        // Check status of git after the commit
        var receivedStatusAfterCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusAfterCommit.repositoryStatus.toString()).isEqualTo("CLEAN");

        // Create file in the local repository and commit it
        Path localFilePath = Paths.get(testRepo.localRepoFile + "/" + fileName);
        var localFile = Files.createFile(localFilePath).toFile();
        // write content to the created file
        FileUtils.write(localFile, "local");
        gitService.stageAllChanges(localRepo);
        testRepo.localGit.commit().setMessage("local").call();

        // Create file in the remote repository and commit it
        Path remoteFilePath = Paths.get(testRepo.originRepoFile + "/" + fileName);
        var remoteFile = Files.createFile(remoteFilePath).toFile();
        // write content to the created file
        FileUtils.write(remoteFile, "remote");
        gitService.stageAllChanges(remoteRepo);
        testRepo.originGit.commit().setMessage("remote").call();

        // Merge the two and a conflict will occur
        testRepo.localGit.fetch().setRemote("origin").call();
        List<Ref> refs = testRepo.localGit.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        var result = testRepo.localGit.merge().include(refs.get(0).getObjectId()).setStrategy(MergeStrategy.RESOLVE).call();
        var status = testRepo.localGit.status().call();
        assertThat(status.getConflicting().size() > 0).isTrue();
        assertThat(MergeResult.MergeStatus.CONFLICTING).isEqualTo(result.getMergeStatus());

        // Execute the reset Rest call
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/reset", null, HttpStatus.OK, null);

        // Check the git status after the reset
        status = testRepo.localGit.status().call();
        assertThat(status.getConflicting().size() == 0).isTrue();
        assertThat(testRepo.getAllLocalCommits().get(0)).isEqualTo(testRepo.getAllOriginCommits().get(0));
        var receivedStatusAfterReset = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusAfterReset.repositoryStatus.toString()).isEqualTo("CLEAN");
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void testGetStatus() throws Exception {
        programmingExerciseRepository.save(programmingExercise);
        var receivedStatusBeforeCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);

        // The current status is "uncommited changes", since we added files and folders, but we didn't commit yet
        assertThat(receivedStatusBeforeCommit.repositoryStatus.toString()).isEqualTo("UNCOMMITTED_CHANGES");

        // Perform a commit to check if the status changes
        request.postWithoutLocation(testRepoBaseUrl + programmingExercise.getId() + "/commit", null, HttpStatus.OK, null);

        // Check if the status of git is "clean" after the commit
        var receivedStatusAfterCommit = request.get(testRepoBaseUrl + programmingExercise.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusAfterCommit.repositoryStatus.toString()).isEqualTo("CLEAN");
    }
}
