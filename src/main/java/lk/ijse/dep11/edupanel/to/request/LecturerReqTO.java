package lk.ijse.dep11.edupanel.to.request;

import lk.ijse.dep11.edupanel.validator.LecturerImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerReqTO {

    @NotBlank(message = "Name can't be empty")
    @Pattern(regexp = "^[A-ZZa-z ]+$", message = "Invalid Name: ${value}")
    private String name;

    @NotBlank(message = "Designation can't be empty")
    @Length(min = 2, message = "Invalid Designation: {value}")
    private String designation;

    @NotBlank(message = "Qualification can't be empty")
    @Length(min = 2, message = "Invalid Qualification: {value}")
    private String qualifications;

    @NotBlank(message = "Type can't be empty")
    @Pattern(regexp = "^(full-time|part-time)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid Type")
    private String type;

    @LecturerImage(/* maximumFileSize = 200*1024 */)
    private MultipartFile picture;

    @Pattern(regexp = "^http[s]?://.+$", message = "Invalid Linkedin URL")
    private String linkedin;
}
