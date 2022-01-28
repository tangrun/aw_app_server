package cn.wildfirechat.app.common.pojo;

import lombok.Data;
import org.springframework.data.domain.PageRequest;

@Data
public class Page {

    private int page;
    private int size;

    public PageRequest convert2PageRequest(){
        return PageRequest.of(Math.max(page-1, 0), Math.max(size, 1));
    }
}
