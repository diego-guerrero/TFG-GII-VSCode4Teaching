package com.vscode4teaching.vscode4teachingserver.servicetests;

import com.vscode4teaching.vscode4teachingserver.model.*;
import com.vscode4teaching.vscode4teachingserver.model.repositories.ExerciseFileRepository;
import com.vscode4teaching.vscode4teachingserver.model.repositories.ExerciseRepository;
import com.vscode4teaching.vscode4teachingserver.model.repositories.ExerciseUserInfoRepository;
import com.vscode4teaching.vscode4teachingserver.model.repositories.UserRepository;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.*;
import com.vscode4teaching.vscode4teachingserver.servicesimpl.ExerciseFilesServiceImpl;
import com.vscode4teaching.vscode4teachingserver.servicesimpl.JWTUserDetailsService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ExerciseFilesServiceImplTests {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseFilesServiceImplTests.class);
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExerciseFileRepository fileRepository;
    @Mock
    private ExerciseUserInfoRepository exerciseUserInfoRepository;
    @InjectMocks
    private ExerciseFilesServiceImpl filesService;
    @Mock
    private JWTUserDetailsService userService;
    private Set<String> pathsSaved = new HashSet<>();

    @BeforeEach
    public void startup() {
        this.pathsSaved = new HashSet<>();
    }

    @AfterEach
    public void cleanup() {
        try {
            FileUtils.deleteDirectory(Paths.get("null/").toFile());
            FileUtils.deleteDirectory(Paths.get("v4t-course-test/").toFile());
            FileUtils.deleteDirectory(Paths.get("test-uploads/").toFile());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void existsExerciseFilesForUser() throws NotInCourseException, ExerciseNotFoundException {
        Course course = new Course("Spring Boot Course");
        course.setId(1L);
        Exercise exercise = new Exercise("Spring Boot Exercise 1");
        Long exerciseId = 2L;
        exercise.setId(exerciseId);
        exercise.setCourse(course);
        course.addExercise(exercise);
        User user = new User("johndoejr@gmail.com", "johndoejr", "studentpassword", "John", "Doe Jr");
        user.addCourse(course);
        course.addUserInCourse(user);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        Boolean actualExists = filesService.existsExerciseFilesForUser(exerciseId, user.getUsername());

        assertThat(actualExists).isFalse();


        // Now files are added
        ExerciseFile file1 = new ExerciseFile("file1", user);
        ExerciseFile file2 = new ExerciseFile("file2", user);
        ExerciseFile file3 = new ExerciseFile("file3", user);
        List<ExerciseFile> expectedExerciseFiles = List.of(file1, file2, file3);
        exercise.setUserFiles(expectedExerciseFiles);

        Boolean actualExists2 = filesService.existsExerciseFilesForUser(exerciseId, user.getUsername());

        assertThat(actualExists2).isTrue();
    }

    @Test
    public void getExerciseFiles_withTemplate() throws Exception {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile file1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        ExerciseFile file2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        exercise.addFileToTemplate(file1);
        exercise.addFileToTemplate(file2);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        Map<Exercise, List<File>> filesMap = filesService.getExerciseFiles(1L, "johndoe");
        List<File> files = filesMap.values().stream().findFirst().get();

        assertThat(files.size()).isEqualTo(2);
        assertThat(files.get(0).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        assertThat(files.get(1).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getExerciseSolution() throws Exception {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile file1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/solution/ej1.txt");
        ExerciseFile file2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/solution/ej2.txt");
        exercise.addFileToSolution(file1);
        exercise.addFileToSolution(file2);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        Map<Exercise, List<File>> filesMap = filesService.getExerciseSolution(1L, "johndoe");
        List<File> files = filesMap.values().stream().findFirst().get();

        assertThat(files.size()).isEqualTo(2);
        assertThat(files.get(0).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/solution/ej1.txt");
        assertThat(files.get(1).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/solution/ej2.txt");
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getExerciseFiles_withUserFiles() throws Exception {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile file1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        ExerciseFile file2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        exercise.addFileToTemplate(file1);
        exercise.addFileToTemplate(file2);
        ExerciseFile file3 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej1.txt");
        ExerciseFile file4 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej2.txt");
        file3.setOwner(student);
        file4.setOwner(student);
        exercise.addUserFile(file3);
        exercise.addUserFile(file4);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        Map<Exercise, List<File>> filesMap = filesService.getExerciseFiles(1L, "johndoe");
        List<File> files = filesMap.values().stream().findFirst().get();

        logger.info(files.get(0).getAbsolutePath());
        logger.info(files.get(1).getAbsolutePath());
        assertThat(files.size()).isEqualTo(2);
        assertThat(files.get(0).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej1.txt");
        assertThat(files.get(1).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej2.txt");
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getExerciseFiles_noTemplate() {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        assertThrows(NoTemplateException.class, () -> filesService.getExerciseFiles(1L, "johndoe"));
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    private List<File> runSaveExerciseFiles() throws Exception {
        // Get files
        File file = Paths.get("src/test/java/com/vscode4teaching/vscode4teachingserver/files", "exs.zip").toFile();
        MultipartFile mockFile = new MockMultipartFile("file", file.getName(), "application/zip",
                new FileInputStream(file));

        Map<Exercise, List<File>> filesMap = filesService.saveExerciseFiles(1L, mockFile, "johndoe");
        return filesMap.values().stream().findFirst().get();
    }

    private boolean checkPathHasBeenSaved(String path) {
        return this.pathsSaved.add(path);
    }

    private void fileAsserts(ExerciseUserInfo eui) throws IOException {
        assertThat(Files.exists(Paths.get("null/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId()))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex1.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex2.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex3/ex3.html"))).isTrue();
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex1.html")))
                .contains("<html>Exercise 1</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex2.html")))
                .contains("<html>Exercise 2</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex3/ex3.html")))
                .contains("<html>Exercise 3</html>");
    }

    private void exerciseUserInfoAsserts(ExerciseUserInfo eui) {
        Exercise exercise = eui.getExercise();
        User student = eui.getUser();
        assertThat(exercise.getUserFiles()).hasSize(3);
        assertThat(exercise.getUserFiles().get(0).getOwner()).isEqualTo(student);
        assertThat(exercise.getUserFiles().get(1).getOwner()).isEqualTo(student);
        assertThat(exercise.getUserFiles().get(2).getOwner()).isEqualTo(student);
        assertThat(exercise.getUserFiles().get(0).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex1.html").toAbsolutePath().toString());
        assertThat(exercise.getUserFiles().get(1).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex2.html").toAbsolutePath().toString());
        assertThat(exercise.getUserFiles().get(2).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/student_" + eui.getId() + "/ex3/ex3.html").toAbsolutePath().toString());
    }

    private ExerciseUserInfo setupSaveExerciseFiles() {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseUserInfo eui = new ExerciseUserInfo(exercise, student);
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exercise));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(student));
        when(fileRepository.findByPath(anyString())).thenAnswer(I -> {
            String path = (String) I.getArguments()[0];
            if (this.checkPathHasBeenSaved(path)) {
                return Optional.empty();
            } else {
                return Optional.of(new ExerciseFile(path));
            }
        });
        when(fileRepository.save(any(ExerciseFile.class))).then(returnsFirstArg());
        when(exerciseRepository.save(any(Exercise.class))).then(returnsFirstArg());
        when(exerciseUserInfoRepository.findByExercise_IdAndUser_Username(anyLong(), anyString()))
                .thenReturn(Optional.of(eui));
        return eui;
    }

    @Test
    public void saveExerciseFiles() throws Exception {
        ExerciseUserInfo eui = this.setupSaveExerciseFiles();
        Exercise exercise = eui.getExercise();

        List<File> savedFiles = this.runSaveExerciseFiles();

        verify(exerciseRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(fileRepository, times(3)).findByPath(anyString());
        verify(fileRepository, times(3)).save(any(ExerciseFile.class));
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
        verify(exerciseUserInfoRepository, times(1)).findByExercise_IdAndUser_Username(anyLong(), anyString());
        this.fileAsserts(eui);
        this.exerciseUserInfoAsserts(eui);
        assertThat(savedFiles.size()).isEqualTo(3);
        assertThat(savedFiles.get(0).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(0).getPath());
        assertThat(savedFiles.get(1).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(1).getPath());
        assertThat(savedFiles.get(2).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(2).getPath());
    }

    @Test
    public void saveExerciseFilesIgnoreDuplicates() throws Exception {
        ExerciseUserInfo eui = this.setupSaveExerciseFiles();
        Exercise exercise = eui.getExercise();

        // Run twice to send duplicates
        this.runSaveExerciseFiles();
        List<File> savedFiles = this.runSaveExerciseFiles();

        verify(exerciseRepository, times(2)).findById(anyLong());
        verify(userRepository, times(2)).findByUsername(anyString());
        verify(fileRepository, times(6)).findByPath(anyString());
        verify(fileRepository, times(3)).save(any(ExerciseFile.class));
        verify(exerciseRepository, times(2)).save(any(Exercise.class));
        verify(exerciseUserInfoRepository, times(2)).findByExercise_IdAndUser_Username(anyLong(), anyString());
        this.fileAsserts(eui);
        this.exerciseUserInfoAsserts(eui);
        assertThat(savedFiles.get(0).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(0).getPath());
        assertThat(savedFiles.get(1).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(1).getPath());
        assertThat(savedFiles.get(2).getAbsolutePath()).isEqualToIgnoringCase(exercise.getUserFiles().get(2).getPath());
    }

    @Test
    public void saveExerciseFilesFinishedError() throws Exception {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseUserInfo eui = new ExerciseUserInfo(exercise, student);
        eui.setStatus(ExerciseStatus.FINISHED);
        when(exerciseUserInfoRepository.findByExercise_IdAndUser_Username(anyLong(), anyString()))
                .thenReturn(Optional.of(eui));
        // Get files
        File file = Paths.get("src/test/java/com/vscode4teaching/vscode4teachingserver/files", "exs.zip").toFile();
        MultipartFile mockFile = new MockMultipartFile("file", file.getName(), "application/zip",
                new FileInputStream(file));

        ExerciseFinishedException e = assertThrows(ExerciseFinishedException.class,
                () -> filesService.saveExerciseFiles(1L, mockFile, "johndoe"));

        assertThat(e.getMessage()).isEqualToIgnoringWhitespace("Exercise is marked as finished: 1");
        verify(exerciseUserInfoRepository, times(1)).findByExercise_IdAndUser_Username(anyLong(), anyString());
    }

    @Test
    public void saveExerciseTemplate() throws Exception {
        User teacher = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        teacher.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        Role teacherRole = new Role("ROLE_TEACHER");
        studentRole.setId(10L);
        teacher.addRole(studentRole);
        teacher.addRole(teacherRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(teacher);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exercise));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(teacher));
        when(fileRepository.save(any(ExerciseFile.class))).then(returnsFirstArg());
        when(exerciseRepository.save(any(Exercise.class))).then(returnsFirstArg());
        // Get files
        File file = Paths.get("src/test/java/com/vscode4teaching/vscode4teachingserver/files", "exs.zip").toFile();
        MultipartFile mockFile = new MockMultipartFile("file", file.getName(), "application/zip",
                new FileInputStream(file));

        Map<Exercise, List<File>> filesMap = filesService.saveExerciseTemplate(1L, mockFile, "johndoe");
        List<File> savedFiles = filesMap.values().stream().findFirst().get();

        assertThat(Files.exists(Paths.get("null/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/template"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex1.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex2.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex3/ex3.html"))).isTrue();
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex1.html")))
                .contains("<html>Exercise 1</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex2.html")))
                .contains("<html>Exercise 2</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex3/ex3.html")))
                .contains("<html>Exercise 3</html>");
        assertThat(exercise.getTemplate()).hasSize(3);
        assertThat(exercise.getTemplate().get(0).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex1.html").toAbsolutePath().toString());
        assertThat(exercise.getTemplate().get(1).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex2.html").toAbsolutePath().toString());
        assertThat(exercise.getTemplate().get(2).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/template/ex3/ex3.html").toAbsolutePath().toString());
        assertThat(savedFiles.size()).isEqualTo(3);
        assertThat(savedFiles.get(0).getAbsolutePath()).isEqualToIgnoringCase(exercise.getTemplate().get(0).getPath());
        assertThat(savedFiles.get(1).getAbsolutePath()).isEqualToIgnoringCase(exercise.getTemplate().get(1).getPath());
        assertThat(savedFiles.get(2).getAbsolutePath()).isEqualToIgnoringCase(exercise.getTemplate().get(2).getPath());
        verify(exerciseRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(fileRepository, times(3)).save(any(ExerciseFile.class));
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

    @Test
    public void saveExerciseSolution() throws Exception {
        User teacher = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        teacher.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        Role teacherRole = new Role("ROLE_TEACHER");
        studentRole.setId(10L);
        teacher.addRole(studentRole);
        teacher.addRole(teacherRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(teacher);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exercise));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(teacher));
        when(fileRepository.save(any(ExerciseFile.class))).then(returnsFirstArg());
        when(exerciseRepository.save(any(Exercise.class))).then(returnsFirstArg());
        // Get files
        File file = Paths.get("src/test/java/com/vscode4teaching/vscode4teachingserver/files", "exs.zip").toFile();
        MultipartFile mockFile = new MockMultipartFile("file", file.getName(), "application/zip",
                new FileInputStream(file));

        Map<Exercise, List<File>> filesMap = filesService.saveExerciseSolution(1L, mockFile, "johndoe");
        List<File> savedFiles = filesMap.values().stream().findFirst().get();

        assertThat(Files.exists(Paths.get("null/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/solution"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex1.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex2.html"))).isTrue();
        assertThat(Files.exists(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex3/ex3.html"))).isTrue();
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex1.html")))
                .contains("<html>Exercise 1</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex2.html")))
                .contains("<html>Exercise 2</html>");
        assertThat(Files.readAllLines(Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex3/ex3.html")))
                .contains("<html>Exercise 3</html>");
        assertThat(exercise.getSolution()).hasSize(3);
        assertThat(exercise.getSolution().get(0).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex1.html").toAbsolutePath().toString());
        assertThat(exercise.getSolution().get(1).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex2.html").toAbsolutePath().toString());
        assertThat(exercise.getSolution().get(2).getPath()).isEqualToIgnoringCase(
                Paths.get("null/spring_boot_course_4/exercise_1_1/solution/ex3/ex3.html").toAbsolutePath().toString());
        assertThat(savedFiles.size()).isEqualTo(3);
        assertThat(savedFiles.get(0).getAbsolutePath()).isEqualToIgnoringCase(exercise.getSolution().get(0).getPath());
        assertThat(savedFiles.get(1).getAbsolutePath()).isEqualToIgnoringCase(exercise.getSolution().get(1).getPath());
        assertThat(savedFiles.get(2).getAbsolutePath()).isEqualToIgnoringCase(exercise.getSolution().get(2).getPath());
        verify(exerciseRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(fileRepository, times(3)).save(any(ExerciseFile.class));
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

    @Test
    public void getTemplate() throws Exception {
        User student = new User("johndoejr@gmail.com", "johndoe", "pass", "John", "Doe");
        student.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        student.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(student);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile file1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        ExerciseFile file2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        exercise.addFileToTemplate(file1);
        exercise.addFileToTemplate(file2);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        Map<Exercise, List<File>> filesMap = filesService.getExerciseTemplate(1L, "johndoe");
        List<File> files = filesMap.values().stream().findFirst().get();

        assertThat(files.size()).isEqualTo(2);
        assertThat(files.get(0).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        assertThat(files.get(1).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getAllStudentExercises() throws ExerciseNotFoundException, NotInCourseException {
        User teacher = new User("johndoe@gmail.com", "johndoe", "pass", "John", "Doe");
        teacher.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        Role teacherRole = new Role("ROLE_TEACHER");
        teacherRole.setId(10L);
        teacher.addRole(studentRole);
        teacher.addRole(teacherRole);
        User student1 = new User("johndoejr1@gmail.com", "johndoejr1", "pass", "John", "Doe Jr 1");
        student1.setId(11L);
        student1.addRole(studentRole);
        User student2 = new User("johndoejr2@gmail.com", "johndoejr2", "pass", "John", "Doe Jr 2");
        student2.setId(12L);
        student2.addRole(studentRole);
        User student3 = new User("johndoejr3@gmail.com", "johndoejr3", "pass", "John", "Doe Jr 3");
        student3.setId(13L);
        student3.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(teacher);
        course.addUserInCourse(student1);
        course.addUserInCourse(student2);
        course.addUserInCourse(student3);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile file1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej1.txt");
        ExerciseFile file2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/template/ej2.txt");
        exercise.addFileToTemplate(file1);
        exercise.addFileToTemplate(file2);
        ExerciseFile teacherFile1 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/student_10/ej1.txt");
        ExerciseFile teacherFile2 = new ExerciseFile("v4t-course-test/spring-boot-course/exercise_1_1/student_10/ej2.txt");
        teacherFile1.setOwner(teacher);
        teacherFile2.setOwner(teacher);
        exercise.addUserFile(teacherFile1);
        exercise.addUserFile(teacherFile2);
        ExerciseFile student1File1 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_11/ej1.txt");
        ExerciseFile student1File2 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_11/ej2.txt");
        student1File1.setOwner(student1);
        student1File2.setOwner(student1);
        exercise.addUserFile(student1File1);
        exercise.addUserFile(student1File2);
        ExerciseFile student2File1 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej1.txt");
        ExerciseFile student2File2 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej2.txt");
        student2File1.setOwner(student2);
        student2File2.setOwner(student2);
        exercise.addUserFile(student2File1);
        exercise.addUserFile(student2File2);
        ExerciseFile student3File1 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_13/ej1.txt");
        ExerciseFile student3File2 = new ExerciseFile(
                "v4t-course-test/spring-boot-course/exercise_1_1/student_13/ej2.txt");
        student3File1.setOwner(student3);
        student3File2.setOwner(student3);
        exercise.addUserFile(student3File1);
        exercise.addUserFile(student3File2);
        Optional<Exercise> exOpt = Optional.of(exercise);
        when(exerciseRepository.findById(anyLong())).thenReturn(exOpt);

        Map<Exercise, List<File>> filesMap = filesService.getAllStudentsFiles(1L, "johndoe");
        List<File> files = filesMap.values().stream().findFirst().get();

        assertThat(files.size()).isEqualTo(6);
        assertThat(files.get(0).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_11/ej1.txt");
        assertThat(files.get(1).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_11/ej2.txt");
        assertThat(files.get(2).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej1.txt");
        assertThat(files.get(3).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_12/ej2.txt");
        assertThat(files.get(4).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_13/ej1.txt");
        assertThat(files.get(5).getPath().replace("\\", "/"))
                .isEqualTo("v4t-course-test/spring-boot-course/exercise_1_1/student_13/ej2.txt");
        verify(exerciseRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getFileIds() throws NotFoundException {
        User teacher = new User("johndoe@gmail.com", "johndoe", "pass", "John", "Doe");
        teacher.setId(3L);
        Role studentRole = new Role("ROLE_STUDENT");
        studentRole.setId(2L);
        Role teacherRole = new Role("ROLE_TEACHER");
        teacherRole.setId(10L);
        teacher.addRole(studentRole);
        teacher.addRole(teacherRole);
        User student1 = new User("johndoejr1@gmail.com", "johndoejr1", "pass", "John", "Doe Jr 1");
        student1.setId(11L);
        student1.addRole(studentRole);
        User student2 = new User("johndoejr2@gmail.com", "johndoejr2", "pass", "John", "Doe Jr 2");
        student2.setId(12L);
        student2.addRole(studentRole);
        User student3 = new User("johndoejr3@gmail.com", "johndoejr3", "pass", "John", "Doe Jr 3");
        student3.setId(13L);
        student3.addRole(studentRole);
        Course course = new Course("Spring Boot Course");
        course.setId(4L);
        course.addUserInCourse(teacher);
        course.addUserInCourse(student1);
        course.addUserInCourse(student2);
        course.addUserInCourse(student3);
        Exercise exercise = new Exercise();
        exercise.setName("Exercise 1");
        exercise.setId(1L);
        course.addExercise(exercise);
        exercise.setCourse(course);
        ExerciseFile ex1 = new ExerciseFile("student_11" + File.separator + "test1");
        ex1.setId(101L);
        ex1.setOwner(student1);
        ExerciseFile ex2 = new ExerciseFile("student_12" + File.separator + "test2");
        ex2.setId(102L);
        ex2.setOwner(student2);
        ExerciseFile ex3 = new ExerciseFile("student_13" + File.separator + "test3");
        ex3.setId(103L);
        ex3.setOwner(student3);
        exercise.addUserFile(ex1);
        exercise.addUserFile(ex2);
        exercise.addUserFile(ex3);
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exercise));
        when(userService.findByUsername("johndoejr1")).thenReturn(student1);

        List<ExerciseFile> files = filesService.getFileIdsByExerciseAndId(1L, "johndoejr1");

        verify(exerciseRepository, times(1)).findById(anyLong());
        assertThat(files.size()).isEqualTo(1);
        assertThat(files.get(0).getId()).isEqualTo(101L);
        assertThat(files.get(0).getPath()).isEqualTo("test1");
    }
}