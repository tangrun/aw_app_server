package cn.wildfirechat.app.work.countdown.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import javafx.util.converter.DateTimeStringConverter;
import lombok.Data;

import java.util.Date;

@Data
public class CountDownInfo {

    private Long id;

    private Date date;

    private String title;

    private String type;

    private Date createTime;

    private Long remind;

    private Boolean top;
}
