package lk.ijse.dep11.edupanel.api;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lk.ijse.dep11.edupanel.to.request.response.LecturerResTo;
import lk.ijse.dep11.edupanel.to.request.LecturerReqTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/lecturers")
@CrossOrigin
public class LecturerHttpController {

    @Autowired
    private DataSource pool;

    @Autowired
    private Bucket bucket;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    public LecturerResTo createNewLecturer(@ModelAttribute @Valid LecturerReqTO lecturer){
        try(Connection connection = pool.getConnection()) {
            connection.setAutoCommit(false);

            try{
                PreparedStatement stmInsertLecturer = connection
                    .prepareStatement("INSERT INTO  lecturer" +
                        "(name, designation, qualifications, linkedin) " +
                        "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmInsertLecturer.setString(1, lecturer.getName());
            stmInsertLecturer.setString(2, lecturer.getDesignation());
            stmInsertLecturer.setString(3, lecturer.getQualifications());
            stmInsertLecturer.setString(4, lecturer.getLinkedin());
            stmInsertLecturer.executeUpdate();
            ResultSet generatedKeys = stmInsertLecturer.getGeneratedKeys();
            generatedKeys.next();
            int lecturerId = generatedKeys.getInt(1);
            String picture = lecturerId + "_" + lecturer.getName(); // create picture path with ID & NAME
            // Path of picture which put in to the bucket

            if (lecturer.getPicture() != null || !lecturer.getPicture().isEmpty()) {
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
                        .executeQuery("SELECT `rank` FROM " + table + " ORDER BY `rank` DESC LIMIT 1");
                int rank;
                if(!rst.next()) rank = 1;
                else rank= rst.getInt("rank")+ 1;

                PreparedStatement stmInsertRank = connection
                        .prepareStatement("INSERT INTO "+ table + " (lecturer_id, `rank`) VALUES (?, ?)");
                stmInsertRank.setInt(1, lecturerId);
                stmInsertRank.setInt(2, rank);
                stmInsertRank.executeUpdate();

                String pictureUrl = null;
                if(lecturer.getPicture() != null || !lecturer.getPicture().isEmpty()){
                    Blob blob = bucket.create(picture, lecturer.getPicture().getInputStream(),
                            lecturer.getPicture().getContentType());
                    pictureUrl = blob.signUrl(1, TimeUnit.DAYS, Storage.SignUrlOption.withV4Signature())
                            .toString();
                }
            connection.commit();
            return new LecturerResTo(lecturerId,
                    lecturer.getName(),
                    lecturer.getDesignation(),
                    lecturer.getQualifications(),
                    lecturer.getType(),
                    pictureUrl,
                    lecturer.getLinkedin());
        }catch (Throwable t) {
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(lecturer);
//        System.out.println("createNewLecturer()");
    }
    @PatchMapping("/{lecturer-id}")
    public void updateLecturerDetails(){
        System.out.println("updateLecturerDetails()");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{lecturer-id}")
    public void deleteLecturer(@PathVariable("lecturer-id") int lecturerId){
        try {
            Connection connection = pool.getConnection();
            PreparedStatement stmExists = connection.prepareStatement("SELECT * FROM lecturer WHERE id=?");
            stmExists.setInt(1, lecturerId);
            if (!stmExists.executeQuery().next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            connection.setAutoCommit(false);
            try{
                // Our Codes goes here
                PreparedStatement stmIdentity = connection.prepareStatement("SELECT l.id, l.name, l.picture, " +
                        "ftr.`rank` AS ftr, ptr.`rank` AS ptr FROM lecturer l " +
                        "LEFT OUTER JOIN full_time_rank ftr ON l.id = ftr.lecturer_id " +
                        "LEFT OUTER JOIN part_time_rank ptr ON l.id = ptr.lecturer_id " +
                        "WHERE l.id = ?");
                stmIdentity.setInt(1, lecturerId);
                ResultSet rst = stmIdentity.executeQuery();
                rst.next();
                int ftr = rst.getInt("ftr");
                int ptr = rst.getInt("ptr");
                String picture = rst.getString("picture");
                String tableName = ftr > 0 ? "full_time_Rank" : "part_time_Rank";
                int rank = ftr > 0 ? ftr :ptr;

                Statement stmDeleteRank = connection.createStatement();
                stmDeleteRank.executeUpdate("DELETE FROM " + tableName + " WHERE `rank`= " +rank);

                Statement stmShift = connection.createStatement();
                stmShift.executeUpdate("UPDATE "+ tableName +" SET `rank` =`rank` -1 WHERE `rank` > "
                + rank);

                PreparedStatement stmDeleteLecturer = connection.prepareStatement("DELETE FROM lecturer WHERE id = ?");
                stmDeleteLecturer.setInt(1, lecturerId);
                stmDeleteLecturer.executeUpdate();

                if (picture != null) bucket.get(picture).delete();

                connection.commit();
            }catch (Throwable t){
                connection.rollback();
            }finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("deleteLecturer()");

    }
    @GetMapping
    public void getAllLecturers(){

        System.out.println("getAllLecturers()");
    }
}
