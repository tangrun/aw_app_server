package cn.wildfirechat.app.config;

import cn.wildfirechat.app.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler(value = Exception.class)
    public RestResult Exception(Exception ex) {
        log.error("ExceptionHandler: ",ex);
        RestResult error = RestResult.error(RestResult.RestCode.ERROR_REQUEST_ERROR);
        return error;
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public RestResult IllegalArgumentException(IllegalArgumentException ex) {
        log.error("ExceptionHandler: ",ex);
        RestResult error = RestResult.error(RestResult.RestCode.ERROR_INVALID_PARAMETER);
        error.setMessage(ex.getMessage());
        return error;
    }


}
