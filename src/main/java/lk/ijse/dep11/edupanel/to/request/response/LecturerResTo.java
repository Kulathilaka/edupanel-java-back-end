package lk.ijse.dep11.edupanel.to.request.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerResTo {
    private Integer id;
    private String name;
    private String designation;
    private String qualifications;
    private String type;
    private String pictureUrl;
    private String linkedin;
}
