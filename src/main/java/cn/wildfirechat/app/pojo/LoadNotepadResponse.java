package cn.wildfirechat.app.pojo;

import cn.wildfirechat.app.jpa.NotepadEntry;

import java.util.List;

public class LoadNotepadResponse {
    public List<NotepadEntry> items;
    public boolean hasMore;
}

