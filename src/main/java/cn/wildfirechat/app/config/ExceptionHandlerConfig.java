package cn.wildfirechat.app.config;

import cn.wildfirechat.app.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler(ConversionFailedException.class)
    public RestResult<Object> onBindException(ConversionFailedException ex) {
        log.error("ExceptionHandler: ", ex);
        return RestResult.error(String.format("类型%s转换为%s失败 %s", ex.getSourceType(),ex.getTargetType(),ex.getValue()));
    }

    @ExceptionHandler(value = BindException.class)
    public RestResult<Object> onBindException(BindException ex) {
        log.error("ExceptionHandler: ", ex);
        FieldError fieldError = ex.getFieldError();
        if (fieldError != null) {
            return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER)
                    .setMessage(String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
    }

    @ExceptionHandler(value = Exception.class)
    public RestResult<Object> Exception(Exception ex) {
        log.error("ExceptionHandler: ", ex);
        return RestResult.error("请求出错："+ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public RestResult<Object> IllegalArgumentException(IllegalArgumentException ex) {
        log.error("ExceptionHandler: ", ex);
        return RestResult.error(ex.getMessage());
    }


}
