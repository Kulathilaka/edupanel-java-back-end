package lk.ijse.dep11.edupanel.validator;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LecturerImageConstraintValidator implements
        ConstraintValidator<LecturerImage, MultipartFile /* annotation name, data type(ex:-Object) */> {
    private long maximumFileSize;
    @Override
    public void initialize(LecturerImage constraintAnnotation) {
        maximumFileSize = constraintAnnotation.maximumFileSize();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        if(multipartFile.getSize() > maximumFileSize) return false;
        // size validation
        if(multipartFile.getContentType() == null) return false;
        // content type should not be null
        if(multipartFile == null || multipartFile.isEmpty()) return true;
        // picture should not add compulsory
        if(!multipartFile.getContentType().startsWith("image/")) return false;
        // it's content type should be image type
        return true;
    }
}
