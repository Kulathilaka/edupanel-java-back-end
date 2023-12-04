package lk.ijse.dep11.edupanel.api;

import lk.ijse.dep11.edupanel.to.request.LecturerReqTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import javax.validation.Valid;
import java.sql.*;

@RestController
@RequestMapping("/api/v1/lecturers")
@CrossOrigin
public class LecturerHttpController {

    @Autowired
    private DataSource pool;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    public void createNewLecturer(@ModelAttribute @Valid LecturerReqTO lecturer){
        try(Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);

            try{

                    PreparedStatement stmInsertLecturer = connection
                    .prepareStatement("INSERT INTO  lecturer" +
                            "(name, designation, qualifications, linkedin)" +
                            "VALUES (?, ?, ?, ?)");
            stmInsertLecturer.setString(1, lecturer.getName());
            stmInsertLecturer.setString(2, lecturer.getDesignation());
            stmInsertLecturer.setString(3, lecturer.getQualifications());
            stmInsertLecturer.setString(4, lecturer.getLinkedin());
            stmInsertLecturer.executeUpdate();
            ResultSet generatedKeys = stmInsertLecturer.getGeneratedKeys();
            generatedKeys.next();
            int lecturerId = generatedKeys.getInt("id");
            String picture = lecturerId + "_" + lecturer.getName(); // create picture path with ID & NAME
            // Path of picture which put in to the bucket

            if (lecturer.getPicture() != null || lecturer.getPicture().isEmpty()) {
                PreparedStatement stmUpdateLecturer = connection
                        .prepareStatement("UPDATE lecturer SET picture = ? WHERE Id =?");
                stmUpdateLecturer.setString(1, picture);
                // bucket eka athule tyena picture eke path eka
                stmUpdateLecturer.setInt(2, lecturerId);
                stmUpdateLecturer.executeUpdate();
            }

            final String table = lecturer.getType().equalsIgnoreCase("full-time")
                    ? "full+time_Rank": "part_time_rank";

                Statement stm = connection.createStatement();
                ResultSet rst = stm
                        .executeQuery("SELECT `rank` FROM full_time_rank ORDER BY `rank` DESC LIMIT 1");
                int rank;
                if(!rst.next()) rank = 1;
                else rank= rst.getInt("rank")+ 1;

                PreparedStatement stmInsertRank = connection
                        .prepareStatement("INSERT INTO full_time_rank (lecturer_id, `rank`) VALUES (?, ?)");
                stmInsertRank.setInt(1, lecturerId);
                stmInsertRank.setInt(2, rank);
                stmInsertRank.executeUpdate();

            connection.commit();
        }catch (Throwable t) {
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(lecturer);
//        System.out.println("createNewLecturer()");
    }
    @PatchMapping("/{lecturer-id}")
    public void updateLecturerDetails(){
        System.out.println("updateLecturerDetails()");
    }
    @DeleteMapping("/{lecturer-id}")
    public void deleteLecturer(){
        System.out.println("deleteLecturer()");
    }
    @GetMapping
    public void getAllLecturers(){
        System.out.println("getAllLecturers()");
    }
}
